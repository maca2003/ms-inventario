package cl.duoc.msinventario.repository;

import cl.duoc.msinventario.model.Inventario;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventarioRepository extends JpaRepository<Inventario, Long> {

    Optional<Inventario> findByProductoId(Long productoId);

    boolean existsByProductoId(Long productoId);

    boolean existsByProductoIdAndIdNot(Long productoId, Long id);

    // Bloqueo pesimista (SELECT ... FOR UPDATE): evita que dos ventas simultáneas
    // descuenten el mismo stock y lo dejen negativo.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventario i WHERE i.productoId = :productoId")
    Optional<Inventario> findByProductoIdParaActualizar(@Param("productoId") Long productoId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventario i WHERE i.id = :id")
    Optional<Inventario> findByIdParaActualizar(@Param("id") Long id);
}
