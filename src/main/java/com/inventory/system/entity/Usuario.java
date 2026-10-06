package com.inventory.system.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad que representa un usuario del sistema (Docente o Alumno).
 * Cada usuario pertenece a una especialidad.
 */
@Entity
@Table(name = "usuario", indexes = {
    @Index(name = "idx_usuario_email", columnList = "email", unique = true),
    @Index(name = "idx_usuario_especialidad", columnList = "especialidad_id"),
    @Index(name = "idx_usuario_rol", columnList = "rol"),
    @Index(name = "idx_usuario_activo", columnList = "activo")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString(exclude = {"passwordHash", "especialidad"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Usuario {

    @Version
    private Long version;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "email", length = 150, nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", length = 255, nullable = false)
    private String passwordHash;

    @Column(name = "nombre_completo", length = 200, nullable = false)
    private String nombreCompleto;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", length = 20, nullable = false)
    private RolUsuario rol;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "especialidad_id", foreignKey = @ForeignKey(name = "fk_usuario_especialidad"))
    private Especialidad especialidad;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "ultimo_login")
    private LocalDateTime ultimoLogin;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    /**
     * Roles de usuario en el sistema.
     */
    public enum RolUsuario {
        DOCENTE,
        ALUMNO
    }

    /**
     * Verifica si el usuario es un docente.
     */
    public boolean esDocente() {
        return RolUsuario.DOCENTE.equals(this.rol);
    }

    /**
     * Verifica si el usuario es un alumno.
     */
    public boolean esAlumno() {
        return RolUsuario.ALUMNO.equals(this.rol);
    }
}