package cl.duoc.msinventario.dto;

public record InventarioResponse(
        Long id,
        Long productoId,
        int stockActual,
        int stockMinimo,
        boolean bajoStockMinimo
) {
}
