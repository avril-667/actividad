package com.inventory.system.controller;

import com.inventory.system.dto.UsuarioDTO;
import com.inventory.system.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de usuarios (Docentes/Alumnos).
 */
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    /**
     * Obtiene usuarios activos por especialidad y rol.
     */
    @GetMapping("/especialidad/{especialidadId}/rol/{rol}")
    public ResponseEntity<List<UsuarioDTO.UsuarioResponse>> getByEspecialidadAndRol(
            @PathVariable Long especialidadId,
            @PathVariable String rol) {
        return ResponseEntity.ok(usuarioService.findByEspecialidadAndRol(especialidadId, rol));
    }

    /**
     * Obtiene todos los usuarios con paginación.
     */
    @GetMapping
    public ResponseEntity<Page<UsuarioDTO.UsuarioResponse>> getAll(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(usuarioService.findAll(pageable));
    }

    /**
     * Obtiene un usuario por ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO.UsuarioResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.findById(id));
    }

    /**
     * Crea un nuevo usuario.
     */
    @PostMapping
    public ResponseEntity<UsuarioDTO.UsuarioResponse> create(
            @Valid @RequestBody UsuarioDTO.UsuarioRequest request) {
        return ResponseEntity.ok(usuarioService.create(request));
    }

    /**
     * Actualiza un usuario existente.
     */
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDTO.UsuarioResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioDTO.UsuarioRequest request) {
        return ResponseEntity.ok(usuarioService.update(id, request));
    }

    /**
     * Activa/desactiva un usuario.
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<UsuarioDTO.UsuarioResponse> toggleActivo(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.toggleActivo(id));
    }

    /**
     * Elimina un usuario.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        usuarioService.delete(id);
        return ResponseEntity.noContent().build();
    }
}