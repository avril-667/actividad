package com.inventory.system.controller;

import com.inventory.system.dto.DashboardDTO;
import com.inventory.system.dto.VentaDTO;
import com.inventory.system.service.VentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador REST para terminal de ventas (POS) y reportes.
 */
@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
public class VentaController {

    private final VentaService ventaService;

    // =====================================================================
    // Terminal de Ventas (POS)
    // =====================================================================

    /**
     * Procesa una venta completa (transacción atómica).
     * - Valida stock
     * - Crea venta y detalles
     * - Descuenta stock
     * - Registra movimientos
     * - Calcula cambio
     */
    @PostMapping
    public ResponseEntity<VentaDTO.VentaResponse> procesarVenta(
            @Valid @RequestBody VentaDTO.VentaRequest request) {
        return ResponseEntity.ok(ventaService.procesarVenta(request));
    }

    /**
     * Obtiene una venta por ID con sus detalles.
     */
    @GetMapping("/{id}")
    public ResponseEntity<VentaDTO.VentaResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ventaService.findById(id));
    }

    /**
     * Obtiene una venta por número de venta.
     */
    @GetMapping("/numero/{numeroVenta}")
    public ResponseEntity<VentaDTO.VentaResponse> getByNumeroVenta(@PathVariable String numeroVenta) {
        return ResponseEntity.ok(ventaService.findByNumeroVenta(numeroVenta));
    }

    /**
     * Obtiene ventas por especialidad y rango de fechas con paginación.
     */
    @GetMapping("/especialidad/{especialidadId}")
    public ResponseEntity<Page<VentaDTO.VentaResponse>> getByEspecialidadAndFecha(
            @PathVariable Long especialidadId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin,
            @PageableDefault(size = 20) Pageable pageable) {

        LocalDateTime start = inicio != null ? inicio : LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime end = fin != null ? fin : LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

        return ResponseEntity.ok(ventaService.findByEspecialidadAndFechaBetween(especialidadId, start, end, pageable));
    }

    /**
     * Anula una venta (restaura stock).
     */
    @PostMapping("/{id}/anular")
    public ResponseEntity<VentaDTO.VentaResponse> anularVenta(
            @PathVariable Long id,
            @RequestParam Long usuarioId,
            @RequestParam(required = false) String motivo) {
        return ResponseEntity.ok(ventaService.anularVenta(id, usuarioId, motivo));
    }

    // =====================================================================
    // Reportes y Dashboard
    // =====================================================================

    /**
     * Obtiene KPIs de ventas para dashboard.
     */
    @GetMapping("/especialidad/{especialidadId}/kpis")
    public ResponseEntity<DashboardDTO.DashboardKPIsResponse> getKPIs(
            @PathVariable Long especialidadId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {

        LocalDateTime start = inicio != null ? inicio : LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime end = fin != null ? fin : LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

        return ResponseEntity.ok(ventaService.getKPIsVentas(especialidadId, start, end));
    }

    /**
     * Obtiene resumen diario de ventas.
     */
    @GetMapping("/especialidad/{especialidadId}/resumen-diario")
    public ResponseEntity<List<DashboardDTO.ResumenVentaDiarioResponse>> getResumenDiario(
            @PathVariable Long especialidadId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        return ResponseEntity.ok(ventaService.getResumenDiario(especialidadId, inicio, fin));
    }

    /**
     * Obtiene productos más vendidos.
     */
    @GetMapping("/especialidad/{especialidadId}/productos-mas-vendidos")
    public ResponseEntity<List<DashboardDTO.ProductoMasVendidoResponse>> getProductosMasVendidos(
            @PathVariable Long especialidadId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        return ResponseEntity.ok(ventaService.getProductosMasVendidos(especialidadId, inicio, fin));
    }
}