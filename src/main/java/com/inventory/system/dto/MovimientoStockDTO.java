package com.inventory.system.dto;

import com.inventory.system.entity.MovimientoStock;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTOs para gestión de movimientos de stock.
 */
public class MovimientoStockDTO {

    /**
     * DTO para registrar un movimiento de stock.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    public static class MovimientoStockRequest {

        @NotNull(message = "El producto es obligatorio")
        private Long productoId;

        @NotNull(message = "El tipo de movimiento es obligatorio")
        private MovimientoStock.TipoMovimiento tipoMovimiento;

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad debe ser mayor a 0")
        private Integer cantidad;

        @Size(max = 100, message = "El motivo no puede exceder 100 caracteres")
        private String motivo;

        private Long referenciaId;

        @Size(max = 30, message = "El tipo de referencia no puede exceder 30 caracteres")
        private String referenciaTipo;

        private Long usuarioId;

        @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
        private String observaciones;
    }

    /**
     * DTO de respuesta para movimiento de stock.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    public static class MovimientoStockResponse {

        private Long id;
        private Long productoId;
        private String productoNombre;
        private String productoCodigoInterno;
        private MovimientoStock.TipoMovimiento tipoMovimiento;
        private Integer cantidad;
        private Integer stockAnterior;
        private Integer stockNuevo;
        private String motivo;
        private Long referenciaId;
        private String referenciaTipo;
        private Long usuarioId;
        private String usuarioNombre;
        private String observaciones;
        private LocalDateTime fechaMovimiento;
    }
}