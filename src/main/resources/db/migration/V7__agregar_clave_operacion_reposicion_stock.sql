ALTER TABLE movimientos_stock
    ADD COLUMN clave_operacion VARCHAR(36);

CREATE UNIQUE INDEX ux_movimientos_stock_clave_operacion
    ON movimientos_stock (clave_operacion)
    WHERE clave_operacion IS NOT NULL;
