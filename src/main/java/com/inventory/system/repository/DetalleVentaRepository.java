package com.inventory.system.repository;

import com.inventory.system.entity.DetalleVenta;
import com.inventory.system.entity.Producto;
import com.inventory.system.entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la entidad DetalleVenta.
 * Permite consultar líneas de venta y estadísticas de productos vendidos.
 */
@Repository
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {

    /**
     * Busca detalles por venta.
     */
    List<DetalleVenta> findByVenta(Venta venta);

    /**
     * Busca detalles por producto.
     */
    List<DetalleVenta> findByProducto(Producto producto);

    /**
     * Busca detalles por venta con producto cargado (fetch join).
     */
    @Query("SELECT dv FROM DetalleVenta dv JOIN FETCH dv.producto WHERE dv.venta.id = :ventaId")
    List<DetalleVenta> findByVentaIdWithProducto(@Param("ventaId") Long ventaId);

    // =====================================================================
    // Consultas para reportes de productos más vendidos
    // =====================================================================

    /**
     * Obtiene los productos más vendidos (por cantidad) en un rango de fechas para una especialidad.
     */
    @Query("SELECT dv.producto, SUM(dv.cantidad) as totalVendido, SUM(dv.subtotalLinea) as ingresoTotal " +
           "FROM DetalleVenta dv " +
           "JOIN dv.venta v " +
           "WHERE v.especialidad.id = :especialidadId " +
           "AND v.estado = 'COMPLETADA' " +
           "AND v.fechaVenta BETWEEN :inicio AND :fin " +
           "GROUP BY dv.producto " +
           "ORDER BY totalVendido DESC")
    List<Object[]> getProductosMasVendidos(@Param("especialidadId") Long especialidadId, @Param("inicio") java.time.LocalDateTime inicio, @Param("fin") java.time.LocalDateTime fin);

    /**
     * Obtiene el total de unidades vendidas de un producto en un rango de fechas.
     */
    @Query("SELECT COALESCE(SUM(dv.cantidad), 0) FROM DetalleVenta dv JOIN dv.venta v WHERE dv.producto.id = :productoId AND v.estado = 'COMPLETADA' AND v.fechaVenta BETWEEN :inicio AND :fin")
    long getTotalUnidadesVendidasByProducto(@Param("productoId") Long productoId, @Param("inicio") java.time.LocalDateTime inicio, @Param("fin") java.time.LocalDateTime fin);

    /**
     * Obtiene el ingreso total generado por un producto en un rango de fechas.
     */
    @Query("SELECT COALESCE(SUM(dv.subtotalLinea), 0) FROM DetalleVenta dv JOIN dv.venta v WHERE dv.producto.id = :productoId AND v.estado = 'COMPLETADA' AND v.fechaVenta BETWEEN :inicio AND :fin")
    java.math.BigDecimal getIngresoTotalByProducto(@Param("productoId") Long productoId, @Param("inicio") java.time.LocalDateTime inicio, @Param("fin") java.time.LocalDateTime fin);
}