package cl.duoc.msinventario.dto;

import java.time.LocalDate;

public record LoteResponse(
        Long id,
        String codigo,
        int cantidad,
        LocalDate fechaVencimiento
) {
}
