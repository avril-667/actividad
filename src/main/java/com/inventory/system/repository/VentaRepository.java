package com.inventory.system.repository;

import com.inventory.system.entity.Venta;
import com.inventory.system.entity.Usuario;
import com.inventory.system.entity.Especialidad;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Venta.
 * Incluye consultas para reportes y generación de números de venta.
 */
@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    /**
     * Busca una venta por su número único.
     */
    Optional<Venta> findByNumeroVenta(String numeroVenta);

    /**
     * Busca ventas por usuario (vendedor).
     */
    List<Venta> findByUsuarioOrderByFechaVentaDesc(Usuario usuario);

    /**
     * Busca ventas por especialidad.
     */
    List<Venta> findByEspecialidadOrderByFechaVentaDesc(Especialidad especialidad);

    /**
     * Busca ventas por estado.
     */
    List<Venta> findByEstado(Venta.EstadoVenta estado);

    /**
     * Busca ventas en un rango de fechas.
     */
    @Query("SELECT v FROM Venta v WHERE v.fechaVenta BETWEEN :inicio AND :fin ORDER BY v.fechaVenta DESC")
    List<Venta> findByFechaVentaBetween(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    /**
     * Busca ventas por especialidad en un rango de fechas.
     */
    @Query("SELECT v FROM Venta v WHERE v.especialidad.id = :especialidadId AND v.fechaVenta BETWEEN :inicio AND :fin ORDER BY v.fechaVenta DESC")
    List<Venta> findByEspecialidadIdAndFechaVentaBetween(@Param("especialidadId") Long especialidadId, @Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    /**
     * Busca ventas por especialidad y estado en un rango de fechas.
     */
    @Query("SELECT v FROM Venta v WHERE v.especialidad.id = :especialidadId AND v.estado = :estado AND v.fechaVenta BETWEEN :inicio AND :fin ORDER BY v.fechaVenta DESC")
    List<Venta> findByEspecialidadIdAndEstadoAndFechaVentaBetween(@Param("especialidadId") Long especialidadId, @Param("estado") Venta.EstadoVenta estado, @Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    /**
     * Busca ventas con paginación por especialidad y rango de fechas.
     */
    Page<Venta> findByEspecialidadIdAndFechaVentaBetween(@Param("especialidadId") Long especialidadId, @Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin, Pageable pageable);

    /**
     * Busca ventas por nombre de cliente (búsqueda parcial).
     */
    @Query("SELECT v FROM Venta v WHERE LOWER(v.clienteNombre) LIKE LOWER(CONCAT('%', :termino, '%')) AND v.especialidad.id = :especialidadId ORDER BY v.fechaVenta DESC")
    List<Venta> buscarPorCliente(@Param("termino") String termino, @Param("especialidadId") Long especialidadId);

    // =====================================================================
    // Consultas para reportes y estadísticas
    // =====================================================================

    /**
     * Obtiene el total de ventas (ingresos) por especialidad en un rango de fechas.
     */
    @Query("SELECT COALESCE(SUM(v.total), 0.0) FROM Venta v WHERE v.especialidad.id = :especialidadId AND v.estado = 'COMPLETADA' AND v.fechaVenta BETWEEN :inicio AND :fin")
    BigDecimal getTotalVentasByEspecialidadAndFechaBetween(@Param("especialidadId") Long especialidadId, @Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    /**
     * Obtiene el conteo de ventas completadas por especialidad en un rango de fechas.
     */
    @Query("SELECT COUNT(v) FROM Venta v WHERE v.especialidad.id = :especialidadId AND v.estado = 'COMPLETADA' AND v.fechaVenta BETWEEN :inicio AND :fin")
    long countVentasCompletadasByEspecialidadAndFechaBetween(@Param("especialidadId") Long especialidadId, @Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    /**
     * Obtiene el ticket promedio por especialidad en un rango de fechas.
     */
    @Query("SELECT COALESCE(AVG(v.total), 0.0) FROM Venta v WHERE v.especialidad.id = :especialidadId AND v.estado = 'COMPLETADA' AND v.fechaVenta BETWEEN :inicio AND :fin")
    BigDecimal getTicketPromedioByEspecialidadAndFechaBetween(@Param("especialidadId") Long especialidadId, @Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    /**
     * Obtiene ventas agrupadas por día para reporte diario.
     */
    @Query("SELECT DATE(v.fechaVenta) as fecha, COUNT(v) as totalVentas, COALESCE(SUM(v.total), 0.0) as ingresoTotal FROM Venta v WHERE v.especialidad.id = :especialidadId AND v.estado = 'COMPLETADA' AND v.fechaVenta BETWEEN :inicio AND :fin GROUP BY DATE(v.fechaVenta) ORDER BY DATE(v.fechaVenta) DESC")
    List<Object[]> getResumenDiarioByEspecialidad(@Param("especialidadId") Long especialidadId, @Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    /**
     * Obtiene el último número de venta generado para una especialidad en la fecha actual.
     * Útil para generar números secuenciales: VTA-YYYYMMDD-XXXX
     */
    @Query("SELECT v.numeroVenta FROM Venta v WHERE v.especialidad.id = :especialidadId AND CAST(v.fechaVenta as LocalDate) = CURRENT_DATE ORDER BY v.fechaVenta DESC")
    List<String> getUltimosNumerosVentaHoy(@Param("especialidadId") Long especialidadId, Pageable pageable);

    /**
     * Verifica si existe un número de venta.
     */
    boolean existsByNumeroVenta(String numeroVenta);
}