package com.inventory.system.repository;

import com.inventory.system.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Usuario.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por email (único).
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Busca usuarios por especialidad.
     */
    List<Usuario> findByEspecialidadId(Long especialidadId);

    /**
     * Busca usuarios por rol.
     */
    List<Usuario> findByRol(Usuario.RolUsuario rol);

    /**
     * Busca usuarios activos por especialidad y rol.
     */
    List<Usuario> findByEspecialidadIdAndRolAndActivoTrue(Long especialidadId, Usuario.RolUsuario rol);

    /**
     * Busca usuario por email ignorando mayúsculas/minúsculas.
     */
    @Query("SELECT u FROM Usuario u WHERE UPPER(u.email) = UPPER(:email)")
    Optional<Usuario> findByEmailIgnoreCase(@Param("email") String email);

    /**
     * Verifica si existe un usuario con el email dado (excluyendo un ID opcional).
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM Usuario u WHERE UPPER(u.email) = UPPER(:email) AND (:idExcluir IS NULL OR u.id <> :idExcluir)")
    boolean existsByEmailIgnoreCaseAndIdNot(@Param("email") String email, @Param("idExcluir") Long idExcluir);

    /**
     * Actualiza la fecha de último login.
     */
    @Query("UPDATE Usuario u SET u.ultimoLogin = CURRENT_TIMESTAMP WHERE u.id = :id")
    void updateUltimoLogin(@Param("id") Long id);

    /**
     * Busca usuarios activos por especialidad.
     */
    @Query("SELECT u FROM Usuario u WHERE u.especialidad.id = :especialidadId AND u.activo = true")
    List<Usuario> findActivosByEspecialidadId(@Param("especialidadId") Long especialidadId);
}