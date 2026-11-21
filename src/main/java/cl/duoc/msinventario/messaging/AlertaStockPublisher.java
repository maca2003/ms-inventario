package cl.duoc.msinventario.messaging;

import cl.duoc.msinventario.dto.AlertaStockMensaje;
import cl.duoc.msinventario.model.Inventario;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.time.LocalDateTime;

/**
 * Productor de la cola "inventario-alertas" (Amazon SQS).
 * Publica un evento asíncrono cuando el stock de un producto llega al mínimo.
 */
@Component
public class AlertaStockPublisher {

    private static final Logger log = LoggerFactory.getLogger(AlertaStockPublisher.class);

    private final String colaUrl;
    private final ObjectMapper objectMapper;
    private final SqsClient sqsClient;

    public AlertaStockPublisher(@Value("${aws.sqs.alertas-url}") String colaUrl,
                                @Value("${aws.region}") String region,
                                ObjectMapper objectMapper) {
        this.colaUrl = colaUrl;
        this.objectMapper = objectMapper;
        // Las credenciales se obtienen del rol de la instancia EC2 (LabRole) o de las variables de entorno
        this.sqsClient = colaUrl.isBlank() ? null : SqsClient.builder().region(Region.of(region)).build();
    }

    /**
     * Publica la alerta solo después de que la transacción se confirma,
     * para no avisar de un descuento de stock que finalmente no se guardó.
     */
    public void publicarStockBajo(Inventario inventario) {
        AlertaStockMensaje mensaje = new AlertaStockMensaje(
                inventario.getProductoId(),
                inventario.getStockActual(),
                inventario.getStockMinimo(),
                LocalDateTime.now().toString());

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    enviar(mensaje);
                }
            });
        } else {
            enviar(mensaje);
        }
    }

    private void enviar(AlertaStockMensaje mensaje) {
        if (sqsClient == null) {
            log.info("SQS no configurado (SQS_ALERTAS_URL vacío). Alerta no enviada: {}", mensaje);
            return;
        }
        try {
            String cuerpo = objectMapper.writeValueAsString(mensaje);
            sqsClient.sendMessage(SendMessageRequest.builder()
                    .queueUrl(colaUrl)
                    .messageBody(cuerpo)
                    .build());
            log.info("Alerta de stock bajo publicada en SQS: {}", cuerpo);
        } catch (JsonProcessingException | RuntimeException ex) {
            // Una falla en la cola no debe impedir la venta: solo se registra
            log.error("No se pudo publicar la alerta en SQS: {}", ex.getMessage());
        }
    }
}
