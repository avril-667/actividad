package com.inventory.system.service;

import com.inventory.system.dto.CategoriaDTO;
import com.inventory.system.entity.Categoria;
import com.inventory.system.exception.RecursoDuplicadoException;
import com.inventory.system.exception.RecursoNoEncontradoException;
import com.inventory.system.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio para gestión de categorías.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    /**
     * Obtiene todas las categorías activas ordenadas por orden de visualización.
     */
    public List<CategoriaDTO.CategoriaResponse> findAllActivas() {
        return categoriaRepository.findByActivaTrueOrderByOrdenVisualizacionAsc().stream()
            .map(CategoriaDTO.CategoriaResponse::fromEntity)
            .toList();
    }

    /**
     * Obtiene todas las categorías con paginación.
     */
    public Page<CategoriaDTO.CategoriaResponse> findAll(Pageable pageable) {
        return categoriaRepository.findAll(pageable)
            .map(CategoriaDTO.CategoriaResponse::fromEntity);
    }

    /**
     * Busca una categoría por ID.
     */
    public CategoriaDTO.CategoriaResponse findById(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Categoría", id));
        return CategoriaDTO.CategoriaResponse.fromEntity(categoria);
    }

    /**
     * Busca una categoría por código.
     */
    public CategoriaDTO.CategoriaResponse findByCodigo(String codigo) {
        Categoria categoria = categoriaRepository.findByCodigoIgnoreCase(codigo)
            .orElseThrow(() -> new RecursoNoEncontradoException("Categoría", "código: " + codigo));
        return CategoriaDTO.CategoriaResponse.fromEntity(categoria);
    }

    /**
     * Crea una nueva categoría.
     */
    @Transactional
    public CategoriaDTO.CategoriaResponse create(CategoriaDTO.CategoriaRequest request) {
        // Validar unicidad de código
        if (categoriaRepository.existsByCodigoIgnoreCaseAndIdNot(request.getCodigo(), null)) {
            throw new RecursoDuplicadoException("Categoría", "código", request.getCodigo());
        }

        Categoria categoria = Categoria.builder()
            .codigo(request.getCodigo().toUpperCase())
            .nombre(request.getNombre())
            .descripcion(request.getDescripcion())
            .icono(request.getIcono())
            .ordenVisualizacion(request.getOrdenVisualizacion() != null ? request.getOrdenVisualizacion() : 0)
            .activa(request.getActiva() != null ? request.getActiva() : true)
            .build();

        Categoria saved = categoriaRepository.save(categoria);
        return CategoriaDTO.CategoriaResponse.fromEntity(saved);
    }

    /**
     * Actualiza una categoría existente.
     */
    @Transactional
    public CategoriaDTO.CategoriaResponse update(Long id, CategoriaDTO.CategoriaRequest request) {
        Categoria categoria = categoriaRepository.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Categoría", id));

        // Validar unicidad de código (excluyendo el actual)
        if (categoriaRepository.existsByCodigoIgnoreCaseAndIdNot(request.getCodigo(), id)) {
            throw new RecursoDuplicadoException("Categoría", "código", request.getCodigo());
        }

        categoria.setCodigo(request.getCodigo().toUpperCase());
        categoria.setNombre(request.getNombre());
        categoria.setDescripcion(request.getDescripcion());
        categoria.setIcono(request.getIcono());
        if (request.getOrdenVisualizacion() != null) {
            categoria.setOrdenVisualizacion(request.getOrdenVisualizacion());
        }
        if (request.getActiva() != null) {
            categoria.setActiva(request.getActiva());
        }

        Categoria saved = categoriaRepository.save(categoria);
        return CategoriaDTO.CategoriaResponse.fromEntity(saved);
    }

    /**
     * Activa/desactiva una categoría.
     */
    @Transactional
    public CategoriaDTO.CategoriaResponse toggleActiva(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Categoría", id));

        categoria.setActiva(!categoria.getActiva());
        Categoria saved = categoriaRepository.save(categoria);
        return CategoriaDTO.CategoriaResponse.fromEntity(saved);
    }

    /**
     * Elimina una categoría.
     */
    @Transactional
    public void delete(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Categoría", id));

        categoriaRepository.delete(categoria);
    }
}