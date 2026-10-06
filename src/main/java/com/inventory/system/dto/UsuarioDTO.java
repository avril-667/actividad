package com.inventory.system.dto;

import com.inventory.system.entity.Usuario;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTOs para gestión de usuarios.
 */
public class UsuarioDTO {

    /**
     * DTO para crear/actualizar un usuario.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    public static class UsuarioRequest {

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email debe ser válido")
        @Size(max = 150, message = "El email no puede exceder 150 caracteres")
        private String email;

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, max = 100, message = "La contraseña debe tener entre 6 y 100 caracteres")
        private String password;

        @NotBlank(message = "El nombre completo es obligatorio")
        @Size(max = 200, message = "El nombre completo no puede exceder 200 caracteres")
        private String nombreCompleto;

        @NotNull(message = "El rol es obligatorio")
        private Usuario.RolUsuario rol;

        private Long especialidadId;

        private Boolean activo;
    }

    /**
     * DTO de respuesta para usuario.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    public static class UsuarioResponse {

        private Long id;
        private String email;
        private String nombreCompleto;
        private Usuario.RolUsuario rol;
        private Long especialidadId;
        private String especialidadNombre;
        private String especialidadCodigo;
        private Boolean activo;
        private LocalDateTime ultimoLogin;
        private LocalDateTime fechaCreacion;
        private LocalDateTime fechaActualizacion;

        public static UsuarioResponse fromEntity(Usuario usuario) {
            return UsuarioResponse.builder()
                .id(usuario.getId())
                .email(usuario.getEmail())
                .nombreCompleto(usuario.getNombreCompleto())
                .rol(usuario.getRol())
                .especialidadId(usuario.getEspecialidad() != null ? usuario.getEspecialidad().getId() : null)
                .especialidadNombre(usuario.getEspecialidad() != null ? usuario.getEspecialidad().getNombre() : null)
                .especialidadCodigo(usuario.getEspecialidad() != null ? usuario.getEspecialidad().getCodigo() : null)
                .activo(usuario.getActivo())
                .ultimoLogin(usuario.getUltimoLogin())
                .fechaCreacion(usuario.getFechaCreacion())
                .fechaActualizacion(usuario.getFechaActualizacion())
                .build();
        }
    }
}