package com.inventory.system.dto;

import com.inventory.system.entity.Venta;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTOs para gestión de ventas (POS/Terminal).
 */
public class VentaDTO {

    /**
     * DTO para crear una venta (POS/Terminal).
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    public static class VentaRequest {

        private String clienteNombre;

        @Size(max = 30, message = "El documento del cliente no puede exceder 30 caracteres")
        private String clienteDocumento;

        @Email(message = "El email del cliente debe ser válido")
        @Size(max = 150, message = "El email no puede exceder 150 caracteres")
        private String clienteEmail;

        @NotNull(message = "La especialidad es obligatoria")
        private Long especialidadId;

        @NotNull(message = "El usuario (vendedor) es obligatorio")
        private Long usuarioId;

        @NotEmpty(message = "La venta debe tener al menos un detalle")
        private List<DetalleVentaRequest> detalles = new ArrayList<>();

        @DecimalMin(value = "0.0", inclusive = true, message = "El descuento no puede ser negativo")
        @Digits(integer = 10, fraction = 2, message = "El descuento debe tener máximo 10 dígitos enteros y 2 decimales")
        private BigDecimal descuentoGlobal;

        @NotNull(message = "El método de pago es obligatorio")
        private Venta.MetodoPago metodoPago;

        @NotNull(message = "El monto recibido es obligatorio para pago en efectivo")
        @DecimalMin(value = "0.0", inclusive = true, message = "El monto recibido no puede ser negativo")
        @Digits(integer = 10, fraction = 2, message = "El monto recibido debe tener máximo 10 dígitos enteros y 2 decimales")
        private BigDecimal montoRecibido;

        @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
        private String observaciones;
    }

    /**
     * DTO para cada línea de detalle de venta.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    public static class DetalleVentaRequest {

        @NotNull(message = "El producto es obligatorio")
        private Long productoId;

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad debe ser mayor a 0")
        private Integer cantidad;

        @DecimalMin(value = "0.0", inclusive = true, message = "El descuento no puede ser negativo")
        @Digits(integer = 10, fraction = 2, message = "El descuento debe tener máximo 10 dígitos enteros y 2 decimales")
        private BigDecimal descuentoLinea;
    }

    /**
     * DTO de respuesta para venta.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    public static class VentaResponse {

        private Long id;
        private String numeroVenta;
        private java.time.LocalDateTime fechaVenta;
        private String clienteNombre;
        private String clienteDocumento;
        private String clienteEmail;
        private BigDecimal subtotal;
        private BigDecimal descuento;
        private BigDecimal impuesto;
        private BigDecimal total;
        private Venta.MetodoPago metodoPago;
        private BigDecimal montoRecibido;
        private BigDecimal cambio;
        private Venta.EstadoVenta estado;
        private Long usuarioId;
        private String usuarioNombre;
        private Long especialidadId;
        private String especialidadNombre;
        private String observaciones;
        private List<DetalleVentaResponse> detalles;
        private java.time.LocalDateTime fechaCreacion;
        private java.time.LocalDateTime fechaActualizacion;
    }

    /**
     * DTO de respuesta para detalle de venta.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    public static class DetalleVentaResponse {

        private Long id;
        private Long productoId;
        private String productoNombre;
        private String productoCodigoInterno;
        private Integer cantidad;
        private java.math.BigDecimal precioUnitario;
        private java.math.BigDecimal descuentoLinea;
        private java.math.BigDecimal subtotalLinea;
    }
}