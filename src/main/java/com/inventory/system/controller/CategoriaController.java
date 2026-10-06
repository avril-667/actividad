package com.inventory.system.controller;

import com.inventory.system.dto.CategoriaDTO;
import com.inventory.system.service.CategoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de categorías.
 */
@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    /**
     * Obtiene todas las categorías activas ordenadas (para UI/tabs).
     */
    @GetMapping("/activas")
    public ResponseEntity<List<CategoriaDTO.CategoriaResponse>> getActivas() {
        return ResponseEntity.ok(categoriaService.findAllActivas());
    }

    /**
     * Obtiene todas las categorías con paginación.
     */
    @GetMapping
    public ResponseEntity<Page<CategoriaDTO.CategoriaResponse>> getAll(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(categoriaService.findAll(pageable));
    }

    /**
     * Obtiene una categoría por ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoriaDTO.CategoriaResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(categoriaService.findById(id));
    }

    /**
     * Obtiene una categoría por código.
     */
    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<CategoriaDTO.CategoriaResponse> getByCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(categoriaService.findByCodigo(codigo));
    }

    /**
     * Crea una nueva categoría.
     */
    @PostMapping
    public ResponseEntity<CategoriaDTO.CategoriaResponse> create(
            @Valid @RequestBody CategoriaDTO.CategoriaRequest request) {
        return ResponseEntity.ok(categoriaService.create(request));
    }

    /**
     * Actualiza una categoría existente.
     */
    @PutMapping("/{id}")
    public ResponseEntity<CategoriaDTO.CategoriaResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CategoriaDTO.CategoriaRequest request) {
        return ResponseEntity.ok(categoriaService.update(id, request));
    }

    /**
     * Activa/desactiva una categoría.
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<CategoriaDTO.CategoriaResponse> toggleActiva(@PathVariable Long id) {
        return ResponseEntity.ok(categoriaService.toggleActiva(id));
    }

    /**
     * Elimina una categoría.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoriaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}