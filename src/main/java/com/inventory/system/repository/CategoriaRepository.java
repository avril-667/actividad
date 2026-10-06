package com.inventory.system.repository;

import com.inventory.system.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Categoria.
 */
@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    /**
     * Busca una categoría por su código único.
     */
    Optional<Categoria> findByCodigo(String codigo);

    /**
     * Busca categorías activas ordenadas por orden de visualización.
     */
    List<Categoria> findByActivaTrueOrderByOrdenVisualizacionAsc();

    /**
     * Busca categoría por código ignorando mayúsculas/minúsculas.
     */
    @Query("SELECT c FROM Categoria c WHERE UPPER(c.codigo) = UPPER(:codigo)")
    Optional<Categoria> findByCodigoIgnoreCase(@Param("codigo") String codigo);

    /**
     * Verifica si existe una categoría con el código dado (excluyendo un ID opcional).
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Categoria c WHERE UPPER(c.codigo) = UPPER(:codigo) AND (:idExcluir IS NULL OR c.id <> :idExcluir)")
    boolean existsByCodigoIgnoreCaseAndIdNot(@Param("codigo") String codigo, @Param("idExcluir") Long idExcluir);
}