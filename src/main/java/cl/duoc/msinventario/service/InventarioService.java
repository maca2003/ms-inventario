package cl.duoc.msinventario.service;

import cl.duoc.msinventario.dto.InventarioRequest;
import cl.duoc.msinventario.dto.InventarioResponse;
import cl.duoc.msinventario.dto.LoteRequest;
import cl.duoc.msinventario.dto.LoteResponse;
import cl.duoc.msinventario.dto.MovimientoRequest;
import cl.duoc.msinventario.dto.MovimientoResponse;
import cl.duoc.msinventario.exception.ConflictoException;
import cl.duoc.msinventario.exception.RecursoNoEncontradoException;
import cl.duoc.msinventario.model.Inventario;
import cl.duoc.msinventario.model.Lote;
import cl.duoc.msinventario.model.Movimiento;
import cl.duoc.msinventario.model.TipoMovimiento;
import cl.duoc.msinventario.repository.InventarioRepository;
import cl.duoc.msinventario.repository.LoteRepository;
import cl.duoc.msinventario.repository.MovimientoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventarioService {

    private final InventarioRepository inventarioRepository;
    private final LoteRepository loteRepository;
    private final MovimientoRepository movimientoRepository;

    public InventarioService(InventarioRepository inventarioRepository,
                             LoteRepository loteRepository,
                             MovimientoRepository movimientoRepository) {
        this.inventarioRepository = inventarioRepository;
        this.loteRepository = loteRepository;
        this.movimientoRepository = movimientoRepository;
    }

    @Transactional(readOnly = true)
    public List<InventarioResponse> listar() {
        return inventarioRepository.findAll().stream().map(this::aResponse).toList();
    }

    @Transactional(readOnly = true)
    public InventarioResponse obtener(Long id) {
        return aResponse(buscar(id));
    }

    @Transactional(readOnly = true)
    public InventarioResponse obtenerPorProducto(Long productoId) {
        return aResponse(buscarPorProducto(productoId));
    }

    @Transactional
    public InventarioResponse crear(InventarioRequest request) {
        if (inventarioRepository.existsByProductoId(request.productoId())) {
            throw new ConflictoException("Ya existe inventario para el producto " + request.productoId());
        }
        Inventario inventario = new Inventario(request.productoId(), request.stockMinimo());
        return aResponse(inventarioRepository.save(inventario));
    }

    @Transactional
    public InventarioResponse actualizar(Long id, InventarioRequest request) {
        Inventario inventario = buscar(id);
        if (inventarioRepository.existsByProductoIdAndIdNot(request.productoId(), id)) {
            throw new ConflictoException("Ya existe inventario para el producto " + request.productoId());
        }
        inventario.setProductoId(request.productoId());
        inventario.setStockMinimo(request.stockMinimo());
        return aResponse(inventarioRepository.save(inventario));
    }

    @Transactional
    public void eliminar(Long id) {
        inventarioRepository.delete(buscar(id));
    }

    @Transactional
    public LoteResponse agregarLote(Long inventarioId, LoteRequest request) {
        Inventario inventario = inventarioRepository.findByIdParaActualizar(inventarioId)
                .orElseThrow(() -> noEncontrado(inventarioId));

        Lote lote = new Lote(request.codigo(), request.cantidad(), request.fechaVencimiento());
        inventario.agregarLote(lote);
        loteRepository.save(lote);

        inventario.setStockActual(inventario.getStockActual() + request.cantidad());
        registrarMovimiento(inventario, TipoMovimiento.ENTRADA, request.cantidad(), "Ingreso lote " + request.codigo());

        return new LoteResponse(lote.getId(), lote.getCodigo(), lote.getCantidad(), lote.getFechaVencimiento());
    }

    @Transactional(readOnly = true)
    public List<LoteResponse> listarLotes(Long inventarioId) {
        buscar(inventarioId);
        return loteRepository.findByInventarioIdOrderByFechaVencimientoAsc(inventarioId).stream()
                .map(l -> new LoteResponse(l.getId(), l.getCodigo(), l.getCantidad(), l.getFechaVencimiento()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MovimientoResponse> listarMovimientos(Long inventarioId) {
        buscar(inventarioId);
        return movimientoRepository.findByInventarioIdOrderByFechaDesc(inventarioId).stream()
                .map(m -> new MovimientoResponse(m.getId(), m.getTipo(), m.getCantidad(), m.getFecha(), m.getMotivo()))
                .toList();
    }

    /**
     * Descuenta stock de un producto. Usa bloqueo pesimista para que dos ventas
     * concurrentes no puedan dejar el stock en negativo.
     */
    @Transactional
    public InventarioResponse registrarSalida(Long productoId, MovimientoRequest request) {
        Inventario inventario = inventarioRepository.findByProductoIdParaActualizar(productoId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe inventario para el producto " + productoId));

        if (inventario.getStockActual() < request.cantidad()) {
            throw new ConflictoException("Stock insuficiente para el producto " + productoId
                    + ". Disponible: " + inventario.getStockActual() + ", solicitado: " + request.cantidad());
        }

        inventario.setStockActual(inventario.getStockActual() - request.cantidad());
        registrarMovimiento(inventario, TipoMovimiento.SALIDA, request.cantidad(), request.motivo());
        return aResponse(inventario);
    }

    /**
     * Devuelve stock a un producto (por ejemplo, al anular una venta).
     */
    @Transactional
    public InventarioResponse registrarEntrada(Long productoId, MovimientoRequest request) {
        Inventario inventario = inventarioRepository.findByProductoIdParaActualizar(productoId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe inventario para el producto " + productoId));

        inventario.setStockActual(inventario.getStockActual() + request.cantidad());
        registrarMovimiento(inventario, TipoMovimiento.ENTRADA, request.cantidad(), request.motivo());
        return aResponse(inventario);
    }

    private void registrarMovimiento(Inventario inventario, TipoMovimiento tipo, int cantidad, String motivo) {
        Movimiento movimiento = new Movimiento(tipo, cantidad, motivo);
        inventario.agregarMovimiento(movimiento);
        movimientoRepository.save(movimiento);
    }

    private Inventario buscar(Long id) {
        return inventarioRepository.findById(id).orElseThrow(() -> noEncontrado(id));
    }

    private Inventario buscarPorProducto(Long productoId) {
        return inventarioRepository.findByProductoId(productoId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe inventario para el producto " + productoId));
    }

    private RecursoNoEncontradoException noEncontrado(Long id) {
        return new RecursoNoEncontradoException("Inventario con id " + id + " no encontrado");
    }

    private InventarioResponse aResponse(Inventario inventario) {
        return new InventarioResponse(
                inventario.getId(),
                inventario.getProductoId(),
                inventario.getStockActual(),
                inventario.getStockMinimo(),
                inventario.getStockActual() <= inventario.getStockMinimo());
    }
}
