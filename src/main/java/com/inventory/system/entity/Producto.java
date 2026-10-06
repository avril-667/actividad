package com.inventory.system.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa un producto en el catálogo.
 * Cada producto pertenece a una categoría y a una especialidad.
 * Incluye control de stock en tiempo real y alertas automáticas.
 */
@Entity
@Table(name = "producto", indexes = {
    @Index(name = "idx_producto_codigo_barras", columnList = "codigo_barras", unique = true),
    @Index(name = "idx_producto_codigo_interno", columnList = "codigo_interno", unique = true),
    @Index(name = "idx_producto_categoria", columnList = "categoria_id"),
    @Index(name = "idx_producto_especialidad", columnList = "especialidad_id"),
    @Index(name = "idx_producto_activo", columnList = "activo"),
    @Index(name = "idx_producto_stock", columnList = "stock_actual")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString(exclude = {"categoria", "especialidad"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Producto {

    @Version
    private Long version;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "codigo_barras", length = 50, unique = true)
    private String codigoBarras;

    @Column(name = "codigo_interno", length = 30, nullable = false, unique = true)
    private String codigoInterno;

    @Column(name = "nombre", length = 200, nullable = false)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false, foreignKey = @ForeignKey(name = "fk_producto_categoria"))
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "especialidad_id", nullable = false, foreignKey = @ForeignKey(name = "fk_producto_especialidad"))
    private Especialidad especialidad;

    @Column(name = "precio_venta", precision = 12, scale = 2, nullable = false)
    private BigDecimal precioVenta;

    @Builder.Default
    @Column(name = "unidad_medida", length = 20, nullable = false)
    private String unidadMedida = "UNIDAD";

    @Builder.Default
    @Column(name = "stock_actual", nullable = false)
    private Integer stockActual = 0;

    @Builder.Default
    @Column(name = "stock_minimo", nullable = false)
    private Integer stockMinimo = 5;

    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;

    @Builder.Default
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    // =====================================================================
    // Métodos de negocio para control de stock
    // =====================================================================

    /**
     * Enum que representa el estado del stock del producto.
     */
    public enum EstadoStock {
        IN_STOCK,      // Stock suficiente
        LOW_STOCK,     // Stock bajo (por agotarse)
        OUT_OF_STOCK   // Agotado
    }

    /**
     * Calcula y retorna el estado actual del stock basado en stockActual y stockMinimo.
     */
    public EstadoStock getEstadoStock() {
        if (stockActual <= 0) {
            return EstadoStock.OUT_OF_STOCK;
        }
        if (stockActual <= stockMinimo) {
            return EstadoStock.LOW_STOCK;
        }
        return EstadoStock.IN_STOCK;
    }

    /**
     * Verifica si hay stock suficiente para la cantidad solicitada.
     */
    public boolean tieneStockSuficiente(int cantidadSolicitada) {
        return stockActual >= cantidadSolicitada;
    }

    /**
     * Incrementa el stock (entrada de mercancía).
     */
    public void incrementarStock(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a incrementar debe ser mayor a 0");
        }
        this.stockActual += cantidad;
    }

    /**
     * Decrementa el stock (salida de mercancía/venta).
     * Lanza excepción si no hay stock suficiente.
     */
    public void decrementarStock(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a decrementar debe ser mayor a 0");
        }
        if (!tieneStockSuficiente(cantidad)) {
            throw new IllegalStateException(
                String.format("Stock insuficiente. Disponible: %d, Solicitado: %d", stockActual, cantidad)
            );
        }
        this.stockActual -= cantidad;
    }

    /**
     * Ajusta el stock a un valor específico (para ajustes de inventario).
     */
    public void ajustarStock(int nuevoStock) {
        if (nuevoStock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
        this.stockActual = nuevoStock;
    }

    /**
     * Verifica si el producto está disponible para venta.
     */
    public boolean estaDisponibleParaVenta() {
        return activo && stockActual > 0;
    }
}