package cl.duoc.msinventario.service;

import cl.duoc.msinventario.dto.InventarioResponse;
import cl.duoc.msinventario.dto.MovimientoRequest;
import cl.duoc.msinventario.exception.ConflictoException;
import cl.duoc.msinventario.model.Inventario;
import cl.duoc.msinventario.repository.InventarioRepository;
import cl.duoc.msinventario.repository.LoteRepository;
import cl.duoc.msinventario.repository.MovimientoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventarioServiceTest {

    @Mock
    private InventarioRepository inventarioRepository;

    @Mock
    private LoteRepository loteRepository;

    @Mock
    private MovimientoRepository movimientoRepository;

    @InjectMocks
    private InventarioService inventarioService;

    @Test
    void salidaDescuentaStock() {
        Inventario inventario = new Inventario(1L, 2);
        inventario.setStockActual(10);
        when(inventarioRepository.findByProductoIdParaActualizar(1L)).thenReturn(Optional.of(inventario));

        InventarioResponse response = inventarioService.registrarSalida(1L, new MovimientoRequest(4, "Venta"));

        assertEquals(6, response.stockActual());
    }

    @Test
    void salidaConStockInsuficienteNoDejaStockNegativo() {
        Inventario inventario = new Inventario(1L, 2);
        inventario.setStockActual(3);
        when(inventarioRepository.findByProductoIdParaActualizar(1L)).thenReturn(Optional.of(inventario));

        assertThrows(ConflictoException.class,
                () -> inventarioService.registrarSalida(1L, new MovimientoRequest(5, "Venta")));
        assertEquals(3, inventario.getStockActual());
    }
}
