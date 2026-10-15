package cl.duoc.msinventario.dto;

import cl.duoc.msinventario.model.TipoMovimiento;

import java.time.LocalDateTime;

public record MovimientoResponse(
        Long id,
        TipoMovimiento tipo,
        int cantidad,
        LocalDateTime fecha,
        String motivo
) {
}
