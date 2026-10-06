package com.inventory.system.repository;

import com.inventory.system.entity.Especialidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Especialidad.
 */
@Repository
public interface EspecialidadRepository extends JpaRepository<Especialidad, Long> {

    /**
     * Busca una especialidad por su código único.
     */
    Optional<Especialidad> findByCodigo(String codigo);

    /**
     * Busca especialidades activas.
     */
    List<Especialidad> findByActivaTrue();

    /**
     * Busca especialidad por código ignorando mayúsculas/minúsculas.
     */
    @Query("SELECT e FROM Especialidad e WHERE UPPER(e.codigo) = UPPER(:codigo)")
    Optional<Especialidad> findByCodigoIgnoreCase(@Param("codigo") String codigo);

    /**
     * Verifica si existe una especialidad con el código dado (excluyendo un ID opcional).
     */
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Especialidad e WHERE UPPER(e.codigo) = UPPER(:codigo) AND (:idExcluir IS NULL OR e.id <> :idExcluir)")
    boolean existsByCodigoIgnoreCaseAndIdNot(@Param("codigo") String codigo, @Param("idExcluir") Long idExcluir);
}