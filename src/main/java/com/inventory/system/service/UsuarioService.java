package com.inventory.system.service;

import com.inventory.system.dto.UsuarioDTO;
import com.inventory.system.entity.Especialidad;
import com.inventory.system.entity.Usuario;
import com.inventory.system.exception.RecursoDuplicadoException;
import com.inventory.system.exception.RecursoNoEncontradoException;
import com.inventory.system.exception.ValidacionException;
import com.inventory.system.repository.EspecialidadRepository;
import com.inventory.system.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio para gestión de usuarios.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EspecialidadRepository especialidadRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Obtiene usuarios activos por especialidad y rol.
     */
    public List<UsuarioDTO.UsuarioResponse> findByEspecialidadAndRol(Long especialidadId, String rol) {
        Usuario.RolUsuario rolEnum;
        try {
            rolEnum = Usuario.RolUsuario.valueOf(rol.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidacionException(List.of("Rol inválido: " + rol));
        }
        return usuarioRepository.findByEspecialidadIdAndRolAndActivoTrue(especialidadId, rolEnum).stream()
            .map(UsuarioDTO.UsuarioResponse::fromEntity)
            .toList();
    }

    /**
     * Obtiene todos los usuarios con paginación.
     */
    public Page<UsuarioDTO.UsuarioResponse> findAll(Pageable pageable) {
        return usuarioRepository.findAll(pageable)
            .map(UsuarioDTO.UsuarioResponse::fromEntity);
    }

    /**
     * Busca un usuario por ID.
     */
    public UsuarioDTO.UsuarioResponse findById(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", id));
        return UsuarioDTO.UsuarioResponse.fromEntity(usuario);
    }

    /**
     * Crea un nuevo usuario.
     */
    @Transactional
    public UsuarioDTO.UsuarioResponse create(UsuarioDTO.UsuarioRequest request) {
        // Validar unicidad de email
        if (usuarioRepository.existsByEmailIgnoreCaseAndIdNot(request.getEmail(), null)) {
            throw new RecursoDuplicadoException("Usuario", "email", request.getEmail());
        }

        // Validar y obtener especialidad si se proporciona
        Especialidad especialidad = null;
        if (request.getEspecialidadId() != null) {
            especialidad = especialidadRepository.findById(request.getEspecialidadId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Especialidad", request.getEspecialidadId()));
        }

        Usuario usuario = Usuario.builder()
            .email(request.getEmail().toLowerCase())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .nombreCompleto(request.getNombreCompleto())
            .rol(request.getRol())
            .especialidad(especialidad)
            .activo(request.getActivo() != null ? request.getActivo() : true)
            .build();

        Usuario saved = usuarioRepository.save(usuario);
        return UsuarioDTO.UsuarioResponse.fromEntity(saved);
    }

    /**
     * Actualiza un usuario existente.
     */
    @Transactional
    public UsuarioDTO.UsuarioResponse update(Long id, UsuarioDTO.UsuarioRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", id));

        // Validar unicidad de email (excluyendo el actual)
        if (usuarioRepository.existsByEmailIgnoreCaseAndIdNot(request.getEmail(), id)) {
            throw new RecursoDuplicadoException("Usuario", "email", request.getEmail());
        }

        // Validar y obtener especialidad si se proporciona
        Especialidad especialidad = null;
        if (request.getEspecialidadId() != null) {
            especialidad = especialidadRepository.findById(request.getEspecialidadId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Especialidad", request.getEspecialidadId()));
        }

        usuario.setEmail(request.getEmail().toLowerCase());
        usuario.setNombreCompleto(request.getNombreCompleto());
        usuario.setRol(request.getRol());
        usuario.setEspecialidad(especialidad);
        if (request.getActivo() != null) {
            usuario.setActivo(request.getActivo());
        }

        // Solo actualizar password si se proporciona uno nuevo
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        Usuario saved = usuarioRepository.save(usuario);
        return UsuarioDTO.UsuarioResponse.fromEntity(saved);
    }

    /**
     * Activa/desactiva un usuario.
     */
    @Transactional
    public UsuarioDTO.UsuarioResponse toggleActivo(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", id));

        usuario.setActivo(!usuario.getActivo());
        Usuario saved = usuarioRepository.save(usuario);
        return UsuarioDTO.UsuarioResponse.fromEntity(saved);
    }

    /**
     * Elimina un usuario.
     */
    @Transactional
    public void delete(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", id));

        usuarioRepository.delete(usuario);
    }
}