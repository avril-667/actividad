package com.inventory.system.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa una línea de detalle de venta.
 * Guarda el precio al momento de la venta para mantener integridad histórica.
 */
@Entity
@Table(name = "detalle_venta", indexes = {
    @Index(name = "idx_detalle_venta", columnList = "venta_id"),
    @Index(name = "idx_detalle_producto", columnList = "producto_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString(exclude = {"venta", "producto"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DetalleVenta {

    @Version
    private Long version;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id", nullable = false, foreignKey = @ForeignKey(name = "fk_detalle_venta"))
    private Venta venta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false, foreignKey = @ForeignKey(name = "fk_detalle_producto"))
    private Producto producto;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", precision = 12, scale = 2, nullable = false)
    private BigDecimal precioUnitario;

    @Column(name = "descuento_linea", precision = 12, scale = 2, nullable = false)
    private BigDecimal descuentoLinea = BigDecimal.ZERO;

    @Column(name = "subtotal_linea", precision = 12, scale = 2, nullable = false)
    private BigDecimal subtotalLinea;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    // =====================================================================
    // Métodos de negocio
    // =====================================================================

    /**
     * Calcula el subtotal de la línea (precio * cantidad - descuento).
     */
    public void calcularSubtotal() {
        BigDecimal subtotalBruto = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
        this.subtotalLinea = subtotalBruto.subtract(descuentoLinea);
    }

    /**
     * Verifica si el detalle tiene descuento aplicado.
     */
    public boolean tieneDescuento() {
        return descuentoLinea != null && descuentoLinea.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Obtiene el precio unitario formateado para mostrar.
     */
    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }
}