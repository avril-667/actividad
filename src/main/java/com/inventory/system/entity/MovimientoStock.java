package com.inventory.system.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad que representa un movimiento de stock (entrada, salida, ajuste, venta, devolución).
 * Permite trazabilidad completa de todos los cambios en el inventario.
 */
@Entity
@Table(name = "movimiento_stock", indexes = {
    @Index(name = "idx_movimiento_producto", columnList = "producto_id"),
    @Index(name = "idx_movimiento_tipo", columnList = "tipo_movimiento"),
    @Index(name = "idx_movimiento_fecha", columnList = "fecha_movimiento"),
    @Index(name = "idx_movimiento_referencia", columnList = "referencia_tipo, referencia_id"),
    @Index(name = "idx_movimiento_usuario", columnList = "usuario_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString(exclude = {"producto", "usuario"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class MovimientoStock {

    @Version
    private Long version;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false, foreignKey = @ForeignKey(name = "fk_movimiento_producto"))
    private Producto producto;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimiento", length = 20, nullable = false)
    private TipoMovimiento tipoMovimiento;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "stock_anterior", nullable = false)
    private Integer stockAnterior;

    @Column(name = "stock_nuevo", nullable = false)
    private Integer stockNuevo;

    @Column(name = "motivo", length = 100)
    private String motivo;

    @Column(name = "referencia_id")
    private Long referenciaId;

    @Column(name = "referencia_tipo", length = 30)
    private String referenciaTipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", foreignKey = @ForeignKey(name = "fk_movimiento_usuario"))
    private Usuario usuario;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @CreationTimestamp
    @Column(name = "fecha_movimiento", nullable = false, updatable = false)
    private LocalDateTime fechaMovimiento;

    /**
     * Tipos de movimientos de stock permitidos.
     */
    public enum TipoMovimiento {
        ENTRADA,      // Compra, recepción de mercancía
        SALIDA,       // Salida por consumo interno, merma
        AJUSTE,       // Ajuste de inventario (conteo físico)
        VENTA,        // Salida por venta registrada
        DEVOLUCION    // Devolución de cliente o proveedor
    }

    /**
     * Motivos comunes para movimientos (valores sugeridos para el campo motivo).
     */
    public static class Motivo {
        public static final String COMPRA = "COMPRA";
        public static final String VENTA = "VENTA";
        public static final String AJUSTE_INVENTARIO = "AJUSTE_INVENTARIO";
        public static final String MERMA = "MERMA";
        public static final String DEVOLUCION_CLIENTE = "DEVOLUCION_CLIENTE";
        public static final String DEVOLUCION_PROVEEDOR = "DEVOLUCION_PROVEEDOR";
        public static final String TRANSFERENCIA = "TRANSFERENCIA";
        public static final String OTRO = "OTRO";
    }
}