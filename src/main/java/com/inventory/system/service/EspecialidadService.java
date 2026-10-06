package com.inventory.system.service;

import com.inventory.system.dto.EspecialidadDTO;
import com.inventory.system.entity.Especialidad;
import com.inventory.system.exception.RecursoDuplicadoException;
import com.inventory.system.exception.RecursoNoEncontradoException;
import com.inventory.system.repository.EspecialidadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio para gestión de especialidades.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EspecialidadService {

    private final EspecialidadRepository especialidadRepository;

    /**
     * Obtiene todas las especialidades activas.
     */
    public List<EspecialidadDTO.EspecialidadResponse> findAllActivas() {
        return especialidadRepository.findByActivaTrue().stream()
            .map(EspecialidadDTO.EspecialidadResponse::fromEntity)
            .toList();
    }

    /**
     * Obtiene todas las especialidades con paginación.
     */
    public Page<EspecialidadDTO.EspecialidadResponse> findAll(Pageable pageable) {
        return especialidadRepository.findAll(pageable)
            .map(EspecialidadDTO.EspecialidadResponse::fromEntity);
    }

    /**
     * Busca una especialidad por ID.
     */
    public EspecialidadDTO.EspecialidadResponse findById(Long id) {
        Especialidad especialidad = especialidadRepository.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Especialidad", id));
        return EspecialidadDTO.EspecialidadResponse.fromEntity(especialidad);
    }

    /**
     * Busca una especialidad por código.
     */
    public EspecialidadDTO.EspecialidadResponse findByCodigo(String codigo) {
        Especialidad especialidad = especialidadRepository.findByCodigoIgnoreCase(codigo)
            .orElseThrow(() -> new RecursoNoEncontradoException("Especialidad", "código: " + codigo));
        return EspecialidadDTO.EspecialidadResponse.fromEntity(especialidad);
    }

    /**
     * Crea una nueva especialidad.
     */
    @Transactional
    public EspecialidadDTO.EspecialidadResponse create(EspecialidadDTO.EspecialidadRequest request) {
        // Validar unicidad de código
        if (especialidadRepository.existsByCodigoIgnoreCaseAndIdNot(request.getCodigo(), null)) {
            throw new RecursoDuplicadoException("Especialidad", "código", request.getCodigo());
        }

        Especialidad especialidad = Especialidad.builder()
            .codigo(request.getCodigo().toUpperCase())
            .nombre(request.getNombre())
            .descripcion(request.getDescripcion())
            .activa(request.getActiva() != null ? request.getActiva() : true)
            .build();

        Especialidad saved = especialidadRepository.save(especialidad);
        return EspecialidadDTO.EspecialidadResponse.fromEntity(saved);
    }

    /**
     * Actualiza una especialidad existente.
     */
    @Transactional
    public EspecialidadDTO.EspecialidadResponse update(Long id, EspecialidadDTO.EspecialidadRequest request) {
        Especialidad especialidad = especialidadRepository.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Especialidad", id));

        // Validar unicidad de código (excluyendo el actual)
        if (especialidadRepository.existsByCodigoIgnoreCaseAndIdNot(request.getCodigo(), id)) {
            throw new RecursoDuplicadoException("Especialidad", "código", request.getCodigo());
        }

        especialidad.setCodigo(request.getCodigo().toUpperCase());
        especialidad.setNombre(request.getNombre());
        especialidad.setDescripcion(request.getDescripcion());
        if (request.getActiva() != null) {
            especialidad.setActiva(request.getActiva());
        }

        Especialidad saved = especialidadRepository.save(especialidad);
        return EspecialidadDTO.EspecialidadResponse.fromEntity(saved);
    }

    /**
     * Activa/desactiva una especialidad (soft delete).
     */
    @Transactional
    public EspecialidadDTO.EspecialidadResponse toggleActiva(Long id) {
        Especialidad especialidad = especialidadRepository.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Especialidad", id));

        especialidad.setActiva(!especialidad.getActiva());
        Especialidad saved = especialidadRepository.save(especialidad);
        return EspecialidadDTO.EspecialidadResponse.fromEntity(saved);
    }

    /**
     * Elimina una especialidad (solo si no tiene productos asociados).
     */
    @Transactional
    public void delete(Long id) {
        Especialidad especialidad = especialidadRepository.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Especialidad", id));

        // Verificar que no tenga productos asociados
        // Esto se validará a nivel de BD por foreign key, pero podemos dar un mensaje más claro
        especialidadRepository.delete(especialidad);
    }
}