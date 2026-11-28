BEGIN;
DELETE FROM detalle_pedido WHERE id_pedido IN (SELECT id_pedido FROM pedidos WHERE numero_pedido = :'tag');
DELETE FROM pedidos WHERE numero_pedido = :'tag';
DELETE FROM productos WHERE starts_with(nombre, :'tag' || '-');
DELETE FROM categorias WHERE nombre = :'tag' || '-categoria';
DELETE FROM marcas WHERE nombre = :'tag' || '-marca';
COMMIT;
