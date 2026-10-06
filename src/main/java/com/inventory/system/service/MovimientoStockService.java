package com.inventory.system.service;

import com.inventory.system.dto.MovimientoStockDTO;
import com.inventory.system.entity.MovimientoStock;
import com.inventory.system.entity.Producto;
import com.inventory.system.entity.Usuario;
import com.inventory.system.exception.RecursoNoEncontradoException;
import com.inventory.system.exception.StockInsuficienteException;
import com.inventory.system.exception.ValidacionException;
import com.inventory.system.repository.MovimientoStockRepository;
import com.inventory.system.repository.ProductoRepository;
import com.inventory.system.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio para gestión de movimientos de stock.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MovimientoStockService {

    private final MovimientoStockRepository movimientoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Registra un movimiento de stock manual (entrada, salida, ajuste, devolución).
     * Nota: Para ventas, usar VentaService que registra el movimiento automáticamente.
     * Usa actualizaciones atómicas en BD para evitar race conditions.
     */
    @Transactional
    public MovimientoStockDTO.MovimientoStockResponse registrarMovimiento(MovimientoStockDTO.MovimientoStockRequest request) {
        // Validar producto con bloqueo pesimista
        Producto producto = productoRepository.findByIdForUpdate(request.getProductoId())
            .orElseThrow(() -> new RecursoNoEncontradoException("Producto", request.getProductoId()));

        // Validar usuario si se proporciona
        Usuario usuario = null;
        if (request.getUsuarioId() != null) {
            usuario = usuarioRepository.findById(request.getUsuarioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", request.getUsuarioId()));
        }

        int stockAnterior = producto.getStockActual();
        int stockNuevo;
        int filasAfectadas;

        // Aplicar movimiento al stock del producto usando UPDATEs atómicos
        switch (request.getTipoMovimiento()) {
            case ENTRADA, DEVOLUCION -> {
                filasAfectadas = productoRepository.incrementarStock(producto.getId(), request.getCantidad());
                if (filasAfectadas == 0) {
                    throw new RecursoNoEncontradoException("Producto", producto.getId());
                }
                stockNuevo = stockAnterior + request.getCantidad();
            }
            case SALIDA, VENTA -> {
                // Validar stock suficiente
                if (!producto.tieneStockSuficiente(request.getCantidad())) {
                    throw new StockInsuficienteException(producto.getId(), stockAnterior, request.getCantidad());
                }
                filasAfectadas = productoRepository.decrementarStock(producto.getId(), request.getCantidad());
                if (filasAfectadas == 0) {
                    // Race condition detectada por la BD
                    throw new StockInsuficienteException(producto.getId(), stockAnterior, request.getCantidad());
                }
                stockNuevo = stockAnterior - request.getCantidad();
            }
            case AJUSTE -> {
                if (request.getCantidad() < 0) {
                    throw new ValidacionException(List.of("El stock no puede ser negativo"));
                }
                filasAfectadas = productoRepository.ajustarStock(producto.getId(), request.getCantidad());
                if (filasAfectadas == 0) {
                    throw new RecursoNoEncontradoException("Producto", producto.getId());
                }
                stockNuevo = request.getCantidad();
            }
            default -> throw new ValidacionException(List.of("Tipo de movimiento no válido: " + request.getTipoMovimiento()));
        }

        // Crear registro de movimiento
        MovimientoStock movimiento = MovimientoStock.builder()
            .producto(producto)
            .tipoMovimiento(request.getTipoMovimiento())
            .cantidad(request.getCantidad())
            .stockAnterior(stockAnterior)
            .stockNuevo(stockNuevo)
            .motivo(request.getMotivo())
            .referenciaId(request.getReferenciaId())
            .referenciaTipo(request.getReferenciaTipo())
            .usuario(usuario)
            .observaciones(request.getObservaciones())
            .build();

        MovimientoStock saved = movimientoRepository.save(movimiento);
        return mapToResponse(saved);
    }

    /**
     * Obtiene el historial de movimientos de un producto.
     */
    public Page<MovimientoStockDTO.MovimientoStockResponse> getHistorialByProducto(Long productoId, Pageable pageable) {
        Producto producto = productoRepository.findById(productoId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Producto", productoId));

        return movimientoRepository.findByProductoOrderByFechaMovimientoDesc(producto, pageable)
            .map(this::mapToResponse);
    }

    /**
     * Obtiene movimientos por tipo en un rango de fechas.
     */
    public List<MovimientoStockDTO.MovimientoStockResponse> getByTipoAndFechaBetween(
            MovimientoStock.TipoMovimiento tipo, LocalDateTime inicio, LocalDateTime fin) {
        return movimientoRepository.findByTipoMovimientoAndFechaMovimientoBetween(tipo, inicio, fin).stream()
            .map(this::mapToResponse)
            .toList();
    }

    /**
     * Obtiene movimientos por referencia (ej. ID de venta).
     */
    public List<MovimientoStockDTO.MovimientoStockResponse> getByReferencia(String referenciaTipo, Long referenciaId) {
        return movimientoRepository.findByReferencia(referenciaTipo, referenciaId).stream()
            .map(this::mapToResponse)
            .toList();
    }

    /**
     * Obtiene resumen de movimientos por producto en un período.
     */
    public List<Object[]> getResumenByProducto(Long productoId, LocalDateTime inicio, LocalDateTime fin) {
        return movimientoRepository.getResumenMovimientosByProducto(productoId, inicio, fin);
    }

    private MovimientoStockDTO.MovimientoStockResponse mapToResponse(MovimientoStock movimiento) {
        return MovimientoStockDTO.MovimientoStockResponse.builder()
            .id(movimiento.getId())
            .productoId(movimiento.getProducto().getId())
            .productoNombre(movimiento.getProducto().getNombre())
            .productoCodigoInterno(movimiento.getProducto().getCodigoInterno())
            .tipoMovimiento(movimiento.getTipoMovimiento())
            .cantidad(movimiento.getCantidad())
            .stockAnterior(movimiento.getStockAnterior())
            .stockNuevo(movimiento.getStockNuevo())
            .motivo(movimiento.getMotivo())
            .referenciaId(movimiento.getReferenciaId())
            .referenciaTipo(movimiento.getReferenciaTipo())
            .usuarioId(movimiento.getUsuario() != null ? movimiento.getUsuario().getId() : null)
            .usuarioNombre(movimiento.getUsuario() != null ? movimiento.getUsuario().getNombreCompleto() : null)
            .observaciones(movimiento.getObservaciones())
            .fechaMovimiento(movimiento.getFechaMovimiento())
            .build();
    }
}