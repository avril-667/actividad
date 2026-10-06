-- =====================================================================
-- ESQUEMA DE BASE DE DATOS - SISTEMA DE GESTIÓN DE INVENTARIO Y VENTAS
-- =====================================================================
-- Compatible con PostgreSQL y MySQL
-- =====================================================================

-- -----------------------------------------------------
-- Tabla: especialidad (Catálogo de especialidades/carreras)
-- -----------------------------------------------------
CREATE TABLE especialidad (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- -----------------------------------------------------
-- Tabla: usuario (Docentes y Alumnos)
-- -----------------------------------------------------
CREATE TABLE usuario (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    nombre_completo VARCHAR(200) NOT NULL,
    rol VARCHAR(20) NOT NULL CHECK (rol IN ('DOCENTE', 'ALUMNO')),
    especialidad_id BIGINT REFERENCES especialidad(id),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    ultimo_login TIMESTAMP,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_usuario_email ON usuario(email);
CREATE INDEX idx_usuario_especialidad ON usuario(especialidad_id);
CREATE INDEX idx_usuario_rol ON usuario(rol);

-- -----------------------------------------------------
-- Tabla: categoria (Categorías de productos)
-- -----------------------------------------------------
CREATE TABLE categoria (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    icono VARCHAR(50), -- Nombre del icono para UI (ej: 'key', 'cup', 'tshirt')
    orden_visualizacion INT NOT NULL DEFAULT 0,
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- -----------------------------------------------------
-- Tabla: producto (Catálogo de productos por especialidad)
-- -----------------------------------------------------
CREATE TABLE producto (
    id BIGSERIAL PRIMARY KEY,
    codigo_barras VARCHAR(50) UNIQUE, -- Código de barras / SKU único
    codigo_interno VARCHAR(30) NOT NULL UNIQUE, -- Código interno legible (ej: LLA-001)
    nombre VARCHAR(200) NOT NULL,
    descripcion TEXT,
    categoria_id BIGINT NOT NULL REFERENCES categoria(id),
    especialidad_id BIGINT NOT NULL REFERENCES especialidad(id),
    precio_venta DECIMAL(12, 2) NOT NULL CHECK (precio_venta >= 0),
    unidad_medida VARCHAR(20) NOT NULL DEFAULT 'UNIDAD', -- UNIDAD, METRO, KILO, PAQUETE
    stock_actual INT NOT NULL DEFAULT 0 CHECK (stock_actual >= 0),
    stock_minimo INT NOT NULL DEFAULT 5 CHECK (stock_minimo >= 0), -- Umbral para alerta Low Stock
    imagen_url VARCHAR(500), -- URL o path de la imagen
    activo BOOLEAN NOT NULL DEFAULT TRUE, -- Soft delete
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_producto_codigo_barras ON producto(codigo_barras);
CREATE INDEX idx_producto_codigo_interno ON producto(codigo_interno);
CREATE INDEX idx_producto_categoria ON producto(categoria_id);
CREATE INDEX idx_producto_especialidad ON producto(especialidad_id);
CREATE INDEX idx_producto_activo ON producto(activo);
CREATE INDEX idx_producto_stock ON producto(stock_actual);

-- -----------------------------------------------------
-- Tabla: movimiento_stock (Historial de entradas/salidas)
-- -----------------------------------------------------
CREATE TABLE movimiento_stock (
    id BIGSERIAL PRIMARY KEY,
    producto_id BIGINT NOT NULL REFERENCES producto(id),
    tipo_movimiento VARCHAR(20) NOT NULL CHECK (tipo_movimiento IN ('ENTRADA', 'SALIDA', 'AJUSTE', 'VENTA', 'DEVOLUCION')),
    cantidad INT NOT NULL CHECK (cantidad > 0),
    stock_anterior INT NOT NULL,
    stock_nuevo INT NOT NULL,
    motivo VARCHAR(100), -- COMPRA, VENTA, AJUSTE_INVENTARIO, MERMA, DEVOLUCION_CLIENTE
    referencia_id BIGINT, -- ID de venta, orden de compra, etc.
    referencia_tipo VARCHAR(30), -- VENTA, COMPRA, AJUSTE
    usuario_id BIGINT REFERENCES usuario(id), -- Quién realizó el movimiento
    observaciones TEXT,
    fecha_movimiento TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_movimiento_producto ON movimiento_stock(producto_id);
CREATE INDEX idx_movimiento_tipo ON movimiento_stock(tipo_movimiento);
CREATE INDEX idx_movimiento_fecha ON movimiento_stock(fecha_movimiento);
CREATE INDEX idx_movimiento_referencia ON movimiento_stock(referencia_tipo, referencia_id);

-- -----------------------------------------------------
-- Tabla: venta (Cabecera de venta / Transacción)
-- -----------------------------------------------------
CREATE TABLE venta (
    id BIGSERIAL PRIMARY KEY,
    numero_venta VARCHAR(30) NOT NULL UNIQUE, -- Formato: VTA-YYYYMMDD-XXXX
    fecha_venta TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cliente_nombre VARCHAR(200),
    cliente_documento VARCHAR(30), -- DNI, RUC, etc. (opcional)
    cliente_email VARCHAR(150),
    subtotal DECIMAL(12, 2) NOT NULL DEFAULT 0 CHECK (subtotal >= 0),
    descuento DECIMAL(12, 2) NOT NULL DEFAULT 0 CHECK (descuento >= 0),
    impuesto DECIMAL(12, 2) NOT NULL DEFAULT 0 CHECK (impuesto >= 0),
    total DECIMAL(12, 2) NOT NULL CHECK (total >= 0),
    metodo_pago VARCHAR(20) NOT NULL DEFAULT 'EFECTIVO' CHECK (metodo_pago IN ('EFECTIVO')),
    monto_recibido DECIMAL(12, 2) CHECK (monto_recibido >= 0),
    cambio DECIMAL(12, 2) NOT NULL DEFAULT 0 CHECK (cambio >= 0),
    estado VARCHAR(20) NOT NULL DEFAULT 'COMPLETADA' CHECK (estado IN ('PENDIENTE', 'COMPLETADA', 'ANULADA', 'DEVUELTA')),
    usuario_id BIGINT NOT NULL REFERENCES usuario(id), -- Vendedor
    especialidad_id BIGINT NOT NULL REFERENCES especialidad(id),
    observaciones TEXT,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_venta_numero ON venta(numero_venta);
CREATE INDEX idx_venta_fecha ON venta(fecha_venta);
CREATE INDEX idx_venta_usuario ON venta(usuario_id);
CREATE INDEX idx_venta_especialidad ON venta(especialidad_id);
CREATE INDEX idx_venta_estado ON venta(estado);
CREATE INDEX idx_venta_cliente ON venta(cliente_nombre);

-- -----------------------------------------------------
-- Tabla: detalle_venta (Líneas de venta)
-- -----------------------------------------------------
CREATE TABLE detalle_venta (
    id BIGSERIAL PRIMARY KEY,
    venta_id BIGINT NOT NULL REFERENCES venta(id) ON DELETE CASCADE,
    producto_id BIGINT NOT NULL REFERENCES producto(id),
    cantidad INT NOT NULL CHECK (cantidad > 0),
    precio_unitario DECIMAL(12, 2) NOT NULL CHECK (precio_unitario >= 0), -- Precio al momento de la venta
    descuento_linea DECIMAL(12, 2) NOT NULL DEFAULT 0 CHECK (descuento_linea >= 0),
    subtotal_linea DECIMAL(12, 2) NOT NULL CHECK (subtotal_linea >= 0),
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_detalle_venta ON detalle_venta(venta_id);
CREATE INDEX idx_detalle_producto ON detalle_venta(producto_id);

-- -----------------------------------------------------
-- Vista: v_producto_stock_estado (Para KPIs y alertas)
-- -----------------------------------------------------
CREATE OR REPLACE VIEW v_producto_stock_estado AS
SELECT
    p.id,
    p.codigo_interno,
    p.nombre,
    p.categoria_id,
    c.nombre AS categoria_nombre,
    p.especialidad_id,
    e.nombre AS especialidad_nombre,
    p.stock_actual,
    p.stock_minimo,
    p.precio_venta,
    p.activo,
    CASE
        WHEN p.stock_actual <= 0 THEN 'OUT_OF_STOCK'
        WHEN p.stock_actual <= p.stock_minimo THEN 'LOW_STOCK'
        ELSE 'IN_STOCK'
    END AS estado_stock
FROM producto p
JOIN categoria c ON p.categoria_id = c.id
JOIN especialidad e ON p.especialidad_id = e.id
WHERE p.activo = TRUE;

-- -----------------------------------------------------
-- Vista: v_resumen_ventas_diario (Para reportes)
-- -----------------------------------------------------
CREATE OR REPLACE VIEW v_resumen_ventas_diario AS
SELECT
    DATE(v.fecha_venta) AS fecha,
    v.especialidad_id,
    e.nombre AS especialidad_nombre,
    COUNT(v.id) AS total_ventas,
    SUM(v.total) AS ingreso_total,
    SUM(v.subtotal) AS subtotal_total,
    SUM(v.descuento) AS descuento_total,
    AVG(v.total) AS ticket_promedio
FROM venta v
JOIN especialidad e ON v.especialidad_id = e.id
WHERE v.estado = 'COMPLETADA'
GROUP BY DATE(v.fecha_venta), v.especialidad_id, e.nombre;

-- -----------------------------------------------------
-- Datos iniciales (Seed data)
-- -----------------------------------------------------

-- Especialidades
INSERT INTO especialidad (codigo, nombre, descripcion) VALUES
('DISENO', 'Diseño Gráfico', 'Especialidad de Diseño Gráfico y Comunicación Visual'),
('INFORMATICA', 'Informática', 'Especialidad de Informática y Programación'),
('ELECTRONICA', 'Electrónica', 'Especialidad de Electrónica y Automatización'),
('MECANICA', 'Mecánica Automotriz', 'Especialidad de Mecánica Automotriz');

-- Categorías
INSERT INTO categoria (codigo, nombre, descripcion, icono, orden_visualizacion) VALUES
('LLAVEROS', 'Llaveros', 'Llaveros personalizados y promocionales', 'key', 1),
('TAZAS', 'Tazas', 'Tazas cerámicas y térmicas personalizadas', 'cup', 2),
('PLAYERAS', 'Playeras', 'Playeras y polos estampados', 'tshirt', 3),
('STICKERS', 'Stickers', 'Stickers y vinilos adhesivos', 'sticker', 4),
('CARTUCHERAS', 'Cartucheras', 'Cartucheras y estuches escolares', 'pencil-case', 5),
('BOLSAS', 'Bolsas', 'Bolsas ecológicas y promocionales', 'shopping-bag', 6);

-- Usuario administrador por defecto (password: admin123 - BCrypt hash)
INSERT INTO usuario (email, password_hash, nombre_completo, rol, especialidad_id) VALUES
('admin@sistema.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'Administrador del Sistema', 'DOCENTE', 1);