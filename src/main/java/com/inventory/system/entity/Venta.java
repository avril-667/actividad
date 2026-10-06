package com.inventory.system.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa una venta (cabecera de transacción).
 * Incluye información del cliente, totales, método de pago y estado.
 */
@Entity
@Table(name = "venta", indexes = {
    @Index(name = "idx_venta_numero", columnList = "numero_venta", unique = true),
    @Index(name = "idx_venta_fecha", columnList = "fecha_venta"),
    @Index(name = "idx_venta_usuario", columnList = "usuario_id"),
    @Index(name = "idx_venta_especialidad", columnList = "especialidad_id"),
    @Index(name = "idx_venta_estado", columnList = "estado"),
    @Index(name = "idx_venta_cliente", columnList = "cliente_nombre")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString(exclude = {"detalles", "usuario", "especialidad"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Venta {

    @Version
    private Long version;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "numero_venta", length = 30, nullable = false, unique = true)
    private String numeroVenta;

    @Column(name = "fecha_venta", nullable = false)
    private LocalDateTime fechaVenta;

    @Column(name = "cliente_nombre", length = 200)
    private String clienteNombre;

    @Column(name = "cliente_documento", length = 30)
    private String clienteDocumento;

    @Column(name = "cliente_email", length = 150)
    private String clienteEmail;

    @Column(name = "subtotal", precision = 12, scale = 2, nullable = false)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "descuento", precision = 12, scale = 2, nullable = false)
    private BigDecimal descuento = BigDecimal.ZERO;

    @Column(name = "impuesto", precision = 12, scale = 2, nullable = false)
    private BigDecimal impuesto = BigDecimal.ZERO;

    @Column(name = "total", precision = 12, scale = 2, nullable = false)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", length = 20, nullable = false)
    private MetodoPago metodoPago = MetodoPago.EFECTIVO;

    @Column(name = "monto_recibido", precision = 12, scale = 2)
    private BigDecimal montoRecibido;

    @Column(name = "cambio", precision = 12, scale = 2, nullable = false)
    private BigDecimal cambio = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20, nullable = false)
    private EstadoVenta estado = EstadoVenta.COMPLETADA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, foreignKey = @ForeignKey(name = "fk_venta_usuario"))
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "especialidad_id", nullable = false, foreignKey = @ForeignKey(name = "fk_venta_especialidad"))
    private Especialidad especialidad;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DetalleVenta> detalles = new ArrayList<>();

    /**
     * Métodos de pago permitidos (actualmente solo Efectivo según especificación).
     */
    public enum MetodoPago {
        EFECTIVO
    }

    /**
     * Estados posibles de una venta.
     */
    public enum EstadoVenta {
        PENDIENTE,      // Venta iniciada pero no completada
        COMPLETADA,     // Venta finalizada y stock descontado
        ANULADA,        // Venta cancelada (stock restituido)
        DEVUELTA        // Venta con devolución total o parcial
    }

    // =====================================================================
    // Métodos de negocio
    // =====================================================================

    /**
     * Agrega un detalle a la venta y recalcula totales.
     */
    public void agregarDetalle(DetalleVenta detalle) {
        detalles.add(detalle);
        detalle.setVenta(this);
        recalcularTotales();
    }

    /**
     * Remueve un detalle de la venta y recalcula totales.
     */
    public void removerDetalle(DetalleVenta detalle) {
        detalles.remove(detalle);
        detalle.setVenta(null);
        recalcularTotales();
    }

    /**
     * Recalcula subtotal, descuento, impuesto y total basándose en los detalles.
     */
    public void recalcularTotales() {
        this.subtotal = detalles.stream()
            .map(DetalleVenta::getSubtotalLinea)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.descuento = detalles.stream()
            .map(DetalleVenta::getDescuentoLinea)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Por ahora impuesto en 0, se puede extender para IGV/IVA
        this.impuesto = BigDecimal.ZERO;

        this.total = this.subtotal.subtract(this.descuento).add(this.impuesto);
    }

    /**
     * Calcula el cambio basado en el monto recibido y el total.
     */
    public void calcularCambio() {
        if (montoRecibido != null && montoRecibido.compareTo(total) >= 0) {
            this.cambio = montoRecibido.subtract(total);
        } else {
            this.cambio = BigDecimal.ZERO;
        }
    }

    /**
     * Verifica si la venta está en un estado que permite modificaciones.
     */
    public boolean esModificable() {
        return EstadoVenta.PENDIENTE.equals(this.estado);
    }

    /**
     * Marca la venta como completada.
     */
    public void completar() {
        this.estado = EstadoVenta.COMPLETADA;
    }

    /**
     * Anula la venta (requiere restitución de stock).
     */
    public void anular() {
        this.estado = EstadoVenta.ANULADA;
    }
}