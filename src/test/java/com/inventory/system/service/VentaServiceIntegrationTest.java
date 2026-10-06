package com.inventory.system.service;

import com.inventory.system.dto.DashboardDTO;
import com.inventory.system.dto.VentaDTO;
import com.inventory.system.entity.*;
import com.inventory.system.exception.StockInsuficienteException;
import com.inventory.system.exception.ValidacionException;
import com.inventory.system.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class VentaServiceIntegrationTest {

    @Autowired
    private VentaService ventaService;

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private DetalleVentaRepository detalleRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EspecialidadRepository especialidadRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    private Especialidad especialidad;
    private Categoria categoria;
    private Usuario usuario;
    private Producto producto1;
    private Producto producto2;

    @BeforeEach
    void setUp() {
        // Limpiar datos
        detalleRepository.deleteAllInBatch();
        ventaRepository.deleteAllInBatch();
        movimientoStockRepository.deleteAllInBatch();
        productoRepository.deleteAllInBatch();
        usuarioRepository.deleteAllInBatch();
        categoriaRepository.deleteAllInBatch();
        especialidadRepository.deleteAllInBatch();

        // Crear especialidad
        especialidad = Especialidad.builder()
            .codigo("DISENO")
            .nombre("Diseño Gráfico")
            .activa(true)
            .build();
        especialidad = especialidadRepository.save(especialidad);

        // Crear categoría
        categoria = Categoria.builder()
            .codigo("LLAVEROS")
            .nombre("Llaveros")
            .activa(true)
            .ordenVisualizacion(1)
            .build();
        categoria = categoriaRepository.save(categoria);

        // Crear usuario
        usuario = Usuario.builder()
            .email("vendedor@test.com")
            .passwordHash("$2a$10$test")
            .nombreCompleto("Vendedor Test")
            .rol(Usuario.RolUsuario.DOCENTE)
            .especialidad(especialidad)
            .activo(true)
            .build();
        usuario = usuarioRepository.save(usuario);

        // Crear productos con stock
        producto1 = Producto.builder()
            .codigoBarras("1111111111111")
            .codigoInterno("LLA-001")
            .nombre("Llavero Acrílico")
            .categoria(categoria)
            .especialidad(especialidad)
            .precioVenta(new BigDecimal("15.00"))
            .stockActual(20)
            .stockMinimo(5)
            .activo(true)
            .build();
        producto1 = productoRepository.save(producto1);

        producto2 = Producto.builder()
            .codigoBarras("2222222222222")
            .codigoInterno("LLA-002")
            .nombre("Llavero Metal")
            .categoria(categoria)
            .especialidad(especialidad)
            .precioVenta(new BigDecimal("25.00"))
            .stockActual(10)
            .stockMinimo(3)
            .activo(true)
            .build();
        producto2 = productoRepository.save(producto2);
    }

    @Autowired
    private MovimientoStockRepository movimientoStockRepository;

    @Test
    void procesarVenta_shouldCreateVentaAndDecreaseStock() {
        // Given
        VentaDTO.VentaRequest request = VentaDTO.VentaRequest.builder()
            .clienteNombre("Juan Pérez")
            .clienteDocumento("12345678")
            .especialidadId(especialidad.getId())
            .usuarioId(usuario.getId())
            .metodoPago(Venta.MetodoPago.EFECTIVO)
            .montoRecibido(new BigDecimal("100.00"))
            .detalles(List.of(
                VentaDTO.DetalleVentaRequest.builder()
                    .productoId(producto1.getId())
                    .cantidad(2)
                    .descuentoLinea(BigDecimal.ZERO)
                    .build(),
                VentaDTO.DetalleVentaRequest.builder()
                    .productoId(producto2.getId())
                    .cantidad(1)
                    .descuentoLinea(new BigDecimal("2.00"))
                    .build()
            ))
            .build();

        // When
        VentaDTO.VentaResponse response = ventaService.procesarVenta(request);

        // Then
        assertThat(response.getId()).isNotNull();
        assertThat(response.getNumeroVenta()).startsWith("VTA-");
        assertThat(response.getClienteNombre()).isEqualTo("Juan Pérez");
        assertThat(response.getEstado()).isEqualTo(Venta.EstadoVenta.COMPLETADA);
        assertThat(response.getMetodoPago()).isEqualTo(Venta.MetodoPago.EFECTIVO);
        assertThat(response.getDetalles()).hasSize(2);

        // Verificar totales: (15*2 - 0) + (25*1 - 2) = 30 + 23 = 53
        // subtotal incluye descuentos de línea, descuento global = 0
        assertThat(response.getSubtotal()).isEqualByComparingTo("53.00");
        assertThat(response.getDescuento()).isEqualByComparingTo("0.00");
        assertThat(response.getTotal()).isEqualByComparingTo("53.00");
        assertThat(response.getCambio()).isEqualByComparingTo("47.00"); // 100 - 53

        // Verificar que el stock se descontó
        Producto p1Updated = productoRepository.findById(producto1.getId()).orElseThrow();
        Producto p2Updated = productoRepository.findById(producto2.getId()).orElseThrow();
        assertThat(p1Updated.getStockActual()).isEqualTo(18); // 20 - 2
        assertThat(p2Updated.getStockActual()).isEqualTo(9);  // 10 - 1

        // Verificar movimientos de stock registrados
        List<MovimientoStock> movimientos = movimientoStockRepository.findByReferencia("VENTA", response.getId());
        assertThat(movimientos).hasSize(2);
    }

    @Test
    void procesarVenta_shouldThrowWhenStockInsuficiente() {
        // Given - producto2 tiene stock 10, pedimos 15
        VentaDTO.VentaRequest request = VentaDTO.VentaRequest.builder()
            .clienteNombre("Cliente Test")
            .especialidadId(especialidad.getId())
            .usuarioId(usuario.getId())
            .metodoPago(Venta.MetodoPago.EFECTIVO)
            .montoRecibido(new BigDecimal("500.00"))
            .detalles(List.of(
                VentaDTO.DetalleVentaRequest.builder()
                    .productoId(producto2.getId())
                    .cantidad(15)
                    .build()
            ))
            .build();

        // When/Then
        assertThatThrownBy(() -> ventaService.procesarVenta(request))
            .isInstanceOf(StockInsuficienteException.class)
            .hasMessageContaining("Stock insuficiente");

        // Verificar que el stock NO cambió
        Producto p2 = productoRepository.findById(producto2.getId()).orElseThrow();
        assertThat(p2.getStockActual()).isEqualTo(10);
    }

    @Test
    void procesarVenta_shouldThrowWhenMontoInsuficiente() {
        // Given - total sería 30, pero pagan 20
        VentaDTO.VentaRequest request = VentaDTO.VentaRequest.builder()
            .clienteNombre("Cliente Test")
            .especialidadId(especialidad.getId())
            .usuarioId(usuario.getId())
            .metodoPago(Venta.MetodoPago.EFECTIVO)
            .montoRecibido(new BigDecimal("20.00"))
            .detalles(List.of(
                VentaDTO.DetalleVentaRequest.builder()
                    .productoId(producto1.getId())
                    .cantidad(2) // 15 * 2 = 30
                    .build()
            ))
            .build();

        // When/Then
        assertThatThrownBy(() -> ventaService.procesarVenta(request))
            .isInstanceOf(ValidacionException.class)
            .hasMessageContaining("menor al total");
    }

    @Test
    void anularVenta_shouldRestoreStock() {
        // Given - primero crear una venta
        VentaDTO.VentaRequest request = VentaDTO.VentaRequest.builder()
            .clienteNombre("Cliente Para Anular")
            .especialidadId(especialidad.getId())
            .usuarioId(usuario.getId())
            .metodoPago(Venta.MetodoPago.EFECTIVO)
            .montoRecibido(new BigDecimal("100.00"))
            .detalles(List.of(
                VentaDTO.DetalleVentaRequest.builder()
                    .productoId(producto1.getId())
                    .cantidad(3)
                    .build()
            ))
            .build();

        VentaDTO.VentaResponse venta = ventaService.procesarVenta(request);

        // Verificar stock descontado
        assertThat(productoRepository.findById(producto1.getId()).orElseThrow().getStockActual()).isEqualTo(17);

        // When - anular venta
        VentaDTO.VentaResponse anulada = ventaService.anularVenta(venta.getId(), usuario.getId(), "Cliente se arrepintió");

        // Then
        assertThat(anulada.getEstado()).isEqualTo(Venta.EstadoVenta.ANULADA);

        // Verificar stock restaurado
        assertThat(productoRepository.findById(producto1.getId()).orElseThrow().getStockActual()).isEqualTo(20);
    }

    @Test
    void getKPIsVentas_shouldReturnCorrectMetrics() {
        // Given - crear algunas ventas
        for (int i = 0; i < 3; i++) {
            VentaDTO.VentaRequest req = VentaDTO.VentaRequest.builder()
                .clienteNombre("Cliente " + i)
                .especialidadId(especialidad.getId())
                .usuarioId(usuario.getId())
                .metodoPago(Venta.MetodoPago.EFECTIVO)
                .montoRecibido(new BigDecimal("100.00"))
                .detalles(List.of(
                    VentaDTO.DetalleVentaRequest.builder()
                        .productoId(producto1.getId())
                        .cantidad(1)
                        .build()
                ))
                .build();
            ventaService.procesarVenta(req);
        }

        LocalDateTime inicio = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime fin = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

        // When
        DashboardDTO.DashboardKPIsResponse kpis = ventaService.getKPIsVentas(especialidad.getId(), inicio, fin);

        // Then
        assertThat(kpis.getVentasHoy()).isEqualTo(3);
        assertThat(kpis.getIngresoHoy()).isEqualByComparingTo("45.00"); // 3 * 15
        assertThat(kpis.getTicketPromedioHoy()).isEqualByComparingTo("15.00");
    }
}