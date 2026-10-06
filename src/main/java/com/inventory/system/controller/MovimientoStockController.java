package com.inventory.system.controller;

import com.inventory.system.dto.MovimientoStockDTO;
import com.inventory.system.entity.MovimientoStock;
import com.inventory.system.service.MovimientoStockService;
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
 * Controlador REST para historial y gestión de movimientos de stock.
 */
@RestController
@RequestMapping("/api/stock/movimientos")
@RequiredArgsConstructor
public class MovimientoStockController {

    private final MovimientoStockService movimientoService;

    /**
     * Registra un movimiento manual de stock (entrada, salida, ajuste, devolución).
     * Para ventas, usar el endpoint de ventas que registra automáticamente.
     */
    @PostMapping
    public ResponseEntity<MovimientoStockDTO.MovimientoStockResponse> registrar(
            @Valid @RequestBody MovimientoStockDTO.MovimientoStockRequest request) {
        return ResponseEntity.ok(movimientoService.registrarMovimiento(request));
    }

    /**
     * Obtiene historial de movimientos de un producto.
     */
    @GetMapping("/producto/{productoId}")
    public ResponseEntity<Page<MovimientoStockDTO.MovimientoStockResponse>> getHistorialByProducto(
            @PathVariable Long productoId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(movimientoService.getHistorialByProducto(productoId, pageable));
    }

    /**
     * Obtiene movimientos por tipo en un rango de fechas.
     */
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<MovimientoStockDTO.MovimientoStockResponse>> getByTipoAndFecha(
            @PathVariable MovimientoStock.TipoMovimiento tipo,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        return ResponseEntity.ok(movimientoService.getByTipoAndFechaBetween(tipo, inicio, fin));
    }

    /**
     * Obtiene movimientos por referencia (ej. ID de venta).
     */
    @GetMapping("/referencia")
    public ResponseEntity<List<MovimientoStockDTO.MovimientoStockResponse>> getByReferencia(
            @RequestParam String tipo,
            @RequestParam Long id) {
        return ResponseEntity.ok(movimientoService.getByReferencia(tipo, id));
    }

    /**
     * Obtiene resumen de movimientos por producto en un período.
     */
    @GetMapping("/producto/{productoId}/resumen")
    public ResponseEntity<List<Object[]>> getResumenByProducto(
            @PathVariable Long productoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        return ResponseEntity.ok(movimientoService.getResumenByProducto(productoId, inicio, fin));
    }
}