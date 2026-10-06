package com.inventory.system.dto;

import com.inventory.system.entity.Especialidad;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTOs para gestión de especialidades.
 */
public class EspecialidadDTO {

    /**
     * DTO para crear/actualizar una especialidad.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    public static class EspecialidadRequest {

        @NotBlank(message = "El código es obligatorio")
        @Size(max = 20, message = "El código no puede exceder 20 caracteres")
        @Pattern(regexp = "^[A-Z0-9_]+$", message = "El código solo puede contener letras mayúsculas, números y guión bajo")
        private String codigo;

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
        private String nombre;

        @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
        private String descripcion;

        private Boolean activa;
    }

    /**
     * DTO de respuesta para especialidad.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    public static class EspecialidadResponse {

        private Long id;
        private String codigo;
        private String nombre;
        private String descripcion;
        private Boolean activa;
        private java.time.LocalDateTime fechaCreacion;
        private java.time.LocalDateTime fechaActualizacion;

        public static EspecialidadResponse fromEntity(Especialidad especialidad) {
            return EspecialidadResponse.builder()
                .id(especialidad.getId())
                .codigo(especialidad.getCodigo())
                .nombre(especialidad.getNombre())
                .descripcion(especialidad.getDescripcion())
                .activa(especialidad.getActiva())
                .fechaCreacion(especialidad.getFechaCreacion())
                .fechaActualizacion(especialidad.getFechaActualizacion())
                .build();
        }
    }
}