-- =====================================================================
-- MIGRACIÓN V2: Agregar columnas version para Optimistic Locking
-- =====================================================================

-- Tabla producto
ALTER TABLE producto ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

-- Tabla venta
ALTER TABLE venta ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

-- Tabla movimiento_stock
ALTER TABLE movimiento_stock ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

-- Tabla usuario
ALTER TABLE usuario ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

-- Tabla especialidad
ALTER TABLE especialidad ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

-- Tabla categoria
ALTER TABLE categoria ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

-- Tabla detalle_venta
ALTER TABLE detalle_venta ADD COLUMN version BIGINT NOT NULL DEFAULT 0;