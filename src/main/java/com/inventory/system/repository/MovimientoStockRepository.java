package com.inventory.system.repository;

import com.inventory.system.entity.MovimientoStock;
import com.inventory.system.entity.Producto;
import com.inventory.system.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para la entidad MovimientoStock.
 * Permite consultar el historial de movimientos con filtros.
 */
@Repository
public interface MovimientoStockRepository extends JpaRepository<MovimientoStock, Long> {

    /**
     * Busca movimientos por producto ordenados por fecha descendente.
     */
    List<MovimientoStock> findByProductoOrderByFechaMovimientoDesc(Producto producto);

    /**
     * Busca movimientos por producto con paginación.
     */
    Page<MovimientoStock> findByProductoOrderByFechaMovimientoDesc(Producto producto, Pageable pageable);

    /**
     * Busca movimientos por tipo.
     */
    List<MovimientoStock> findByTipoMovimiento(MovimientoStock.TipoMovimiento tipoMovimiento);

    /**
     * Busca movimientos por usuario.
     */
    List<MovimientoStock> findByUsuarioOrderByFechaMovimientoDesc(Usuario usuario);

    /**
     * Busca movimientos en un rango de fechas.
     */
    @Query("SELECT m FROM MovimientoStock m WHERE m.fechaMovimiento BETWEEN :inicio AND :fin ORDER BY m.fechaMovimiento DESC")
    List<MovimientoStock> findByFechaMovimientoBetween(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    /**
     * Busca movimientos por tipo en un rango de fechas.
     */
    @Query("SELECT m FROM MovimientoStock m WHERE m.tipoMovimiento = :tipo AND m.fechaMovimiento BETWEEN :inicio AND :fin ORDER BY m.fechaMovimiento DESC")
    List<MovimientoStock> findByTipoMovimientoAndFechaMovimientoBetween(@Param("tipo") MovimientoStock.TipoMovimiento tipo, @Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    /**
     * Busca movimientos por producto en un rango de fechas.
     */
    @Query("SELECT m FROM MovimientoStock m WHERE m.producto.id = :productoId AND m.fechaMovimiento BETWEEN :inicio AND :fin ORDER BY m.fechaMovimiento DESC")
    List<MovimientoStock> findByProductoIdAndFechaMovimientoBetween(@Param("productoId") Long productoId, @Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    /**
     * Busca movimientos por referencia (ej. ID de venta).
     */
    @Query("SELECT m FROM MovimientoStock m WHERE m.referenciaTipo = :referenciaTipo AND m.referenciaId = :referenciaId ORDER BY m.fechaMovimiento DESC")
    List<MovimientoStock> findByReferencia(@Param("referenciaTipo") String referenciaTipo, @Param("referenciaId") Long referenciaId);

    /**
     * Obtiene el último movimiento de un producto.
     */
    @Query("SELECT m FROM MovimientoStock m WHERE m.producto.id = :productoId ORDER BY m.fechaMovimiento DESC")
    List<MovimientoStock> findUltimoMovimientoByProductoId(@Param("productoId") Long productoId, Pageable pageable);

    /**
     * Cuenta movimientos por tipo en un rango de fechas.
     */
    @Query("SELECT COUNT(m) FROM MovimientoStock m WHERE m.tipoMovimiento = :tipo AND m.fechaMovimiento BETWEEN :inicio AND :fin")
    long countByTipoMovimientoAndFechaBetween(@Param("tipo") MovimientoStock.TipoMovimiento tipo, @Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    /**
     * Obtiene resumen de entradas/salidas por producto en un período.
     */
    @Query("SELECT m.tipoMovimiento, SUM(m.cantidad) FROM MovimientoStock m WHERE m.producto.id = :productoId AND m.fechaMovimiento BETWEEN :inicio AND :fin GROUP BY m.tipoMovimiento")
    List<Object[]> getResumenMovimientosByProducto(@Param("productoId") Long productoId, @Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);
}