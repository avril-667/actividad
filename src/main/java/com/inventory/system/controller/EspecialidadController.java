package com.inventory.system.controller;

import com.inventory.system.dto.EspecialidadDTO;
import com.inventory.system.service.EspecialidadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de especialidades.
 */
@RestController
@RequestMapping("/api/especialidades")
@RequiredArgsConstructor
public class EspecialidadController {

    private final EspecialidadService especialidadService;

    /**
     * Obtiene todas las especialidades activas (para selects/combos).
     */
    @GetMapping("/activas")
    public ResponseEntity<List<EspecialidadDTO.EspecialidadResponse>> getActivas() {
        return ResponseEntity.ok(especialidadService.findAllActivas());
    }

    /**
     * Obtiene todas las especialidades con paginación.
     */
    @GetMapping
    public ResponseEntity<Page<EspecialidadDTO.EspecialidadResponse>> getAll(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(especialidadService.findAll(pageable));
    }

    /**
     * Obtiene una especialidad por ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<EspecialidadDTO.EspecialidadResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(especialidadService.findById(id));
    }

    /**
     * Obtiene una especialidad por código.
     */
    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<EspecialidadDTO.EspecialidadResponse> getByCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(especialidadService.findByCodigo(codigo));
    }

    /**
     * Crea una nueva especialidad.
     */
    @PostMapping
    public ResponseEntity<EspecialidadDTO.EspecialidadResponse> create(
            @Valid @RequestBody EspecialidadDTO.EspecialidadRequest request) {
        return ResponseEntity.ok(especialidadService.create(request));
    }

    /**
     * Actualiza una especialidad existente.
     */
    @PutMapping("/{id}")
    public ResponseEntity<EspecialidadDTO.EspecialidadResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody EspecialidadDTO.EspecialidadRequest request) {
        return ResponseEntity.ok(especialidadService.update(id, request));
    }

    /**
     * Activa/desactiva una especialidad.
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<EspecialidadDTO.EspecialidadResponse> toggleActiva(@PathVariable Long id) {
        return ResponseEntity.ok(especialidadService.toggleActiva(id));
    }

    /**
     * Elimina una especialidad.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        especialidadService.delete(id);
        return ResponseEntity.noContent().build();
    }
}