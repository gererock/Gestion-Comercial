-- US24: una venta local puede existir antes de registrar su pago (US26).
ALTER TABLE pedidos ALTER COLUMN id_medio_pago DROP NOT NULL;
ALTER TABLE pedidos ADD CONSTRAINT chk_pedido_medio_pago CHECK
 (id_medio_pago IS NOT NULL OR (tipo_venta = 'LOCAL' AND estado_pago = 'PENDIENTE'));
ALTER TABLE pedidos ADD COLUMN id_vendedor_responsable INT REFERENCES usuarios(id_usuario);
CREATE INDEX ix_pedidos_vendedor ON pedidos(id_vendedor_responsable);
