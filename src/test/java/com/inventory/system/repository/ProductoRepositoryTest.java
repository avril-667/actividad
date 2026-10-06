package com.inventory.system.repository;

import com.inventory.system.entity.Categoria;
import com.inventory.system.entity.Especialidad;
import com.inventory.system.entity.Producto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ProductoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private EspecialidadRepository especialidadRepository;

    private Especialidad crearEspecialidad() {
        Especialidad esp = Especialidad.builder()
            .codigo("TEST")
            .nombre("Test Especialidad")
            .activa(true)
            .build();
        return entityManager.persistAndFlush(esp);
    }

    private Categoria crearCategoria() {
        Categoria cat = Categoria.builder()
            .codigo("TEST_CAT")
            .nombre("Test Categoría")
            .activa(true)
            .ordenVisualizacion(1)
            .build();
        return entityManager.persistAndFlush(cat);
    }

    @Test
    void findByCodigoBarras_shouldReturnProducto() {
        // Given
        Especialidad esp = crearEspecialidad();
        Categoria cat = crearCategoria();

        Producto producto = Producto.builder()
            .codigoBarras("7890123456789")
            .codigoInterno("TEST-001")
            .nombre("Producto Test")
            .categoria(cat)
            .especialidad(esp)
            .precioVenta(new BigDecimal("100.00"))
            .stockActual(10)
            .stockMinimo(5)
            .activo(true)
            .build();

        entityManager.persistAndFlush(producto);

        // When
        Optional<Producto> found = productoRepository.findByCodigoBarras("7890123456789");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getNombre()).isEqualTo("Producto Test");
    }

    @Test
    void findByCodigoInterno_shouldReturnProducto() {
        // Given
        Especialidad esp = crearEspecialidad();
        Categoria cat = crearCategoria();

        Producto producto = Producto.builder()
            .codigoBarras("7890123456789")
            .codigoInterno("TEST-001")
            .nombre("Producto Test")
            .categoria(cat)
            .especialidad(esp)
            .precioVenta(new BigDecimal("100.00"))
            .stockActual(10)
            .stockMinimo(5)
            .activo(true)
            .build();

        entityManager.persistAndFlush(producto);

        // When
        Optional<Producto> found = productoRepository.findByCodigoInterno("TEST-001");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getCodigoInterno()).isEqualTo("TEST-001");
    }

    @Test
    void countProductosLowStockByEspecialidad_shouldReturnCorrectCount() {
        // Given
        Especialidad esp = crearEspecialidad();
        Categoria cat = crearCategoria();

        // Producto con stock bajo (stock > 0 && stock <= minimo)
        Producto lowStock = Producto.builder()
            .codigoBarras("1111111111111")
            .codigoInterno("LOW-001")
            .nombre("Producto Stock Bajo")
            .categoria(cat)
            .especialidad(esp)
            .precioVenta(new BigDecimal("50.00"))
            .stockActual(3)
            .stockMinimo(5)
            .activo(true)
            .build();

        // Producto agotado
        Producto outOfStock = Producto.builder()
            .codigoBarras("2222222222222")
            .codigoInterno("OUT-001")
            .nombre("Producto Agotado")
            .categoria(cat)
            .especialidad(esp)
            .precioVenta(new BigDecimal("30.00"))
            .stockActual(0)
            .stockMinimo(5)
            .activo(true)
            .build();

        // Producto OK
        Producto okStock = Producto.builder()
            .codigoBarras("3333333333333")
            .codigoInterno("OK-001")
            .nombre("Producto OK")
            .categoria(cat)
            .especialidad(esp)
            .precioVenta(new BigDecimal("20.00"))
            .stockActual(20)
            .stockMinimo(5)
            .activo(true)
            .build();

        entityManager.persistAndFlush(lowStock);
        entityManager.persistAndFlush(outOfStock);
        entityManager.persistAndFlush(okStock);

        // When
        long lowStockCount = productoRepository.countProductosLowStockByEspecialidad(esp.getId());
        long outOfStockCount = productoRepository.countProductosOutOfStockByEspecialidad(esp.getId());
        long totalActivos = productoRepository.countProductosActivosByEspecialidad(esp.getId());

        // Then
        assertThat(lowStockCount).isEqualTo(1);
        assertThat(outOfStockCount).isEqualTo(1);
        assertThat(totalActivos).isEqualTo(3);
    }

    @Test
    void decrementarStock_shouldDecreaseStockWhenSufficient() {
        // Given
        Especialidad esp = crearEspecialidad();
        Categoria cat = crearCategoria();

        Producto producto = Producto.builder()
            .codigoBarras("4444444444444")
            .codigoInterno("STOCK-001")
            .nombre("Producto Stock Test")
            .categoria(cat)
            .especialidad(esp)
            .precioVenta(new BigDecimal("100.00"))
            .stockActual(10)
            .stockMinimo(5)
            .activo(true)
            .build();

        entityManager.persistAndFlush(producto);

        // When
        int rowsAffected = productoRepository.decrementarStock(producto.getId(), 3);

        // Then
        assertThat(rowsAffected).isEqualTo(1);

        Producto updated = entityManager.find(Producto.class, producto.getId());
        assertThat(updated.getStockActual()).isEqualTo(7);
    }

    @Test
    void decrementarStock_shouldNotDecreaseWhenInsufficient() {
        // Given
        Especialidad esp = crearEspecialidad();
        Categoria cat = crearCategoria();

        Producto producto = Producto.builder()
            .codigoBarras("5555555555555")
            .codigoInterno("STOCK-002")
            .nombre("Producto Stock Insuficiente")
            .categoria(cat)
            .especialidad(esp)
            .precioVenta(new BigDecimal("100.00"))
            .stockActual(2)
            .stockMinimo(5)
            .activo(true)
            .build();

        entityManager.persistAndFlush(producto);

        // When
        int rowsAffected = productoRepository.decrementarStock(producto.getId(), 5);

        // Then
        assertThat(rowsAffected).isEqualTo(0);

        Producto unchanged = entityManager.find(Producto.class, producto.getId());
        assertThat(unchanged.getStockActual()).isEqualTo(2);
    }

    @Test
    void incrementarStock_shouldIncreaseStock() {
        // Given
        Especialidad esp = crearEspecialidad();
        Categoria cat = crearCategoria();

        Producto producto = Producto.builder()
            .codigoBarras("6666666666666")
            .codigoInterno("STOCK-003")
            .nombre("Producto Incrementar Stock")
            .categoria(cat)
            .especialidad(esp)
            .precioVenta(new BigDecimal("100.00"))
            .stockActual(5)
            .stockMinimo(5)
            .activo(true)
            .build();

        entityManager.persistAndFlush(producto);

        // When
        int rowsAffected = productoRepository.incrementarStock(producto.getId(), 10);

        // Then
        assertThat(rowsAffected).isEqualTo(1);

        Producto updated = entityManager.find(Producto.class, producto.getId());
        assertThat(updated.getStockActual()).isEqualTo(15);
    }

    @Test
    void findLowStockByEspecialidad_shouldReturnLowStockProducts() {
        // Given
        Especialidad esp = crearEspecialidad();
        Categoria cat = crearCategoria();

        Producto low1 = Producto.builder()
            .codigoBarras("7777777777777")
            .codigoInterno("LOW-002")
            .nombre("Stock Bajo 1")
            .categoria(cat)
            .especialidad(esp)
            .precioVenta(new BigDecimal("10.00"))
            .stockActual(2)
            .stockMinimo(5)
            .activo(true)
            .build();

        Producto low2 = Producto.builder()
            .codigoBarras("8888888888888")
            .codigoInterno("LOW-003")
            .nombre("Stock Bajo 2")
            .categoria(cat)
            .especialidad(esp)
            .precioVenta(new BigDecimal("20.00"))
            .stockActual(5)
            .stockMinimo(5)
            .activo(true)
            .build();

        Producto ok = Producto.builder()
            .codigoBarras("9999999999999")
            .codigoInterno("OK-002")
            .nombre("Stock OK")
            .categoria(cat)
            .especialidad(esp)
            .precioVenta(new BigDecimal("30.00"))
            .stockActual(10)
            .stockMinimo(5)
            .activo(true)
            .build();

        entityManager.persistAndFlush(low1);
        entityManager.persistAndFlush(low2);
        entityManager.persistAndFlush(ok);

        // When
        List<Producto> lowStock = productoRepository.findLowStockByEspecialidad(esp.getId());

        // Then
        assertThat(lowStock).hasSize(2);
        assertThat(lowStock).extracting(Producto::getCodigoInterno)
            .containsExactlyInAnyOrder("LOW-002", "LOW-003");
    }
}