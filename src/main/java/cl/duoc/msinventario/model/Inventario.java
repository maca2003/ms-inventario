package cl.duoc.msinventario.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inventarios")
public class Inventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Id del producto en MS-PRODUCTOS (cada microservicio tiene su propia base de datos)
    @Column(name = "producto_id", nullable = false, unique = true)
    private Long productoId;

    @Column(name = "stock_actual", nullable = false)
    private int stockActual = 0;

    @Column(name = "stock_minimo", nullable = false)
    private int stockMinimo = 0;

    // Un inventario tiene muchos lotes
    @OneToMany(mappedBy = "inventario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Lote> lotes = new ArrayList<>();

    // Un inventario tiene muchos movimientos
    @OneToMany(mappedBy = "inventario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Movimiento> movimientos = new ArrayList<>();

    public Inventario() {
    }

    public Inventario(Long productoId, int stockMinimo) {
        this.productoId = productoId;
        this.stockMinimo = stockMinimo;
    }

    public void agregarLote(Lote lote) {
        lote.setInventario(this);
        lotes.add(lote);
    }

    public void agregarMovimiento(Movimiento movimiento) {
        movimiento.setInventario(this);
        movimientos.add(movimiento);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public int getStockActual() {
        return stockActual;
    }

    public void setStockActual(int stockActual) {
        this.stockActual = stockActual;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(int stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public List<Lote> getLotes() {
        return lotes;
    }

    public void setLotes(List<Lote> lotes) {
        this.lotes = lotes;
    }

    public List<Movimiento> getMovimientos() {
        return movimientos;
    }

    public void setMovimientos(List<Movimiento> movimientos) {
        this.movimientos = movimientos;
    }
}
