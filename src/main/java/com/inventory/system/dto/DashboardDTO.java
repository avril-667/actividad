package com.inventory.system.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * DTOs para dashboard y reportes.
 */
public class DashboardDTO {

    /**
     * DTO para KPIs del dashboard (métricas rápidas).
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    public static class DashboardKPIsResponse {

        private Long productosRegistrados;
        private Long stockBajo;
        private Long agotados;
        private Long totalEspecialidades;
        private BigDecimal ingresoHoy;
        private Long ventasHoy;
        private BigDecimal ticketPromedioHoy;

        private Long productosRegistradosMes;
        private Long ventasMes;
        private BigDecimal ingresoMes;
    }

    /**
     * DTO para resumen de ventas diario.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    public static class ResumenVentaDiarioResponse {

        private java.time.LocalDate fecha;
        private Long especialidadId;
        private String especialidadNombre;
        private Long totalVentas;
        private BigDecimal ingresoTotal;
        private BigDecimal subtotalTotal;
        private BigDecimal descuentoTotal;
        private BigDecimal ticketPromedio;
    }

    /**
     * DTO para producto más vendido.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    public static class ProductoMasVendidoResponse {

        private Long productoId;
        private String productoNombre;
        private String productoCodigoInterno;
        private Long totalVendido;
        private BigDecimal ingresoTotal;
    }
}