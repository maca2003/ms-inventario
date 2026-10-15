package cl.duoc.msinventario.controller;

import cl.duoc.msinventario.dto.InventarioRequest;
import cl.duoc.msinventario.dto.InventarioResponse;
import cl.duoc.msinventario.dto.LoteRequest;
import cl.duoc.msinventario.dto.LoteResponse;
import cl.duoc.msinventario.dto.MovimientoRequest;
import cl.duoc.msinventario.dto.MovimientoResponse;
import cl.duoc.msinventario.service.InventarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inventario")
public class InventarioController {

    private final InventarioService inventarioService;

    public InventarioController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    @GetMapping
    public ResponseEntity<List<InventarioResponse>> listar() {
        return ResponseEntity.ok(inventarioService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventarioResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(inventarioService.obtener(id));
    }

    @GetMapping("/producto/{productoId}")
    public ResponseEntity<InventarioResponse> obtenerPorProducto(@PathVariable Long productoId) {
        return ResponseEntity.ok(inventarioService.obtenerPorProducto(productoId));
    }

    @PostMapping
    public ResponseEntity<InventarioResponse> crear(@Valid @RequestBody InventarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventarioService.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InventarioResponse> actualizar(@PathVariable Long id,
                                                         @Valid @RequestBody InventarioRequest request) {
        return ResponseEntity.ok(inventarioService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        inventarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/lotes")
    public ResponseEntity<LoteResponse> agregarLote(@PathVariable Long id,
                                                    @Valid @RequestBody LoteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventarioService.agregarLote(id, request));
    }

    @GetMapping("/{id}/lotes")
    public ResponseEntity<List<LoteResponse>> listarLotes(@PathVariable Long id) {
        return ResponseEntity.ok(inventarioService.listarLotes(id));
    }

    @GetMapping("/{id}/movimientos")
    public ResponseEntity<List<MovimientoResponse>> listarMovimientos(@PathVariable Long id) {
        return ResponseEntity.ok(inventarioService.listarMovimientos(id));
    }

    @PostMapping("/producto/{productoId}/salida")
    public ResponseEntity<InventarioResponse> registrarSalida(@PathVariable Long productoId,
                                                              @Valid @RequestBody MovimientoRequest request) {
        return ResponseEntity.ok(inventarioService.registrarSalida(productoId, request));
    }

    @PostMapping("/producto/{productoId}/entrada")
    public ResponseEntity<InventarioResponse> registrarEntrada(@PathVariable Long productoId,
                                                               @Valid @RequestBody MovimientoRequest request) {
        return ResponseEntity.ok(inventarioService.registrarEntrada(productoId, request));
    }
}
