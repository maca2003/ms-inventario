package cl.duoc.msinventario.repository;

import cl.duoc.msinventario.model.Lote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoteRepository extends JpaRepository<Lote, Long> {

    List<Lote> findByInventarioIdOrderByFechaVencimientoAsc(Long inventarioId);
}
