-- ============================================================
-- Usuarios iniciales para administración y ventas
-- Contraseña temporal para desarrollo: root
-- ============================================================

INSERT INTO usuarios (
    id_rol,
    nombre,
    apellido,
    email,
    password_hash,
    estado
)
SELECT
    r.id_rol,
    'Administrador',
    NULL,
    'admin@gestion.local',
    '$2a$10$kfn2MlQFp/VbN2ncw61Mqu7Ilbil/GCKB0xiG.4JieWgQ8l.f9M7y',
    'ACTIVO'
FROM roles r
WHERE r.nombre = 'ADMINISTRADOR'
  AND NOT EXISTS (
      SELECT 1
      FROM usuarios u
      WHERE LOWER(u.email) = LOWER('admin@gestion.local')
  );


INSERT INTO usuarios (
    id_rol,
    nombre,
    apellido,
    email,
    password_hash,
    estado
)
SELECT
    r.id_rol,
    'Vendedor',
    NULL,
    'vendedor@gestion.local',
    '$2a$10$ihYd1r98SkNsF2VHLhu4m.0YiDrHlKDmE/aMUkMvCIEMDAgwKOwXm',
    'ACTIVO'
FROM roles r
WHERE r.nombre = 'VENDEDOR'
  AND NOT EXISTS (
      SELECT 1
      FROM usuarios u
      WHERE LOWER(u.email) = LOWER('vendedor@gestion.local')
  );