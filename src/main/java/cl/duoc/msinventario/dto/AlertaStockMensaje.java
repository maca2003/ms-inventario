package cl.duoc.msinventario.dto;

/**
 * Mensaje que se publica en la cola SQS cuando un producto queda con stock bajo.
 * Lo consume la función serverless fn-alertas-inventario.
 */
public record AlertaStockMensaje(
        Long productoId,
        int stockActual,
        int stockMinimo,
        String fecha
) {
}
