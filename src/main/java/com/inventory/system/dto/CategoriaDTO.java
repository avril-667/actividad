package com.inventory.system.dto;

import com.inventory.system.entity.Categoria;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTOs para gestión de categorías.
 */
public class CategoriaDTO {

    /**
     * DTO para crear/actualizar una categoría.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    public static class CategoriaRequest {

        @NotBlank(message = "El código es obligatorio")
        @Size(max = 20, message = "El código no puede exceder 20 caracteres")
        @Pattern(regexp = "^[A-Z0-9_]+$", message = "El código solo puede contener letras mayúsculas, números y guión bajo")
        private String codigo;

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
        private String nombre;

        @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
        private String descripcion;

        @Size(max = 50, message = "El icono no puede exceder 50 caracteres")
        private String icono;

        @Min(value = 0, message = "El orden de visualización no puede ser negativo")
        private Integer ordenVisualizacion;

        private Boolean activa;
    }

    /**
     * DTO de respuesta para categoría.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    public static class CategoriaResponse {

        private Long id;
        private String codigo;
        private String nombre;
        private String descripcion;
        private String icono;
        private Integer ordenVisualizacion;
        private Boolean activa;
        private java.time.LocalDateTime fechaCreacion;
        private java.time.LocalDateTime fechaActualizacion;

        public static CategoriaResponse fromEntity(Categoria categoria) {
            return CategoriaResponse.builder()
                .id(categoria.getId())
                .codigo(categoria.getCodigo())
                .nombre(categoria.getNombre())
                .descripcion(categoria.getDescripcion())
                .icono(categoria.getIcono())
                .ordenVisualizacion(categoria.getOrdenVisualizacion())
                .activa(categoria.getActiva())
                .fechaCreacion(categoria.getFechaCreacion())
                .fechaActualizacion(categoria.getFechaActualizacion())
                .build();
        }
    }
}