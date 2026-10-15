package cl.duoc.msinventario.repository;

import cl.duoc.msinventario.model.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    List<Movimiento> findByInventarioIdOrderByFechaDesc(Long inventarioId);
}
