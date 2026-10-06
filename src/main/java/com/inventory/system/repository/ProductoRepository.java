package com.inventory.system.repository;

import com.inventory.system.entity.Producto;
import com.inventory.system.entity.Producto.EstadoStock;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Producto.
 * Incluye consultas específicas para control de stock y alertas.
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // =====================================================================
    // Consultas básicas
    // =====================================================================

    /**
     * Busca un producto por código de barras.
     */
    Optional<Producto> findByCodigoBarras(String codigoBarras);

    /**
     * Busca un producto por código interno.
     */
    Optional<Producto> findByCodigoInterno(String codigoInterno);

    /**
     * Busca productos por categoría.
     */
    List<Producto> findByCategoriaId(Long categoriaId);

    /**
     * Busca productos por especialidad.
     */
    List<Producto> findByEspecialidadId(Long especialidadId);

    /**
     * Busca productos activos por especialidad.
     */
    List<Producto> findByEspecialidadIdAndActivoTrue(Long especialidadId);

    /**
     * Busca productos activos por categoría y especialidad.
     */
    List<Producto> findByCategoriaIdAndEspecialidadIdAndActivoTrue(Long categoriaId, Long especialidadId);

    /**
     * Busca productos por nombre (búsqueda parcial, case-insensitive).
     */
    @Query("SELECT p FROM Producto p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) AND p.activo = true")
    List<Producto> buscarPorNombre(@Param("termino") String termino);

    /**
     * Busca productos con paginación por especialidad y término de búsqueda.
     */
    @Query("SELECT p FROM Producto p WHERE p.especialidad.id = :especialidadId AND p.activo = true AND (LOWER(p.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) OR LOWER(p.codigoInterno) LIKE LOWER(CONCAT('%', :termino, '%')))")
    Page<Producto> buscarPorEspecialidadYTermino(@Param("especialidadId") Long especialidadId, @Param("termino") String termino, Pageable pageable);

    // =====================================================================
    // Consultas para control de stock (KPIs y Alertas)
    // =====================================================================

    /**
     * Cuenta total de productos registrados (activos) por especialidad.
     */
    @Query("SELECT COUNT(p) FROM Producto p WHERE p.especialidad.id = :especialidadId AND p.activo = true")
    long countProductosActivosByEspecialidad(@Param("especialidadId") Long especialidadId);

    /**
     * Cuenta productos con stock bajo (stock > 0 y stock <= stockMinimo) por especialidad.
     */
    @Query("SELECT COUNT(p) FROM Producto p WHERE p.especialidad.id = :especialidadId AND p.activo = true AND p.stockActual > 0 AND p.stockActual <= p.stockMinimo")
    long countProductosLowStockByEspecialidad(@Param("especialidadId") Long especialidadId);

    /**
     * Cuenta productos agotados (stock <= 0) por especialidad.
     */
    @Query("SELECT COUNT(p) FROM Producto p WHERE p.especialidad.id = :especialidadId AND p.activo = true AND p.stockActual <= 0")
    long countProductosOutOfStockByEspecialidad(@Param("especialidadId") Long especialidadId);

    /**
     * Obtiene productos con stock bajo por especialidad.
     */
    @Query("SELECT p FROM Producto p WHERE p.especialidad.id = :especialidadId AND p.activo = true AND p.stockActual > 0 AND p.stockActual <= p.stockMinimo ORDER BY p.stockActual ASC")
    List<Producto> findLowStockByEspecialidad(@Param("especialidadId") Long especialidadId);

    /**
     * Obtiene productos agotados por especialidad.
     */
    @Query("SELECT p FROM Producto p WHERE p.especialidad.id = :especialidadId AND p.activo = true AND p.stockActual <= 0 ORDER BY p.nombre ASC")
    List<Producto> findOutOfStockByEspecialidad(@Param("especialidadId") Long especialidadId);

    /**
     * Obtiene todos los productos con su estado de stock calculado (para reportes).
     */
    @Query("SELECT p FROM Producto p WHERE p.especialidad.id = :especialidadId AND p.activo = true ORDER BY p.categoria.ordenVisualizacion ASC, p.nombre ASC")
    List<Producto> findAllWithStockEstadoByEspecialidad(@Param("especialidadId") Long especialidadId);

    // =====================================================================
    // Operaciones de actualización de stock (Transaccionales)
    // =====================================================================

    /**
     * Busca un producto por ID con bloqueo pesimista (SELECT FOR UPDATE).
     * Útil para operaciones que modifican stock para evitar race conditions.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Producto p WHERE p.id = :id")
    Optional<Producto> findByIdForUpdate(@Param("id") Long id);

    /**
     * Incrementa el stock de un producto (entrada de mercancía).
     * Retorna el nuevo stock actual.
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Producto p SET p.stockActual = p.stockActual + :cantidad, p.fechaActualizacion = CURRENT_TIMESTAMP WHERE p.id = :id")
    int incrementarStock(@Param("id") Long id, @Param("cantidad") int cantidad);

    /**
     * Decrementa el stock de un producto (salida/venta).
     * Solo ejecuta si hay stock suficiente. Retorna filas afectadas (1 si éxito, 0 si stock insuficiente).
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Producto p SET p.stockActual = p.stockActual - :cantidad, p.fechaActualizacion = CURRENT_TIMESTAMP WHERE p.id = :id AND p.stockActual >= :cantidad")
    int decrementarStock(@Param("id") Long id, @Param("cantidad") int cantidad);

    /**
     * Ajusta el stock a un valor específico.
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Producto p SET p.stockActual = :nuevoStock, p.fechaActualizacion = CURRENT_TIMESTAMP WHERE p.id = :id")
    int ajustarStock(@Param("id") Long id, @Param("nuevoStock") int nuevoStock);

    /**
     * Actualiza el stock mínimo de alerta.
     */
    @Modifying
    @Transactional
    @Query("UPDATE Producto p SET p.stockMinimo = :stockMinimo, p.fechaActualizacion = CURRENT_TIMESTAMP WHERE p.id = :id")
    int actualizarStockMinimo(@Param("id") Long id, @Param("stockMinimo") int stockMinimo);

    // =====================================================================
    // Validaciones de unicidad
    // =====================================================================

    /**
     * Verifica si existe un código de barras (excluyendo un ID opcional).
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Producto p WHERE p.codigoBarras = :codigoBarras AND (:idExcluir IS NULL OR p.id <> :idExcluir)")
    boolean existsByCodigoBarrasAndIdNot(@Param("codigoBarras") String codigoBarras, @Param("idExcluir") Long idExcluir);

    /**
     * Verifica si existe un código interno (excluyendo un ID opcional).
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Producto p WHERE UPPER(p.codigoInterno) = UPPER(:codigoInterno) AND (:idExcluir IS NULL OR p.id <> :idExcluir)")
    boolean existsByCodigoInternoIgnoreCaseAndIdNot(@Param("codigoInterno") String codigoInterno, @Param("idExcluir") Long idExcluir);

    /**
     * Verifica si existe un producto con el mismo nombre en la misma especialidad (excluyendo ID).
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Producto p WHERE LOWER(p.nombre) = LOWER(:nombre) AND p.especialidad.id = :especialidadId AND (:idExcluir IS NULL OR p.id <> :idExcluir)")
    boolean existsByNombreAndEspecialidadIdAndIdNot(@Param("nombre") String nombre, @Param("especialidadId") Long especialidadId, @Param("idExcluir") Long idExcluir);
}