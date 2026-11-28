BEGIN;
INSERT INTO categorias (nombre) VALUES (:'tag' || '-categoria');
INSERT INTO marcas (nombre) VALUES (:'tag' || '-marca');
INSERT INTO productos (nombre, estado, precio_venta, stock_actual, id_categoria, id_marca)
SELECT :'tag' || '-' || nombres.sufijo, nombres.estado, 20.75, 18, c.id_categoria, m.id_marca
FROM (VALUES ('ACTIVO-' || repeat('descripción larga ', 4), 'ACTIVO'), ('PAUSADO', 'PAUSADO'), ('DISCONTINUADO', 'DISCONTINUADO')) AS nombres(sufijo, estado)
CROSS JOIN categorias c CROSS JOIN marcas m
WHERE c.nombre = :'tag' || '-categoria' AND m.nombre = :'tag' || '-marca';
INSERT INTO productos (nombre, estado, precio_venta, stock_actual)
VALUES (:'tag' || '-SIN-RELACIONES', 'ACTIVO', 10, 5);
INSERT INTO pedidos (numero_pedido, tipo_venta, estado_pedido, estado_pago, medio_entrega, id_medio_pago, fecha_creacion)
SELECT :'tag', 'LOCAL', 'ENTREGADO', 'PAGADO', 'RETIRO_LOCAL', id_medio_pago,
       (CURRENT_TIMESTAMP AT TIME ZONE 'America/Argentina/Cordoba')::date + time '12:00'
FROM medios_pago WHERE activo ORDER BY id_medio_pago LIMIT 1;
INSERT INTO detalle_pedido (id_pedido,id_producto,cantidad,precio_unitario,subtotal)
SELECT o.id_pedido,p.id_producto,4,11,40
FROM pedidos o CROSS JOIN productos p
WHERE o.numero_pedido = :'tag' AND starts_with(p.nombre, :'tag' || '-');
COMMIT;
