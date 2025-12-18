-- Script para verificar y corregir el orden de las rutas en la base de datos
-- Este script asegura que las rutas más específicas estén primero

-- Verificar rutas de configuración para owner
SELECT id_ruta, ruta, id_rol FROM ruta 
WHERE ruta LIKE '/configuracion%' AND id_rol = 2 
ORDER BY 
    CASE WHEN ruta = '/configuracion' THEN 1 ELSE 2 END,
    LENGTH(ruta) DESC;

-- Verificar rutas de reseñas para owner
SELECT id_ruta, ruta, id_rol FROM ruta 
WHERE ruta LIKE '/resenas%' AND id_rol IN (1, 2) 
ORDER BY id_rol, 
    CASE WHEN ruta = '/resenas/owner' THEN 1 
         WHEN ruta = '/resenas/owner/**' THEN 2
         WHEN ruta = '/resenas' THEN 3
         ELSE 4 END;

