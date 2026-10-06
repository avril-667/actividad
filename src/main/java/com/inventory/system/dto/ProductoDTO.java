package com.inventory.system.dto;

import com.inventory.system.entity.Producto;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTOs para gestión de productos.
 */
public class ProductoDTO {

    /**
     * DTO para crear/actualizar un producto.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    public static class ProductoRequest {

        @Size(max = 50, message = "El código de barras no puede exceder 50 caracteres")
        private String codigoBarras;

        @NotBlank(message = "El código interno es obligatorio")
        @Size(max = 30, message = "El código interno no puede exceder 30 caracteres")
        private String codigoInterno;

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
        private String nombre;

        @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
        private String descripcion;

        @NotNull(message = "La categoría es obligatoria")
        private Long categoriaId;

        @NotNull(message = "La especialidad es obligatoria")
        private Long especialidadId;

        @NotNull(message = "El precio de venta es obligatorio")
        @DecimalMin(value = "0.0", inclusive = true, message = "El precio debe ser mayor o igual a 0")
        @Digits(integer = 10, fraction = 2, message = "El precio debe tener máximo 10 dígitos enteros y 2 decimales")
        private BigDecimal precioVenta;

        @Size(max = 20, message = "La unidad de medida no puede exceder 20 caracteres")
        private String unidadMedida;

        @Min(value = 0, message = "El stock inicial no puede ser negativo")
        private Integer stockActual;

        @Min(value = 0, message = "El stock mínimo no puede ser negativo")
        private Integer stockMinimo;

        private String imagenUrl;

        private Boolean activo;
    }

    /**
     * DTO de respuesta para producto (incluye estado de stock calculado).
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    public static class ProductoResponse {

        private Long id;
        private String codigoBarras;
        private String codigoInterno;
        private String nombre;
        private String descripcion;
        private Long categoriaId;
        private String categoriaNombre;
        private String categoriaIcono;
        private Long especialidadId;
        private String especialidadNombre;
        private BigDecimal precioVenta;
        private String unidadMedida;
        private Integer stockActual;
        private Integer stockMinimo;
        private String imagenUrl;
        private Boolean activo;
        private Producto.EstadoStock estadoStock;
        private LocalDateTime fechaCreacion;
        private LocalDateTime fechaActualizacion;

        /**
         * Crea un ProductoResponse desde una entidad Producto.
         */
        public static ProductoResponse fromEntity(Producto producto) {
            return ProductoResponse.builder()
                .id(producto.getId())
                .codigoBarras(producto.getCodigoBarras())
                .codigoInterno(producto.getCodigoInterno())
                .nombre(producto.getNombre())
                .descripcion(producto.getDescripcion())
                .categoriaId(producto.getCategoria() != null ? producto.getCategoria().getId() : null)
                .categoriaNombre(producto.getCategoria() != null ? producto.getCategoria().getNombre() : null)
                .categoriaIcono(producto.getCategoria() != null ? producto.getCategoria().getIcono() : null)
                .especialidadId(producto.getEspecialidad() != null ? producto.getEspecialidad().getId() : null)
                .especialidadNombre(producto.getEspecialidad() != null ? producto.getEspecialidad().getNombre() : null)
                .precioVenta(producto.getPrecioVenta())
                .unidadMedida(producto.getUnidadMedida())
                .stockActual(producto.getStockActual())
                .stockMinimo(producto.getStockMinimo())
                .imagenUrl(producto.getImagenUrl())
                .activo(producto.getActivo())
                .estadoStock(producto.getEstadoStock())
                .fechaCreacion(producto.getFechaCreacion())
                .fechaActualizacion(producto.getFechaActualizacion())
                .build();
        }
    }
}