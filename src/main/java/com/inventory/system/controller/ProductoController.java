package com.inventory.system.controller;

import com.inventory.system.dto.DashboardDTO;
import com.inventory.system.dto.ProductoDTO;
import com.inventory.system.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de productos y control de stock.
 */
@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    // =====================================================================
    // Consultas para Catálogo Visual (UI)
    // =====================================================================

    /**
     * Obtiene todos los productos activos de una especialidad para el catálogo visual.
     * Agrupados por categoría para tabs en UI.
     */
    @GetMapping("/especialidad/{especialidadId}")
    public ResponseEntity<List<ProductoDTO.ProductoResponse>> getByEspecialidad(
            @PathVariable Long especialidadId) {
        return ResponseEntity.ok(productoService.findAllByEspecialidad(especialidadId));
    }

    /**
     * Obtiene productos por categoría y especialidad.
     */
    @GetMapping("/especialidad/{especialidadId}/categoria/{categoriaId}")
    public ResponseEntity<List<ProductoDTO.ProductoResponse>> getByCategoriaAndEspecialidad(
            @PathVariable Long especialidadId,
            @PathVariable Long categoriaId) {
        return ResponseEntity.ok(productoService.findByCategoriaAndEspecialidad(categoriaId, especialidadId));
    }

    /**
     * Busca productos por nombre o código (para barra de búsqueda).
     */
    @GetMapping("/especialidad/{especialidadId}/buscar")
    public ResponseEntity<Page<ProductoDTO.ProductoResponse>> buscar(
            @PathVariable Long especialidadId,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(productoService.buscar(especialidadId, q, pageable));
    }

    /**
     * Obtiene un producto por código de barras (escáner).
     */
    @GetMapping("/codigo-barras/{codigoBarras}")
    public ResponseEntity<ProductoDTO.ProductoResponse> getByCodigoBarras(
            @PathVariable String codigoBarras) {
        return ResponseEntity.ok(productoService.findByCodigoBarras(codigoBarras));
    }

    /**
     * Obtiene un producto por código interno.
     */
    @GetMapping("/codigo-interno/{codigoInterno}")
    public ResponseEntity<ProductoDTO.ProductoResponse> getByCodigoInterno(
            @PathVariable String codigoInterno) {
        return ResponseEntity.ok(productoService.findByCodigoInterno(codigoInterno));
    }

    // =====================================================================
    // KPIs y Alertas de Stock (Dashboard)
    // =====================================================================

    /**
     * Obtiene KPIs del dashboard para una especialidad.
     */
    @GetMapping("/especialidad/{especialidadId}/kpis")
    public ResponseEntity<DashboardDTO.DashboardKPIsResponse> getKPIs(
            @PathVariable Long especialidadId) {
        return ResponseEntity.ok(productoService.getKPIs(especialidadId));
    }

    /**
     * Obtiene productos con stock bajo (alertas).
     */
    @GetMapping("/especialidad/{especialidadId}/alertas/stock-bajo")
    public ResponseEntity<List<ProductoDTO.ProductoResponse>> getLowStock(
            @PathVariable Long especialidadId) {
        return ResponseEntity.ok(productoService.getLowStock(especialidadId));
    }

    /**
     * Obtiene productos agotados (alertas).
     */
    @GetMapping("/especialidad/{especialidadId}/alertas/agotados")
    public ResponseEntity<List<ProductoDTO.ProductoResponse>> getOutOfStock(
            @PathVariable Long especialidadId) {
        return ResponseEntity.ok(productoService.getOutOfStock(especialidadId));
    }

    /**
     * Obtiene todos los productos con su estado de stock calculado.
     */
    @GetMapping("/especialidad/{especialidadId}/stock-estado")
    public ResponseEntity<List<ProductoDTO.ProductoResponse>> getAllWithStockEstado(
            @PathVariable Long especialidadId) {
        return ResponseEntity.ok(productoService.getAllWithStockEstado(especialidadId));
    }

    // =====================================================================
    // CRUD Básico
    // =====================================================================

    /**
     * Obtiene un producto por ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductoDTO.ProductoResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.findById(id));
    }

    /**
     * Crea un nuevo producto.
     */
    @PostMapping
    public ResponseEntity<ProductoDTO.ProductoResponse> create(
            @Valid @RequestBody ProductoDTO.ProductoRequest request) {
        return ResponseEntity.ok(productoService.create(request));
    }

    /**
     * Actualiza un producto existente.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductoDTO.ProductoResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductoDTO.ProductoRequest request) {
        return ResponseEntity.ok(productoService.update(id, request));
    }

    /**
     * Activa/desactiva un producto (soft delete).
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ProductoDTO.ProductoResponse> toggleActivo(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.toggleActivo(id));
    }

    /**
     * Elimina un producto.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // =====================================================================
    // Operaciones de Stock
    // =====================================================================

    /**
     * Incrementa stock (entrada de mercancía).
     */
    @PostMapping("/{id}/stock/entrada")
    public ResponseEntity<ProductoDTO.ProductoResponse> incrementarStock(
            @PathVariable Long id,
            @RequestParam int cantidad) {
        return ResponseEntity.ok(productoService.incrementarStock(id, cantidad));
    }

    /**
     * Decrementa stock (salida manual).
     */
    @PostMapping("/{id}/stock/salida")
    public ResponseEntity<ProductoDTO.ProductoResponse> decrementarStock(
            @PathVariable Long id,
            @RequestParam int cantidad) {
        return ResponseEntity.ok(productoService.decrementarStock(id, cantidad));
    }

    /**
     * Ajusta stock a valor específico (inventario físico).
     */
    @PostMapping("/{id}/stock/ajuste")
    public ResponseEntity<ProductoDTO.ProductoResponse> ajustarStock(
            @PathVariable Long id,
            @RequestParam int stock) {
        return ResponseEntity.ok(productoService.ajustarStock(id, stock));
    }

    /**
     * Actualiza stock mínimo de alerta.
     */
    @PatchMapping("/{id}/stock/minimo")
    public ResponseEntity<ProductoDTO.ProductoResponse> actualizarStockMinimo(
            @PathVariable Long id,
            @RequestParam int stockMinimo) {
        return ResponseEntity.ok(productoService.actualizarStockMinimo(id, stockMinimo));
    }
}