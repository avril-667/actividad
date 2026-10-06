package com.inventory.system.service;

import com.inventory.system.dto.DashboardDTO;
import com.inventory.system.dto.VentaDTO;
import com.inventory.system.entity.DetalleVenta;
import com.inventory.system.entity.MovimientoStock;
import com.inventory.system.entity.Producto;
import com.inventory.system.entity.Usuario;
import com.inventory.system.entity.Venta;
import com.inventory.system.exception.RecursoNoEncontradoException;
import com.inventory.system.exception.StockInsuficienteException;
import com.inventory.system.exception.ValidacionException;
import com.inventory.system.repository.DetalleVentaRepository;
import com.inventory.system.repository.MovimientoStockRepository;
import com.inventory.system.repository.ProductoRepository;
import com.inventory.system.repository.UsuarioRepository;
import com.inventory.system.repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Servicio para gestión de ventas (POS/Terminal).
 * Procesa transacciones atómicas: crea venta, detalles y descuenta stock.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VentaService {

    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final MovimientoStockRepository movimientoStockRepository;

    // Contador atómico para números de venta secuenciales por día
    private final AtomicLong contadorVentaDiaria = new AtomicLong(0);
    private String ultimaFechaContador = "";

    // =====================================================================
    // Procesamiento de Ventas (POS)
    // =====================================================================

    /**
     * Procesa una venta completa de forma transaccional.
     * - Valida stock de todos los productos
     * - Crea la venta y sus detalles
     * - Descuenta stock de cada producto
     * - Registra movimientos de stock tipo VENTA
     * - Calcula totales y cambio
     */
    @Transactional
    public VentaDTO.VentaResponse procesarVenta(VentaDTO.VentaRequest request) {
        // Validar usuario
        Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
            .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", request.getUsuarioId()));

        // Inicializar proxy de especialidad AHORA (dentro de la transacción)
        // para evitar LazyInitializationException posterior
        usuario.getEspecialidad().getNombre();

        // Validar especialidad
        if (!usuario.getEspecialidad().getId().equals(request.getEspecialidadId())) {
            throw new ValidacionException(List.of("El usuario no pertenece a la especialidad indicada"));
        }

        // Validar y preparar detalles
        List<DetalleVenta> detalles = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (VentaDTO.DetalleVentaRequest detReq : request.getDetalles()) {
            Producto producto = productoRepository.findById(detReq.getProductoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto", detReq.getProductoId()));

            // Verificar que el producto pertenezca a la especialidad
            if (!producto.getEspecialidad().getId().equals(request.getEspecialidadId())) {
                throw new ValidacionException(List.of(
                    "El producto " + producto.getNombre() + " no pertenece a la especialidad seleccionada"
                ));
            }

            // Verificar stock disponible (SELECT FOR UPDATE para evitar race conditions)
            Producto productoLocked = productoRepository.findByIdForUpdate(detReq.getProductoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto", detReq.getProductoId()));

            // Verificar que el producto pertenezca a la especialidad
            if (!productoLocked.getEspecialidad().getId().equals(request.getEspecialidadId())) {
                throw new ValidacionException(List.of(
                    "El producto " + productoLocked.getNombre() + " no pertenece a la especialidad seleccionada"
                ));
            }

            // Verificar stock disponible
            if (!productoLocked.tieneStockSuficiente(detReq.getCantidad())) {
                throw new StockInsuficienteException(productoLocked.getId(), productoLocked.getStockActual(), detReq.getCantidad());
            }

            // Verificar que el producto esté activo
            if (!productoLocked.getActivo()) {
                throw new ValidacionException(List.of("El producto " + productoLocked.getNombre() + " no está disponible"));
            }

            // Usar precio actual del producto (precio al momento de la venta)
            BigDecimal precioUnitario = productoLocked.getPrecioVenta();
            BigDecimal descuentoLinea = detReq.getDescuentoLinea() != null ? detReq.getDescuentoLinea() : BigDecimal.ZERO;

            // Calcular subtotal de la línea
            BigDecimal subtotalBruto = precioUnitario.multiply(BigDecimal.valueOf(detReq.getCantidad()));
            BigDecimal subtotalLinea = subtotalBruto.subtract(descuentoLinea);

            // Crear detalle (no guardamos aún, solo preparamos)
            DetalleVenta detalle = DetalleVenta.builder()
                .producto(productoLocked)
                .cantidad(detReq.getCantidad())
                .precioUnitario(precioUnitario)
                .descuentoLinea(descuentoLinea)
                .subtotalLinea(subtotalLinea)
                .build();

            detalles.add(detalle);
            subtotal = subtotal.add(subtotalLinea);
        }

        // Calcular descuento global si se proporciona
        BigDecimal descuentoGlobal = request.getDescuentoGlobal() != null ? request.getDescuentoGlobal() : BigDecimal.ZERO;

        // Calcular total (impuesto = 0 por ahora, se puede extender para IGV/IVA)
        BigDecimal impuesto = BigDecimal.ZERO;
        BigDecimal total = subtotal.subtract(descuentoGlobal).add(impuesto);

        // Validar monto recibido para efectivo
        BigDecimal montoRecibido = request.getMontoRecibido();
        if (montoRecibido == null || montoRecibido.compareTo(total) < 0) {
            throw new ValidacionException(List.of(
                "El monto recibido (" + montoRecibido + ") es menor al total a pagar (" + total + ")"
            ));
        }

        BigDecimal cambio = montoRecibido.subtract(total);

        // Generar número de venta
        String numeroVenta = generarNumeroVenta(request.getEspecialidadId());

        // Crear venta
        Venta venta = Venta.builder()
            .numeroVenta(numeroVenta)
            .fechaVenta(LocalDateTime.now())
            .clienteNombre(request.getClienteNombre())
            .clienteDocumento(request.getClienteDocumento())
            .clienteEmail(request.getClienteEmail())
            .subtotal(subtotal)
            .descuento(descuentoGlobal)
            .impuesto(impuesto)
            .total(total)
            .metodoPago(request.getMetodoPago())
            .montoRecibido(montoRecibido)
            .cambio(cambio)
            .estado(Venta.EstadoVenta.COMPLETADA)
            .usuario(usuario)
            .especialidad(usuario.getEspecialidad())
            .observaciones(request.getObservaciones())
            .build();

        // Asociar detalles a la venta
        for (DetalleVenta detalle : detalles) {
            detalle.setVenta(venta);
        }
        venta.setDetalles(detalles);

        // Guardar venta (cascade guardará los detalles)
        Venta savedVenta = ventaRepository.save(venta);

        // Descontar stock y registrar movimientos para cada detalle
        for (DetalleVenta detalle : detalles) {
            Long productoId = detalle.getProducto().getId();
            int cantidad = detalle.getCantidad();

            // Capturar stock anterior ANTES de la actualización atómica
            Producto productoParaStock = productoRepository.findById(productoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto", productoId));
            int stockAnterior = productoParaStock.getStockActual();

            // Decrementar stock ATÓMICAMENTE (UPDATE con WHERE stock_actual >= cantidad)
            int filasAfectadas = productoRepository.decrementarStock(productoId, cantidad);
            if (filasAfectadas == 0) {
                // Si no se afectó ninguna fila, es porque no había stock suficiente
                // (condición de carrera detectada por la BD)
                Producto productoActual = productoRepository.findById(productoId)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Producto", productoId));
                throw new StockInsuficienteException(productoId, productoActual.getStockActual(), cantidad);
            }

            // Obtener stock nuevo después de la actualización atómica
            int stockNuevo = stockAnterior - cantidad;

            // Registrar movimiento de stock tipo VENTA
            MovimientoStock movimiento = MovimientoStock.builder()
                .producto(productoParaStock)
                .tipoMovimiento(MovimientoStock.TipoMovimiento.VENTA)
                .cantidad(cantidad)
                .stockAnterior(stockAnterior)
                .stockNuevo(stockNuevo)
                .motivo(MovimientoStock.Motivo.VENTA)
                .referenciaId(savedVenta.getId())
                .referenciaTipo("VENTA")
                .usuario(usuario)
                .observaciones("Venta N° " + numeroVenta)
                .build();

            movimientoStockRepository.save(movimiento);
        }

        return mapToResponse(savedVenta);
    }

    /**
     * Genera un número de venta único: VTA-YYYYMMDD-XXXX
     */
    private synchronized String generarNumeroVenta(Long especialidadId) {
        String hoy = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefijo = "VTA-" + hoy + "-";

        // Reiniciar contador si cambió el día
        if (!hoy.equals(ultimaFechaContador)) {
            ultimaFechaContador = hoy;
            contadorVentaDiaria.set(0);
        }

        long secuencia = contadorVentaDiaria.incrementAndGet();
        return String.format("%s%04d", prefijo, secuencia);
    }

    // =====================================================================
    // Consultas
    // =====================================================================

    /**
     * Obtiene una venta por ID con sus detalles.
     */
    public VentaDTO.VentaResponse findById(Long id) {
        Venta venta = ventaRepository.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Venta", id));
        return mapToResponse(venta);
    }

    /**
     * Obtiene una venta por número de venta.
     */
    public VentaDTO.VentaResponse findByNumeroVenta(String numeroVenta) {
        Venta venta = ventaRepository.findByNumeroVenta(numeroVenta)
            .orElseThrow(() -> new RecursoNoEncontradoException("Venta", "número: " + numeroVenta));
        return mapToResponse(venta);
    }

    /**
     * Obtiene ventas por especialidad y rango de fechas con paginación.
     */
    public Page<VentaDTO.VentaResponse> findByEspecialidadAndFechaBetween(
            Long especialidadId, LocalDateTime inicio, LocalDateTime fin, Pageable pageable) {
        return ventaRepository.findByEspecialidadIdAndFechaVentaBetween(especialidadId, inicio, fin, pageable)
            .map(this::mapToResponse);
    }

    /**
     * Obtiene KPIs de ventas para dashboard.
     */
    public DashboardDTO.DashboardKPIsResponse getKPIsVentas(Long especialidadId, LocalDateTime inicio, LocalDateTime fin) {
        BigDecimal ingresoTotal = ventaRepository.getTotalVentasByEspecialidadAndFechaBetween(especialidadId, inicio, fin);
        Long totalVentas = ventaRepository.countVentasCompletadasByEspecialidadAndFechaBetween(especialidadId, inicio, fin);
        BigDecimal ticketPromedio = ventaRepository.getTicketPromedioByEspecialidadAndFechaBetween(especialidadId, inicio, fin);

        return DashboardDTO.DashboardKPIsResponse.builder()
            .ingresoHoy(ingresoTotal)
            .ventasHoy(totalVentas)
            .ticketPromedioHoy(ticketPromedio)
            .build();
    }

    /**
     * Obtiene resumen diario de ventas.
     */
    public List<DashboardDTO.ResumenVentaDiarioResponse> getResumenDiario(Long especialidadId, LocalDateTime inicio, LocalDateTime fin) {
        List<Object[]> resultados = ventaRepository.getResumenDiarioByEspecialidad(especialidadId, inicio, fin);
        return resultados.stream()
            .map(row -> DashboardDTO.ResumenVentaDiarioResponse.builder()
                .fecha((java.time.LocalDate) row[0])
                .especialidadId(especialidadId)
                .especialidadNombre("") // Se completaría con join si necesario
                .totalVentas(((Number) row[1]).longValue())
                .ingresoTotal((BigDecimal) row[2])
                .build())
            .toList();
    }

    /**
     * Obtiene productos más vendidos.
     */
    public List<DashboardDTO.ProductoMasVendidoResponse> getProductosMasVendidos(Long especialidadId, LocalDateTime inicio, LocalDateTime fin) {
        List<Object[]> resultados = detalleRepository.getProductosMasVendidos(especialidadId, inicio, fin);
        return resultados.stream()
            .map(row -> DashboardDTO.ProductoMasVendidoResponse.builder()
                .productoId(((Producto) row[0]).getId())
                .productoNombre(((Producto) row[0]).getNombre())
                .productoCodigoInterno(((Producto) row[0]).getCodigoInterno())
                .totalVendido(((Number) row[1]).longValue())
                .ingresoTotal((BigDecimal) row[2])
                .build())
            .toList();
    }

    /**
     * Anula una venta (restaura stock).
     */
    @Transactional
    public VentaDTO.VentaResponse anularVenta(Long id, Long usuarioId, String motivo) {
        Venta venta = ventaRepository.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Venta", id));

        if (!venta.esModificable() && venta.getEstado() != Venta.EstadoVenta.COMPLETADA) {
            throw new ValidacionException(List.of("Solo se pueden anular ventas completadas"));
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", usuarioId));

        // Restaurar stock de cada producto
        for (DetalleVenta detalle : venta.getDetalles()) {
            Long productoId = detalle.getProducto().getId();
            int cantidad = detalle.getCantidad();

            // Capturar stock anterior
            Producto productoParaStock = productoRepository.findById(productoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto", productoId));
            int stockAnterior = productoParaStock.getStockActual();

            // Incrementar stock ATÓMICAMENTE
            int filasAfectadas = productoRepository.incrementarStock(productoId, cantidad);
            if (filasAfectadas == 0) {
                throw new RecursoNoEncontradoException("Producto", productoId);
            }

            int stockNuevo = stockAnterior + cantidad;

            // Registrar movimiento de devolución
            MovimientoStock movimiento = MovimientoStock.builder()
                .producto(productoParaStock)
                .tipoMovimiento(MovimientoStock.TipoMovimiento.DEVOLUCION)
                .cantidad(cantidad)
                .stockAnterior(stockAnterior)
                .stockNuevo(stockNuevo)
                .motivo(MovimientoStock.Motivo.DEVOLUCION_CLIENTE)
                .referenciaId(venta.getId())
                .referenciaTipo("ANULACION_VENTA")
                .usuario(usuario)
                .observaciones("Anulación venta N° " + venta.getNumeroVenta() + ": " + motivo)
                .build();

            movimientoStockRepository.save(movimiento);
        }

        venta.anular();
        Venta saved = ventaRepository.save(venta);
        return mapToResponse(saved);
    }

    private VentaDTO.VentaResponse mapToResponse(Venta venta) {
        List<VentaDTO.DetalleVentaResponse> detallesResponse = venta.getDetalles().stream()
            .map(det -> VentaDTO.DetalleVentaResponse.builder()
                .id(det.getId())
                .productoId(det.getProducto().getId())
                .productoNombre(det.getProducto().getNombre())
                .productoCodigoInterno(det.getProducto().getCodigoInterno())
                .cantidad(det.getCantidad())
                .precioUnitario(det.getPrecioUnitario())
                .descuentoLinea(det.getDescuentoLinea())
                .subtotalLinea(det.getSubtotalLinea())
                .build())
            .toList();

        return VentaDTO.VentaResponse.builder()
            .id(venta.getId())
            .numeroVenta(venta.getNumeroVenta())
            .fechaVenta(venta.getFechaVenta())
            .clienteNombre(venta.getClienteNombre())
            .clienteDocumento(venta.getClienteDocumento())
            .clienteEmail(venta.getClienteEmail())
            .subtotal(venta.getSubtotal())
            .descuento(venta.getDescuento())
            .impuesto(venta.getImpuesto())
            .total(venta.getTotal())
            .metodoPago(venta.getMetodoPago())
            .montoRecibido(venta.getMontoRecibido())
            .cambio(venta.getCambio())
            .estado(venta.getEstado())
            .usuarioId(venta.getUsuario().getId())
            .usuarioNombre(venta.getUsuario().getNombreCompleto())
            .especialidadId(venta.getEspecialidad().getId())
            .especialidadNombre(venta.getEspecialidad().getNombre())
            .observaciones(venta.getObservaciones())
            .detalles(detallesResponse)
            .fechaCreacion(venta.getFechaCreacion())
            .fechaActualizacion(venta.getFechaActualizacion())
            .build();
    }
}