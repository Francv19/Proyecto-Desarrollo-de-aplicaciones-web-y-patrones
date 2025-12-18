-- Script para agregar las rutas de reseñas y configuración para el rol de owner (dueno)
-- Ejecutar este script en la base de datos para permitir acceso a /resenas/owner y /configuracion

-- Rutas de reseñas para owner
INSERT INTO ruta (ruta, id_rol) 
SELECT '/resenas/owner', 2
WHERE NOT EXISTS (
    SELECT 1 FROM ruta WHERE ruta = '/resenas/owner' AND id_rol = 2
);

INSERT INTO ruta (ruta, id_rol) 
SELECT '/resenas/owner/**', 2
WHERE NOT EXISTS (
    SELECT 1 FROM ruta WHERE ruta = '/resenas/owner/**' AND id_rol = 2
);

-- Rutas de configuración para owner
INSERT INTO ruta (ruta, id_rol) 
SELECT '/configuracion/owner', 2
WHERE NOT EXISTS (
    SELECT 1 FROM ruta WHERE ruta = '/configuracion/owner' AND id_rol = 2
);

INSERT INTO ruta (ruta, id_rol) 
SELECT '/configuracion/owner/**', 2
WHERE NOT EXISTS (
    SELECT 1 FROM ruta WHERE ruta = '/configuracion/owner/**' AND id_rol = 2
);

-- Rutas de configuración para admin
INSERT INTO ruta (ruta, id_rol) 
SELECT '/configuracion', 3
WHERE NOT EXISTS (
    SELECT 1 FROM ruta WHERE ruta = '/configuracion' AND id_rol = 3
);

INSERT INTO ruta (ruta, id_rol) 
SELECT '/configuracion/**', 3
WHERE NOT EXISTS (
    SELECT 1 FROM ruta WHERE ruta = '/configuracion/**' AND id_rol = 3
);

