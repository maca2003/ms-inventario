package cl.duoc.msinventario.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record InventarioRequest(
        @NotNull(message = "El id del producto es obligatorio")
        Long productoId,

        @NotNull(message = "El stock mínimo es obligatorio")
        @Min(value = 0, message = "El stock mínimo no puede ser negativo")
        Integer stockMinimo
) {
}
