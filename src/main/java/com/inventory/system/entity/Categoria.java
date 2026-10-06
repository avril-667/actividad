package com.inventory.system.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad que representa una categoría de productos.
 * Ejemplos: Llaveros, Tazas, Playeras, Stickers, Cartucheras, Bolsas.
 */
@Entity
@Table(name = "categoria", indexes = {
    @Index(name = "idx_categoria_codigo", columnList = "codigo", unique = true),
    @Index(name = "idx_categoria_activa", columnList = "activa"),
    @Index(name = "idx_categoria_orden", columnList = "orden_visualizacion")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Categoria {

    @Version
    private Long version;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "codigo", length = 20, nullable = false, unique = true)
    private String codigo;

    @Column(name = "nombre", length = 100, nullable = false)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "icono", length = 50)
    private String icono;

    @Column(name = "orden_visualizacion", nullable = false)
    private Integer ordenVisualizacion = 0;

    @Column(name = "activa", nullable = false)
    private Boolean activa = true;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;
}