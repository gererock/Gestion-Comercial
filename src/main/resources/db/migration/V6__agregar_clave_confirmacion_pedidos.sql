-- US10 - Identificación de cada confirmación online

ALTER TABLE pedidos
ADD COLUMN clave_confirmacion UUID;

CREATE UNIQUE INDEX ux_pedidos_clave_confirmacion
ON pedidos (clave_confirmacion)
WHERE clave_confirmacion IS NOT NULL;