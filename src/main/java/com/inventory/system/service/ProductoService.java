package com.inventory.system.service;

import com.inventory.system.dto.DashboardDTO;
import com.inventory.system.dto.ProductoDTO;
import com.inventory.system.entity.Categoria;
import com.inventory.system.entity.Especialidad;
import com.inventory.system.entity.Producto;
import com.inventory.system.exception.RecursoDuplicadoException;
import com.inventory.system.exception.RecursoNoEncontradoException;
import com.inventory.system.exception.StockInsuficienteException;
import com.inventory.system.exception.ValidacionException;
import com.inventory.system.repository.CategoriaRepository;
import com.inventory.system.repository.EspecialidadRepository;
import com.inventory.system.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para gestión de productos y control de stock.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final EspecialidadRepository especialidadRepository;

    // =====================================================================
    // Consultas básicas
    // =====================================================================

    /**
     * Obtiene todos los productos activos de una especialidad.
     */
    public List<ProductoDTO.ProductoResponse> findAllByEspecialidad(Long especialidadId) {
        return productoRepository.findByEspecialidadIdAndActivoTrue(especialidadId).stream()
            .map(ProductoDTO.ProductoResponse::fromEntity)
            .toList();
    }

    /**
     * Obtiene productos por categoría y especialidad.
     */
    public List<ProductoDTO.ProductoResponse> findByCategoriaAndEspecialidad(Long categoriaId, Long especialidadId) {
        return productoRepository.findByCategoriaIdAndEspecialidadIdAndActivoTrue(categoriaId, especialidadId).stream()
            .map(ProductoDTO.ProductoResponse::fromEntity)
            .toList();
    }

    /**
     * Busca productos por término (nombre o código interno).
     */
    public Page<ProductoDTO.ProductoResponse> buscar(Long especialidadId, String termino, Pageable pageable) {
        return productoRepository.buscarPorEspecialidadYTermino(especialidadId, termino, pageable)
            .map(ProductoDTO.ProductoResponse::fromEntity);
    }

    /**
     * Obtiene un producto por ID.
     */
    public ProductoDTO.ProductoResponse findById(Long id) {
        Producto producto = productoRepository.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Producto", id));
        return ProductoDTO.ProductoResponse.fromEntity(producto);
    }

    /**
     * Obtiene un producto por código de barras.
     */
    public ProductoDTO.ProductoResponse findByCodigoBarras(String codigoBarras) {
        Producto producto = productoRepository.findByCodigoBarras(codigoBarras)
            .orElseThrow(() -> new RecursoNoEncontradoException("Producto", "código de barras: " + codigoBarras));
        return ProductoDTO.ProductoResponse.fromEntity(producto);
    }

    /**
     * Obtiene un producto por código interno.
     */
    public ProductoDTO.ProductoResponse findByCodigoInterno(String codigoInterno) {
        Producto producto = productoRepository.findByCodigoInterno(codigoInterno)
            .orElseThrow(() -> new RecursoNoEncontradoException("Producto", "código interno: " + codigoInterno));
        return ProductoDTO.ProductoResponse.fromEntity(producto);
    }

    // =====================================================================
    // KPIs y Alertas de Stock
    // =====================================================================

    /**
     * Obtiene KPIs del dashboard para una especialidad.
     */
    public DashboardDTO.DashboardKPIsResponse getKPIs(Long especialidadId) {
        Long productosRegistrados = productoRepository.countProductosActivosByEspecialidad(especialidadId);
        Long stockBajo = productoRepository.countProductosLowStockByEspecialidad(especialidadId);
        Long agotados = productoRepository.countProductosOutOfStockByEspecialidad(especialidadId);

        return DashboardDTO.DashboardKPIsResponse.builder()
            .productosRegistrados(productosRegistrados)
            .stockBajo(stockBajo)
            .agotados(agotados)
            .build();
    }

    /**
     * Obtiene productos con stock bajo.
     */
    public List<ProductoDTO.ProductoResponse> getLowStock(Long especialidadId) {
        return productoRepository.findLowStockByEspecialidad(especialidadId).stream()
            .map(ProductoDTO.ProductoResponse::fromEntity)
            .toList();
    }

    /**
     * Obtiene productos agotados.
     */
    public List<ProductoDTO.ProductoResponse> getOutOfStock(Long especialidadId) {
        return productoRepository.findOutOfStockByEspecialidad(especialidadId).stream()
            .map(ProductoDTO.ProductoResponse::fromEntity)
            .toList();
    }

    /**
     * Obtiene todos los productos con su estado de stock calculado.
     */
    public List<ProductoDTO.ProductoResponse> getAllWithStockEstado(Long especialidadId) {
        return productoRepository.findAllWithStockEstadoByEspecialidad(especialidadId).stream()
            .map(ProductoDTO.ProductoResponse::fromEntity)
            .toList();
    }

    // =====================================================================
    // Operaciones CRUD
    // =====================================================================

    /**
     * Crea un nuevo producto.
     */
    @Transactional
    public ProductoDTO.ProductoResponse create(ProductoDTO.ProductoRequest request) {
        validarRequest(request, null);

        // Validar unicidad
        if (request.getCodigoBarras() != null && productoRepository.existsByCodigoBarrasAndIdNot(request.getCodigoBarras(), null)) {
            throw new RecursoDuplicadoException("Producto", "código de barras", request.getCodigoBarras());
        }
        if (productoRepository.existsByCodigoInternoIgnoreCaseAndIdNot(request.getCodigoInterno(), null)) {
            throw new RecursoDuplicadoException("Producto", "código interno", request.getCodigoInterno());
        }
        if (productoRepository.existsByNombreAndEspecialidadIdAndIdNot(request.getNombre(), request.getEspecialidadId(), null)) {
            throw new RecursoDuplicadoException("Producto", "nombre en esta especialidad", request.getNombre());
        }

        // Buscar entidades relacionadas
        Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
            .orElseThrow(() -> new RecursoNoEncontradoException("Categoría", request.getCategoriaId()));
        Especialidad especialidad = especialidadRepository.findById(request.getEspecialidadId())
            .orElseThrow(() -> new RecursoNoEncontradoException("Especialidad", request.getEspecialidadId()));

        Producto producto = buildProductoFromRequest(request, categoria, especialidad);
        Producto saved = productoRepository.save(producto);
        return ProductoDTO.ProductoResponse.fromEntity(saved);
    }

    /**
     * Actualiza un producto existente.
     */
    @Transactional
    public ProductoDTO.ProductoResponse update(Long id, ProductoDTO.ProductoRequest request) {
        Producto producto = productoRepository.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Producto", id));

        validarRequest(request, id);

        // Validar unicidad (excluyendo el actual)
        if (request.getCodigoBarras() != null && productoRepository.existsByCodigoBarrasAndIdNot(request.getCodigoBarras(), id)) {
            throw new RecursoDuplicadoException("Producto", "código de barras", request.getCodigoBarras());
        }
        if (productoRepository.existsByCodigoInternoIgnoreCaseAndIdNot(request.getCodigoInterno(), id)) {
            throw new RecursoDuplicadoException("Producto", "código interno", request.getCodigoInterno());
        }
        if (productoRepository.existsByNombreAndEspecialidadIdAndIdNot(request.getNombre(), request.getEspecialidadId(), id)) {
            throw new RecursoDuplicadoException("Producto", "nombre en esta especialidad", request.getNombre());
        }

        // Buscar entidades relacionadas si cambiaron
        if (!producto.getCategoria().getId().equals(request.getCategoriaId())) {
            Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría", request.getCategoriaId()));
            producto.setCategoria(categoria);
        }
        if (!producto.getEspecialidad().getId().equals(request.getEspecialidadId())) {
            Especialidad especialidad = especialidadRepository.findById(request.getEspecialidadId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Especialidad", request.getEspecialidadId()));
            producto.setEspecialidad(especialidad);
        }

        updateProductoFromRequest(producto, request);
        Producto saved = productoRepository.save(producto);
        return ProductoDTO.ProductoResponse.fromEntity(saved);
    }

    /**
     * Activa/desactiva un producto (soft delete).
     */
    @Transactional
    public ProductoDTO.ProductoResponse toggleActivo(Long id) {
        Producto producto = productoRepository.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Producto", id));

        producto.setActivo(!producto.getActivo());
        Producto saved = productoRepository.save(producto);
        return ProductoDTO.ProductoResponse.fromEntity(saved);
    }

    /**
     * Elimina un producto (solo si no tiene ventas asociadas).
     */
    @Transactional
    public void delete(Long id) {
        Producto producto = productoRepository.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Producto", id));

        try {
            productoRepository.delete(producto);
        } catch (DataIntegrityViolationException e) {
            if (e.getMessage() != null && e.getMessage().contains("detalle_venta")) {
                throw new ValidacionException(List.of(
                    "No se puede eliminar el producto porque tiene ventas asociadas. " +
                    "Considere desactivarlo (soft delete) en su lugar."
                ));
            }
            throw new ValidacionException(List.of("No se puede eliminar el producto por restricciones de integridad referencial"));
        }
    }

    // =====================================================================
    // Operaciones de Stock
    // =====================================================================

    /**
     * Incrementa el stock de un producto (entrada de mercancía).
     */
    @Transactional
    public ProductoDTO.ProductoResponse incrementarStock(Long id, int cantidad) {
        if (cantidad <= 0) {
            throw new ValidacionException(List.of("La cantidad debe ser mayor a 0"));
        }

        int filasAfectadas = productoRepository.incrementarStock(id, cantidad);
        if (filasAfectadas == 0) {
            throw new RecursoNoEncontradoException("Producto", id);
        }

        return findById(id);
    }

    /**
     * Decrementa el stock de un producto (salida/venta).
     */
    @Transactional
    public ProductoDTO.ProductoResponse decrementarStock(Long id, int cantidad) {
        if (cantidad <= 0) {
            throw new ValidacionException(List.of("La cantidad debe ser mayor a 0"));
        }

        int filasAfectadas = productoRepository.decrementarStock(id, cantidad);
        if (filasAfectadas == 0) {
            // Verificar si es producto no encontrado o stock insuficiente
            Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto", id));

            throw new StockInsuficienteException(id, producto.getStockActual(), cantidad);
        }

        return findById(id);
    }

    /**
     * Ajusta el stock a un valor específico.
     */
    @Transactional
    public ProductoDTO.ProductoResponse ajustarStock(Long id, int nuevoStock) {
        if (nuevoStock < 0) {
            throw new ValidacionException(List.of("El stock no puede ser negativo"));
        }

        int filasAfectadas = productoRepository.ajustarStock(id, nuevoStock);
        if (filasAfectadas == 0) {
            throw new RecursoNoEncontradoException("Producto", id);
        }

        return findById(id);
    }

    /**
     * Actualiza el stock mínimo de alerta.
     */
    @Transactional
    public ProductoDTO.ProductoResponse actualizarStockMinimo(Long id, int stockMinimo) {
        if (stockMinimo < 0) {
            throw new ValidacionException(List.of("El stock mínimo no puede ser negativo"));
        }

        int filasAfectadas = productoRepository.actualizarStockMinimo(id, stockMinimo);
        if (filasAfectadas == 0) {
            throw new RecursoNoEncontradoException("Producto", id);
        }

        return findById(id);
    }

    // =====================================================================
    // Métodos auxiliares privados
    // =====================================================================

    private void validarRequest(ProductoDTO.ProductoRequest request, Long excludedId) {
        List<String> errores = new ArrayList<>();

        if (request.getNombre() == null || request.getNombre().trim().isEmpty()) {
            errores.add("El nombre es obligatorio");
        }
        if (request.getCodigoInterno() == null || request.getCodigoInterno().trim().isEmpty()) {
            errores.add("El código interno es obligatorio");
        }
        if (request.getCategoriaId() == null) {
            errores.add("La categoría es obligatoria");
        }
        if (request.getEspecialidadId() == null) {
            errores.add("La especialidad es obligatoria");
        }
        if (request.getPrecioVenta() == null || request.getPrecioVenta().signum() < 0) {
            errores.add("El precio de venta debe ser mayor o igual a 0");
        }
        if (request.getStockActual() != null && request.getStockActual() < 0) {
            errores.add("El stock actual no puede ser negativo");
        }
        if (request.getStockMinimo() != null && request.getStockMinimo() < 0) {
            errores.add("El stock mínimo no puede ser negativo");
        }

        if (!errores.isEmpty()) {
            throw new ValidacionException(errores);
        }
    }

    private Producto buildProductoFromRequest(ProductoDTO.ProductoRequest request, Categoria categoria, Especialidad especialidad) {
        return Producto.builder()
            .codigoBarras(request.getCodigoBarras())
            .codigoInterno(request.getCodigoInterno().toUpperCase())
            .nombre(request.getNombre())
            .descripcion(request.getDescripcion())
            .categoria(categoria)
            .especialidad(especialidad)
            .precioVenta(request.getPrecioVenta())
            .unidadMedida(request.getUnidadMedida() != null ? request.getUnidadMedida() : "UNIDAD")
            .stockActual(request.getStockActual() != null ? request.getStockActual() : 0)
            .stockMinimo(request.getStockMinimo() != null ? request.getStockMinimo() : 5)
            .imagenUrl(request.getImagenUrl())
            .activo(request.getActivo() != null ? request.getActivo() : true)
            .build();
    }

    private void updateProductoFromRequest(Producto producto, ProductoDTO.ProductoRequest request) {
        producto.setCodigoBarras(request.getCodigoBarras());
        producto.setCodigoInterno(request.getCodigoInterno().toUpperCase());
        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecioVenta(request.getPrecioVenta());
        if (request.getUnidadMedida() != null) {
            producto.setUnidadMedida(request.getUnidadMedida());
        }
        if (request.getStockMinimo() != null) {
            producto.setStockMinimo(request.getStockMinimo());
        }
        if (request.getImagenUrl() != null) {
            producto.setImagenUrl(request.getImagenUrl());
        }
        if (request.getActivo() != null) {
            producto.setActivo(request.getActivo());
        }
    }
}