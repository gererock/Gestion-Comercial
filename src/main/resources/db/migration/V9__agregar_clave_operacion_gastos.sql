ALTER TABLE gastos
ADD COLUMN clave_operacion VARCHAR(50);


UPDATE gastos
SET clave_operacion = 'LEGACY-' || id_gasto
WHERE clave_operacion IS NULL;


ALTER TABLE gastos
ALTER COLUMN clave_operacion SET NOT NULL;


CREATE UNIQUE INDEX ux_gastos_clave_operacion
ON gastos (clave_operacion);