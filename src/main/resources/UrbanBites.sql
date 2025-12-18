/* ============================================================
   Foodtrucks – Esquema completo alineado a 20 HUs
   ============================================================ */

/* ---------- 0) Creación de base de datos y usuario (opcional) ---------- */
DROP DATABASE IF EXISTS foodplaza;
CREATE DATABASE foodplaza CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE foodplaza;

/* ---------- (Opcional) Crear usuario de app con privilegios mínimos para la demo ----------*/
CREATE USER IF NOT EXISTS 'foodapp'@'localhost' IDENTIFIED BY 'cambia_esta_clave';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX
ON foodplaza.* TO 'foodapp'@'localhost';
FLUSH PRIVILEGES;

/* ---------- 1) Usuarios, roles y seguridad (HU 1–3) ---------- */
CREATE TABLE usuario (
  id_usuario       INT NOT NULL AUTO_INCREMENT,
  username         VARCHAR(50) NOT NULL,
  password         VARCHAR(255) NOT NULL,
  nombre           VARCHAR(80) NOT NULL,
  apellidos        VARCHAR(120) NOT NULL,
  correo           VARCHAR(120) NOT NULL,
  telefono         VARCHAR(25),
  activo           BOOLEAN NOT NULL DEFAULT TRUE,
  fecha_creacion   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_modificacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id_usuario),
  UNIQUE KEY uq_usuario_username (username),
  UNIQUE KEY uq_usuario_correo (correo)
) ENGINE=InnoDB;

CREATE TABLE rol (
  id_rol       INT NOT NULL AUTO_INCREMENT,
  nombre       VARCHAR(30) NOT NULL, /* 'cliente', 'dueno', 'admin' */
  descripcion  VARCHAR(200),
  PRIMARY KEY (id_rol),
  UNIQUE KEY uq_rol_nombre (nombre)
) ENGINE=InnoDB;

CREATE TABLE usuario_rol (
  id_usuario INT NOT NULL,
  id_rol     INT NOT NULL,
  PRIMARY KEY (id_usuario, id_rol),
  CONSTRAINT fk_ur_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
  CONSTRAINT fk_ur_rol FOREIGN KEY (id_rol) REFERENCES rol(id_rol)
) ENGINE=InnoDB;

CREATE TABLE ruta (
  id_ruta       INT NOT NULL AUTO_INCREMENT,
  ruta          VARCHAR(255) NOT NULL,
  id_rol        INT NULL,
  requiere_rol  BOOLEAN NOT NULL DEFAULT TRUE,
  fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_modificacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id_ruta),
  CONSTRAINT fk_ruta_rol FOREIGN KEY (id_rol) REFERENCES rol(id_rol),
  CHECK (id_rol IS NOT NULL OR requiere_rol = FALSE)
) ENGINE=InnoDB;

/* ---------- 2) Foodtrucks, menús y productos (HU 4–6, 15–16) ---------- */
CREATE TABLE foodtrucks (
  id_foodtruck       INT NOT NULL AUTO_INCREMENT,
  id_dueno           INT NOT NULL,
  nombre             VARCHAR(100) NOT NULL,
  descripcion        VARCHAR(300),
  telefono           VARCHAR(25),
  email              VARCHAR(120),
  porcentaje_puntos  TINYINT UNSIGNED DEFAULT 0 CHECK (porcentaje_puntos BETWEEN 0 AND 100),
  ruta_imagen        TEXT,
  activo             BOOLEAN NOT NULL DEFAULT TRUE,
  fecha_creacion     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_modificacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id_foodtruck),
  KEY ndx_food_dueno (id_dueno),
  CONSTRAINT fk_food_dueno FOREIGN KEY (id_dueno) REFERENCES usuario(id_usuario)
) ENGINE=InnoDB;

CREATE TABLE menu (
  id_menu        INT NOT NULL AUTO_INCREMENT,
  id_foodtruck   INT NOT NULL,
  nombre         VARCHAR(100) NOT NULL,
  descripcion    VARCHAR(300),
  orden          INT NOT NULL DEFAULT 1,
  activo         BOOLEAN NOT NULL DEFAULT TRUE,
  PRIMARY KEY (id_menu),
  KEY ndx_menu_food (id_foodtruck),
  CONSTRAINT fk_menu_food FOREIGN KEY (id_foodtruck) REFERENCES foodtrucks(id_foodtruck) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE productos (
  id_producto    INT NOT NULL AUTO_INCREMENT,
  id_foodtruck   INT NOT NULL,
  id_menu        INT NOT NULL,
  nombre         VARCHAR(120) NOT NULL,
  descripcion    VARCHAR(400),
  precio         DECIMAL(10,2) NOT NULL DEFAULT 0.01 CHECK (precio > 0),
  disponible     BOOLEAN NOT NULL DEFAULT TRUE,
  fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id_producto),
  KEY ndx_prod_food (id_foodtruck),
  KEY ndx_prod_menu (id_menu),
  CONSTRAINT fk_prod_food FOREIGN KEY (id_foodtruck) REFERENCES foodtrucks(id_foodtruck) ON DELETE CASCADE,
  CONSTRAINT fk_prod_menu FOREIGN KEY (id_menu) REFERENCES menu(id_menu) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE fotos_productos (
  id_foto      INT NOT NULL AUTO_INCREMENT,
  id_producto  INT NOT NULL,
  url          TEXT,
  alt_text     VARCHAR(150),
  formato      ENUM('jpg','jpeg','png','webp') NOT NULL DEFAULT 'jpg',
  bytes        INT UNSIGNED,
  activo       BOOLEAN NOT NULL DEFAULT TRUE,
  PRIMARY KEY (id_foto),
  KEY ndx_foto_prod (id_producto),
  CONSTRAINT fk_foto_producto FOREIGN KEY (id_producto) REFERENCES productos(id_producto) ON DELETE CASCADE
) ENGINE=InnoDB;

/* ---------- 3) Carritos y pedidos (HU 7–9) ---------- */
CREATE TABLE carritos (
  id_carrito   INT NOT NULL AUTO_INCREMENT,
  id_usuario   INT NOT NULL,
  estado       ENUM('abierto','confirmado','cancelado') NOT NULL DEFAULT 'abierto',
  fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id_carrito),
  KEY ndx_cart_user (id_usuario),
  CONSTRAINT fk_cart_user FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
) ENGINE=InnoDB;

CREATE TABLE detalle_carrito (
  id_detalle    INT NOT NULL AUTO_INCREMENT,
  id_carrito    INT NOT NULL,
  id_producto   INT NOT NULL,
  cantidad      INT NOT NULL DEFAULT 1 CHECK (cantidad > 0),
  precio_unit   DECIMAL(10,2) NOT NULL DEFAULT 0.01 CHECK (precio_unit >= 0),
  notas         VARCHAR(300),
  PRIMARY KEY (id_detalle),
  KEY ndx_dcart_carrito (id_carrito),
  KEY ndx_dcart_producto (id_producto),
  KEY idx_carrito_producto (id_carrito, id_producto),
  CONSTRAINT fk_dcart_carrito FOREIGN KEY (id_carrito) REFERENCES carritos(id_carrito) ON DELETE CASCADE,
  CONSTRAINT fk_dcart_producto FOREIGN KEY (id_producto) REFERENCES productos(id_producto) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE pedidos (
  id_pedido      INT NOT NULL AUTO_INCREMENT,
  id_usuario     INT NOT NULL,
  id_foodtruck   INT NOT NULL,
  estado         ENUM('recibido','en_preparacion','listo','entregado','cancelado') NOT NULL DEFAULT 'recibido',
  eta_minutos    INT,
  total_bruto    DECIMAL(10,2) NOT NULL DEFAULT 0,
  descuento      DECIMAL(10,2) NOT NULL DEFAULT 0,
  total_neto     DECIMAL(10,2) NOT NULL DEFAULT 0,
  notas          VARCHAR(300),
  fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_modificacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id_pedido),
  KEY ndx_pedido_user (id_usuario),
  KEY ndx_pedido_food (id_foodtruck),
  CONSTRAINT fk_pedido_user FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
  CONSTRAINT fk_pedido_food FOREIGN KEY (id_foodtruck) REFERENCES foodtrucks(id_foodtruck) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE detalle_pedido (
  id_detalle     INT NOT NULL AUTO_INCREMENT,
  id_pedido      INT NOT NULL,
  id_producto    INT NOT NULL,
  nombre_producto VARCHAR(150) NOT NULL,
  cantidad       INT NOT NULL CHECK (cantidad > 0),
  precio_unit    DECIMAL(10,2) NOT NULL CHECK (precio_unit >= 0),
  subtotal       DECIMAL(10,2) NOT NULL CHECK (subtotal >= 0),
  PRIMARY KEY (id_detalle),
  KEY ndx_dped_pedido (id_pedido),
  KEY ndx_dped_producto (id_producto),
  CONSTRAINT fk_dped_pedido FOREIGN KEY (id_pedido) REFERENCES pedidos(id_pedido),
  CONSTRAINT fk_dped_producto FOREIGN KEY (id_producto) REFERENCES productos(id_producto) ON DELETE CASCADE
) ENGINE=InnoDB;

/* ---------- 4) Horarios y ubicaciones (HU 10–11) ---------- */
CREATE TABLE horarios_foodtruck (
  id_horario     INT NOT NULL AUTO_INCREMENT,
  id_foodtruck   INT NOT NULL,
  dia_semana     TINYINT NOT NULL CHECK (dia_semana BETWEEN 1 AND 7), /* 1=Lunes .. 7=Domingo */
  direccion      VARCHAR(250),
  latitud        DECIMAL(10,6),
  longitud       DECIMAL(10,6),
  hora_apertura  TIME NOT NULL,
  hora_cierre    TIME NOT NULL,
  activo         BOOLEAN NOT NULL DEFAULT TRUE,
  PRIMARY KEY (id_horario),
  KEY ndx_horario_food (id_foodtruck, dia_semana),
  CONSTRAINT fk_horario_food FOREIGN KEY (id_foodtruck) REFERENCES foodtrucks(id_foodtruck) ON DELETE CASCADE
) ENGINE=InnoDB;

/* ---------- 5) Promociones (HU 15–16) ---------- */
CREATE TABLE promociones (
  id_promocion  INT NOT NULL AUTO_INCREMENT,
  id_foodtruck  INT NOT NULL,
  tipo_descuento ENUM('porcentaje','monto_fijo') NOT NULL DEFAULT 'porcentaje',
  valor         DECIMAL(10,2) NOT NULL CHECK (valor >= 0),
  descripcion   VARCHAR(500),
  fecha_inicio  DATETIME NOT NULL,
  fecha_fin     DATETIME NOT NULL,
  activo        BOOLEAN NOT NULL DEFAULT TRUE,
  PRIMARY KEY (id_promocion),
  KEY ndx_promo_food (id_foodtruck, fecha_inicio, fecha_fin),
  CONSTRAINT fk_promo_food FOREIGN KEY (id_foodtruck) REFERENCES foodtrucks(id_foodtruck) ON DELETE CASCADE
) ENGINE=InnoDB;

/* ---------- 6) Reseñas y moderación (HU 17–18) ---------- */
CREATE TABLE resenas (
  id_resena     INT NOT NULL AUTO_INCREMENT,
  id_usuario    INT NOT NULL,
  id_foodtruck  INT NOT NULL,
  id_pedido     INT NOT NULL,
  calificacion  TINYINT NOT NULL CHECK (calificacion BETWEEN 1 AND 5),
  comentario    VARCHAR(500),
  estado        ENUM('pendiente','aprobada','oculta') NOT NULL DEFAULT 'pendiente',
  fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id_resena),
  KEY ndx_resena_estado (estado),
  KEY ndx_resena_food (id_foodtruck),
  CONSTRAINT fk_resena_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
  CONSTRAINT fk_resena_food FOREIGN KEY (id_foodtruck) REFERENCES foodtrucks(id_foodtruck) ON DELETE CASCADE,
  CONSTRAINT fk_resena_pedido FOREIGN KEY (id_pedido) REFERENCES pedidos(id_pedido) ON DELETE CASCADE
) ENGINE=InnoDB;

/* ---------- 7) Puntos y reglas (HU 12–14) ---------- */
CREATE TABLE puntos_cliente (
  id_mov       INT NOT NULL AUTO_INCREMENT,
  id_usuario   INT NOT NULL,
  id_foodtruck INT NOT NULL,
  id_pedido    INT,
  tipo         ENUM('acumulados','redimidos') NOT NULL DEFAULT 'acumulados',
  puntos       INT NOT NULL CHECK (puntos >= 0),
  motivo       VARCHAR(200),
  fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id_mov),
  KEY ndx_pts_user (id_usuario),
  KEY ndx_pts_food (id_foodtruck),
  CONSTRAINT fk_pts_user FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
  CONSTRAINT fk_pts_food FOREIGN KEY (id_foodtruck) REFERENCES foodtrucks(id_foodtruck) ON DELETE CASCADE,
  CONSTRAINT fk_pts_pedido FOREIGN KEY (id_pedido) REFERENCES pedidos(id_pedido) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE reglas_puntos (
  id_regla       INT NOT NULL AUTO_INCREMENT,
  id_foodtruck   INT NOT NULL,
  porcentaje     TINYINT UNSIGNED NOT NULL CHECK (porcentaje BETWEEN 0 AND 100),
  fecha_inicio   DATETIME NOT NULL,
  fecha_fin      DATETIME NOT NULL,
  activo         BOOLEAN NOT NULL DEFAULT TRUE,
  PRIMARY KEY (id_regla),
  KEY ndx_regla_rango (id_foodtruck, fecha_inicio, fecha_fin),
  CONSTRAINT fk_regla_food FOREIGN KEY (id_foodtruck) REFERENCES foodtrucks(id_foodtruck) ON DELETE CASCADE
) ENGINE=InnoDB;

/* ---------- 8) Eventos con cotización embebida (HU 19–20) ---------- */
CREATE TABLE eventos (
  id_evento         INT NOT NULL AUTO_INCREMENT,
  id_foodtruck      INT NOT NULL,
  id_solicitante    INT NULL,  /* cliente que solicita */
  id_dueno_cotizador INT NULL,

  estado            ENUM('pendiente','cotizado','aceptado','rechazado','cancelado') NOT NULL DEFAULT 'pendiente',
  tipo_servicio     ENUM('catering','delivery','otro') NOT NULL DEFAULT 'catering',
  nombre            VARCHAR(120) NOT NULL,
  descripcion       VARCHAR(500),
  direccion         VARCHAR(250),
  invitados         INT CHECK (invitados IS NULL OR invitados >= 0),
  fecha_inicio      DATETIME NOT NULL,
  fecha_fin         DATETIME NOT NULL,
  latitud           DECIMAL(10,6),
  longitud          DECIMAL(10,6),
  monto_cotizado    DECIMAL(12,2) NULL CHECK (monto_cotizado IS NULL OR monto_cotizado >= 0),
  detalles_cotizacion VARCHAR(600) NULL,
  fecha_cotizacion  DATETIME NULL,

  fecha_creacion    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (id_evento),

  KEY ndx_evento_food (id_foodtruck),
  KEY ndx_evento_estado (estado),
  KEY ndx_evento_fecha (fecha_inicio),
  CONSTRAINT fk_evento_food FOREIGN KEY (id_foodtruck) REFERENCES foodtrucks(id_foodtruck) ON DELETE CASCADE,
  CONSTRAINT fk_evento_solicitante FOREIGN KEY (id_solicitante) REFERENCES usuario(id_usuario),
  CONSTRAINT fk_evento_dueno_cotizador FOREIGN KEY (id_dueno_cotizador) REFERENCES usuario(id_usuario),
  CHECK (fecha_fin > fecha_inicio)
) ENGINE=InnoDB;

/* ============================================================
   DATOS DE EJEMPLO (consistentes)
   ============================================================ */

/* Roles */
INSERT INTO rol (nombre, descripcion) VALUES
('cliente','Cliente que realiza pedidos'),
('dueno','Dueño de food truck'),
('admin','Administrador del sistema');

/* Usuarios - Contraseña para todos: 1234 */
INSERT INTO usuario (username,password,nombre,apellidos,correo,telefono,activo) VALUES 
('cliente@urbanbites.com','$2a$10$Hj2VE26rWukC.H.5fTXvee.rDEgJz8.0ZJG1ZXUaHTw6RI8xMaMA6','Julian','Castillo Mendez','cliente@urbanbites.com','4556-8978',TRUE),
('dueno@urbanbites.com','$2a$10$tvvr.0sKBSHxjnxsD.59De5tVKexmr2LvClQW7LKUZ2EThI2BIMCi','Pedro','Sanchez Valle','dueno@urbanbites.com','4789-1245',TRUE),
('admin@urbanbites.com','$2a$10$0c0teXWoPTO2LG9vLM3qQ.jYx.RWZKD/eVj.JCkW7y22/CE71mMYS','Karla','Flores Vega','admin@urbanbites.com','4574-8756',TRUE);

/* Mapear roles */
INSERT INTO usuario_rol (id_usuario, id_rol) VALUES
((SELECT id_usuario FROM usuario WHERE username = 'cliente@urbanbites.com'), (SELECT id_rol FROM rol WHERE nombre = 'cliente')),
((SELECT id_usuario FROM usuario WHERE username = 'dueno@urbanbites.com'), (SELECT id_rol FROM rol WHERE nombre = 'dueno')),
((SELECT id_usuario FROM usuario WHERE username = 'admin@urbanbites.com'), (SELECT id_rol FROM rol WHERE nombre = 'admin'));

/* Rutas públicas (no requieren autenticación) */
INSERT INTO ruta (ruta, requiere_rol) VALUES
('/', FALSE),
('/landing/**', FALSE),
('/errores/**', FALSE),
('/registro/**', FALSE),
('/login', FALSE),
('/logout', FALSE),
('/js/**', FALSE),
('/webjars/**', FALSE),
('/css/**', FALSE),
('/images/**', FALSE),
('/img/**', FALSE),
('/menu', FALSE),
('/menu/**', FALSE),
('/promociones', FALSE),
('/promociones/**', FALSE),
('/horarios', FALSE),
('/food-trucks', FALSE);

/* Rutas para ADMIN (id_rol = 3) */
INSERT INTO ruta (ruta, id_rol) VALUES
('/admin/horarios/**', 3),
('/configuracion', 3),
('/configuracion/**', 3),
('/admin/**', 3),
('/usuario/**', 3),
('/producto/**', 3),
('/categoria/**', 3),
('/reportes/**', 3);

/* Rutas para DUEÑO (id_rol = 2) */
INSERT INTO ruta (ruta, id_rol) VALUES
('/resenas/owner', 2),
('/resenas/owner/**', 2),
('/pedidos/*/estado', 2),
('/pedidos/*/eta', 2),
('/configuracion/owner', 2),
('/configuracion/owner/**', 2),
('/app/owner/**', 2),
('/pedidos/owner/**', 2),
('/cotizaciones/**', 2),
('/owner/productos/**', 2),
('/owner/foodtrucks/**', 2),
('/owner/reglas-puntos/**', 2),
('/owner/promociones/**', 2),
('/owner/eventos/**', 2),
('/owner/horarios/**', 2);

/* Rutas para CLIENTE (id_rol = 1) - autenticado */
INSERT INTO ruta (ruta, id_rol) VALUES
('/carrito', 1),
('/carrito/**', 1),
('/resenas', 1),
('/resenas/**', 1),
('/configuracion', 1),
('/configuracion/**', 1),
('/app/cliente/**', 1),
('/puntos/**', 1),
('/pedidos', 1),
('/eventos', 1),
('/eventos/**', 1);
/* ============================================================
   FIN DEL SCRIPT
   ============================================================ */
