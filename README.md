# Sistema de Gestión de Inventario y Control de Ventas por Especialidad

## Descripción General

Sistema web centralizado para gestionar stock en tiempo real, registrar ventas y generar reportes de ingresos para productos producidos por distintas especialidades (ej. Diseño Gráfico, Informática, Electrónica, Mecánica Automotriz).

## Stack Tecnológico

- **Backend:** Java 21 + Spring Boot 3.2.x
- **Frontend:** React / Angular (por implementar)
- **Base de Datos:** PostgreSQL (producción) / H2 (desarrollo/testing)
- **Seguridad:** JWT (JSON Web Tokens) + Spring Security
- **Documentación:** OpenAPI 3 / Swagger UI
- **Migraciones:** Flyway
- **Testing:** JUnit 5 + Testcontainers

## Arquitectura del Proyecto

```
inventory-system/
├── src/main/java/com/inventory/system/
│   ├── config/                 # Configuraciones (JPA, etc.)
│   ├── controller/             # Controladores REST
│   │   ├── AuthController.java
│   │   ├── CategoriaController.java
│   │   ├── EspecialidadController.java
│   │   ├── MovimientoStockController.java
│   │   ├── ProductoController.java
│   │   ├── UsuarioController.java
│   │   └── VentaController.java
│   ├── dto/                    # Data Transfer Objects
│   │   ├── AuthDTO.java
│   │   ├── CategoriaDTO.java
│   │   ├── DashboardDTO.java
│   │   ├── EspecialidadDTO.java
│   │   ├── MovimientoStockDTO.java
│   │   ├── ProductoDTO.java
│   │   ├── UsuarioDTO.java
│   │   └── VentaDTO.java
│   ├── entity/                 # Entidades JPA
│   │   ├── Categoria.java
│   │   ├── DetalleVenta.java
│   │   ├── Especialidad.java
│   │   ├── MovimientoStock.java
│   │   ├── Producto.java
│   │   ├── Usuario.java
│   │   └── Venta.java
│   ├── exception/              # Excepciones personalizadas
│   │   ├── GlobalExceptionHandler.java
│   │   ├── RecursoDuplicadoException.java
│   │   ├── RecursoNoEncontradoException.java
│   │   ├── StockInsuficienteException.java
│   │   └── ValidacionException.java
│   ├── repository/             # Repositorios Spring Data JPA
│   │   ├── CategoriaRepository.java
│   │   ├── DetalleVentaRepository.java
│   │   ├── EspecialidadRepository.java
│   │   ├── MovimientoStockRepository.java
│   │   ├── ProductoRepository.java
│   │   ├── UsuarioRepository.java
│   │   └── VentaRepository.java
│   ├── security/               # Configuración de seguridad JWT
│   │   ├── CustomUserDetailsService.java
│   │   ├── JwtAuthenticationFilter.java
│   │   ├── JwtTokenProvider.java
│   │   └── SecurityConfig.java
│   ├── service/                # Lógica de negocio
│   │   ├── AuthService.java
│   │   ├── CategoriaService.java
│   │   ├── EspecialidadService.java
│   │   ├── MovimientoStockService.java
│   │   ├── ProductoService.java
│   │   ├── UsuarioService.java
│   │   └── VentaService.java
│   └── InventorySystemApplication.java
├── src/main/resources/
│   ├── application.yml         # Configuración principal
│   ├── application-dev.yml     # Configuración desarrollo (H2)
│   └── db/migration/
│       └── V1__initial_schema.sql  # Esquema BD + datos semilla
└── src/test/                   # Tests unitarios e integración
```

## Modelo Entidad-Relación

### Tablas Principales

| Tabla | Descripción |
|-------|-------------|
| `especialidad` | Catálogo de especialidades/carreras |
| `usuario` | Docentes y Alumnos asignados a especialidades |
| `categoria` | Categorías de productos (Llaveros, Tazas, Playeras, etc.) |
| `producto` | Catálogo de productos con control de stock |
| `movimiento_stock` | Historial de entradas/salidas/ajustes de stock |
| `venta` | Cabecera de transacciones de venta (POS) |
| `detalle_venta` | Líneas de detalle de cada venta |

### Relaciones Clave

```
Especialidad 1──∞ Usuario
Especialidad 1──∞ Producto
Especialidad 1──∞ Venta
Categoria 1──∞ Producto
Producto 1──∞ MovimientoStock
Producto 1──∞ DetalleVenta
Venta 1──∞ DetalleVenta
Usuario 1──∞ Venta (vendedor)
Usuario 1──∞ MovimientoStock (responsable)
```

### Estados de Stock (Calculados Automáticamente)

- **IN_STOCK**: `stock_actual > stock_minimo`
- **LOW_STOCK**: `0 < stock_actual <= stock_minimo`
- **OUT_OF_STOCK**: `stock_actual <= 0`

## API Endpoints Principales

### Autenticación
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/auth/login` | Iniciar sesión (retorna JWT) |
| POST | `/api/auth/refresh` | Renovar access token |
| POST | `/api/auth/change-password` | Cambiar contraseña |
| GET | `/api/auth/me` | Info usuario autenticado |

### Especialidades
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/especialidades/activas` | Listar activas (para selects) |
| GET | `/api/especialidades` | Listar paginadas |
| GET | `/api/especialidades/{id}` | Obtener por ID |
| POST | `/api/especialidades` | Crear |
| PUT | `/api/especialidades/{id}` | Actualizar |
| PATCH | `/api/especialidades/{id}/toggle` | Activar/Desactivar |
| DELETE | `/api/especialidades/{id}` | Eliminar |

### Categorías
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/categorias/activas` | Listar activas ordenadas (para tabs UI) |
| GET | `/api/categorias` | Listar paginadas |
| POST/PUT/PATCH/DELETE | `/api/categorias/...` | CRUD completo |

### Productos y Stock
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/productos/especialidad/{id}` | Catálogo completo por especialidad |
| GET | `/api/productos/especialidad/{id}/categoria/{catId}` | Filtrar por categoría |
| GET | `/api/productos/especialidad/{id}/buscar?q=` | Buscar por nombre/código |
| GET | `/api/productos/codigo-barras/{codigo}` | Buscar por código barras (escáner) |
| GET | `/api/productos/especialidad/{id}/kpis` | KPIs dashboard (registrados, low, out) |
| GET | `/api/productos/especialidad/{id}/alertas/stock-bajo` | Productos stock bajo |
| GET | `/api/productos/especialidad/{id}/alertas/agotados` | Productos agotados |
| POST | `/api/productos` | Crear producto |
| PUT | `/api/productos/{id}` | Actualizar producto |
| PATCH | `/api/productos/{id}/toggle` | Activar/Desactivar (soft delete) |
| POST | `/api/productos/{id}/stock/entrada?cantidad=` | Incrementar stock |
| POST | `/api/productos/{id}/stock/salida?cantidad=` | Decrementar stock |
| POST | `/api/productos/{id}/stock/ajuste?stock=` | Ajuste inventario |
| PATCH | `/api/productos/{id}/stock/minimo?stockMinimo=` | Actualizar umbral alerta |

### Movimientos de Stock
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/stock/movimientos` | Registrar movimiento manual |
| GET | `/api/stock/movimientos/producto/{id}` | Historial por producto |
| GET | `/api/stock/movimientos/tipo/{tipo}` | Por tipo y rango fechas |
| GET | `/api/stock/movimientos/referencia?tipo=&id=` | Por referencia (venta) |
| GET | `/api/stock/movimientos/producto/{id}/resumen` | Resumen entradas/salidas |

### Ventas (POS/Terminal)
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/ventas` | **Procesar venta completa (transaccional)** |
| GET | `/api/ventas/{id}` | Obtener venta con detalles |
| GET | `/api/ventas/numero/{numero}` | Obtener por número de venta |
| GET | `/api/ventas/especialidad/{id}` | Historial paginado por fechas |
| POST | `/api/ventas/{id}/anular` | Anular venta (restaura stock) |
| GET | `/api/ventas/especialidad/{id}/kpis` | KPIs ventas (ingresos, ticket prom.) |
| GET | `/api/ventas/especialidad/{id}/resumen-diario` | Resumen diario |
| GET | `/api/ventas/especialidad/{id}/productos-mas-vendidos` | Ranking productos |

### Usuarios
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/usuarios/especialidad/{id}/rol/{rol}` | Listar por especialidad y rol |
| GET/POST/PUT/PATCH/DELETE | `/api/usuarios/...` | CRUD completo |

## Flujo de Procesamiento de Venta (Transaccional)

```java
// POST /api/ventas
{
  "clienteNombre": "Juan Pérez",
  "clienteDocumento": "12345678",
  "especialidadId": 1,
  "usuarioId": 5,
  "metodoPago": "EFECTIVO",
  "montoRecibido": 100.00,
  "detalles": [
    {"productoId": 10, "cantidad": 2, "descuentoLinea": 0},
    {"productoId": 15, "cantidad": 1, "descuentoLinea": 2.00}
  ]
}
```

**Operaciones atómicas en una sola transacción:**
1. ✅ Valida stock de TODOS los productos solicitados
2. ✅ Crea la venta y sus detalles
3. ✅ Descuenta stock de cada producto
4. ✅ Registra movimientos de stock tipo `VENTA`
5. ✅ Calcula totales, descuentos y cambio
6. ✅ Retorna venta completa con número único `VTA-YYYYMMDD-XXXX`

## Configuración y Ejecución

### Requisitos Previos
- Java 21+
- Maven 3.9+
- PostgreSQL 15+ (producción) o H2 (desarrollo)

### Variables de Entorno (.env)
```bash
# Base de Datos
DB_URL=jdbc:postgresql://localhost:5432/inventory_db
DB_USERNAME=postgres
DB_PASSWORD=postgres

# JWT
JWT_SECRET=tu_clave_secreta_base64_de_al_menos_256_bits

# Servidor
SERVER_PORT=8080

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:4200
```

### Ejecución Desarrollo (H2 In-Memory)
```bash
cd inventory-system
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```
- API: http://localhost:8080/api
- Swagger UI: http://localhost:8080/api/swagger-ui.html
- H2 Console: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:inventory_dev`)

### Ejecución Producción (PostgreSQL)
```bash
# 1. Crear BD
createdb inventory_db

# 2. Ejecutar migraciones (Flyway automático al iniciar)
./mvnw spring-boot:run
```

### Tests
```bash
# Tests unitarios
./mvnw test

# Tests con cobertura
./mvnw verify
```

## Datos Semilla (Seed Data)

El script `V1__initial_schema.sql` incluye:

- **4 Especialidades**: Diseño Gráfico, Informática, Electrónica, Mecánica Automotriz
- **6 Categorías**: Llaveros, Tazas, Playeras, Stickers, Cartucheras, Bolsas (con iconos y orden)
- **1 Usuario Admin**: `admin@sistema.com` / `admin123` (rol DOCENTE, especialidad Diseño)

## Seguridad

- **Autenticación**: Stateless JWT (Access Token 24h, Refresh Token 7d)
- **Autorización**: Por rol (DOCENTE/ALUMNO) y especialidad
- **Passwords**: BCrypt (strength 10)
- **CORS**: Configurado para localhost:3000, 4200, 5173

## Próximos Pasos (Fases 3-4)

### Fase 3: Frontend UI/UX
- [ ] Login responsive con estado de carga
- [ ] Dashboard con KPIs y alertas visuales
- [ ] Catálogo visual con tabs por categoría y búsqueda
- [ ] Terminal POS para registro de ventas
- [ ] Control de stock con alertas codificadas por color
- [ ] Reportes de ingresos con gráficos

### Fase 4: Pruebas y Cobertura
- [ ] Tests unitarios servicios (cobertura > 80%)
- [ ] Tests integración repositorios
- [ ] Tests E2E flujos críticos (venta, stock)
- [ ] Validación casos límite (stock 0, códigos duplicados, concurrencia)

## Documentación Adicional

- **OpenAPI/Swagger**: Disponible en `/api/swagger-ui.html` al ejecutar la app
- **Actuator Health**: `/api/actuator/health`
- **Métricas Prometheus**: `/api/actuator/prometheus`

## Convenciones de Código

- **Packages**: `com.inventory.system.{layer}`
- **Naming**: PascalCase clases, camelCase métodos/atributos
- **Validaciones**: Bean Validation (@NotNull, @Size, @DecimalMin, etc.)
- **Excepciones**: Personalizadas + GlobalExceptionHandler
- **Transacciones**: @Transactional en servicios (readOnly=true por defecto)
- **Auditoría**: @CreationTimestamp / @UpdateTimestamp en entidades

## Licencia

Proyecto educativo - Sistema de Gestión de Inventario y Ventas