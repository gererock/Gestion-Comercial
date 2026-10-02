-- ============================================================
-- Datos mínimos necesarios para empezar a utilizar la aplicación
-- ============================================================

-- Roles del sistema
INSERT INTO roles (nombre, descripcion) VALUES
    ('ADMINISTRADOR', 'Acceso completo a la gestión del comercio'),
    ('VENDEDOR', 'Gestión operativa de pedidos, clientes y ventas'),
    ('CLIENTE', 'Usuario de la tienda online')
ON CONFLICT DO NOTHING;

-- Medios de pago iniciales
INSERT INTO medios_pago (nombre, activo) VALUES
    ('Efectivo', TRUE),
    ('Transferencia', TRUE)
ON CONFLICT DO NOTHING;

-- Configuración inicial del comercio.
-- El monto puede modificarse luego desde US61.
INSERT INTO configuracion_comercio (
    monto_minimo_envio,
    permite_envio_villa_maria,
    permite_envio_villa_nueva
)
SELECT 25000.00, TRUE, TRUE
WHERE NOT EXISTS (SELECT 1 FROM configuracion_comercio);
