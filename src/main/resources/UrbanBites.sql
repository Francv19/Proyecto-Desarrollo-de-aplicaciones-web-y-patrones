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

/* ---------- 2) Foodtrucks, menús y productos (HU 4–6, 15–16) ---------- */
CREATE TABLE foodtrucks (
  id_foodtruck       INT NOT NULL AUTO_INCREMENT,
  id_dueno           INT NOT NULL,
  nombre             VARCHAR(100) NOT NULL,
  descripcion        VARCHAR(300),
  telefono           VARCHAR(25),
  email              VARCHAR(120),
  porcentaje_puntos  TINYINT UNSIGNED DEFAULT 0 CHECK (porcentaje_puntos BETWEEN 0 AND 100),
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
  CONSTRAINT fk_menu_food FOREIGN KEY (id_foodtruck) REFERENCES foodtrucks(id_foodtruck)
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
  CONSTRAINT fk_prod_food FOREIGN KEY (id_foodtruck) REFERENCES foodtrucks(id_foodtruck),
  CONSTRAINT fk_prod_menu FOREIGN KEY (id_menu) REFERENCES menu(id_menu)
) ENGINE=InnoDB;

CREATE TABLE fotos_productos (
  id_foto      INT NOT NULL AUTO_INCREMENT,
  id_producto  INT NOT NULL,
  url          VARCHAR(500) NOT NULL,
  alt_text     VARCHAR(150),
  formato      ENUM('jpg','jpeg','png','webp') NOT NULL DEFAULT 'jpg',
  bytes        INT UNSIGNED,
  activo       BOOLEAN NOT NULL DEFAULT TRUE,
  PRIMARY KEY (id_foto),
  KEY ndx_foto_prod (id_producto),
  CONSTRAINT fk_foto_producto FOREIGN KEY (id_producto) REFERENCES productos(id_producto)
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
  CONSTRAINT fk_dcart_producto FOREIGN KEY (id_producto) REFERENCES productos(id_producto)
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
  CONSTRAINT fk_pedido_food FOREIGN KEY (id_foodtruck) REFERENCES foodtrucks(id_foodtruck)
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
  CONSTRAINT fk_dped_producto FOREIGN KEY (id_producto) REFERENCES productos(id_producto)
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
  CONSTRAINT fk_horario_food FOREIGN KEY (id_foodtruck) REFERENCES foodtrucks(id_foodtruck)
) ENGINE=InnoDB;

/* ---------- 5) Promociones (HU 15–16) ---------- */
CREATE TABLE promociones (
  id_promocion  INT NOT NULL AUTO_INCREMENT,
  id_foodtruck  INT NOT NULL,
  tipo_descuento ENUM('porcentaje','monto_fijo') NOT NULL DEFAULT 'porcentaje',
  valor         DECIMAL(10,2) NOT NULL CHECK (valor >= 0),
  fecha_inicio  DATETIME NOT NULL,
  fecha_fin     DATETIME NOT NULL,
  activo        BOOLEAN NOT NULL DEFAULT TRUE,
  PRIMARY KEY (id_promocion),
  KEY ndx_promo_food (id_foodtruck, fecha_inicio, fecha_fin),
  CONSTRAINT fk_promo_food FOREIGN KEY (id_foodtruck) REFERENCES foodtrucks(id_foodtruck)
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
  CONSTRAINT fk_resena_food FOREIGN KEY (id_foodtruck) REFERENCES foodtrucks(id_foodtruck),
  CONSTRAINT fk_resena_pedido FOREIGN KEY (id_pedido) REFERENCES pedidos(id_pedido)
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
  CONSTRAINT fk_pts_food FOREIGN KEY (id_foodtruck) REFERENCES foodtrucks(id_foodtruck),
  CONSTRAINT fk_pts_pedido FOREIGN KEY (id_pedido) REFERENCES pedidos(id_pedido)
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
  CONSTRAINT fk_regla_food FOREIGN KEY (id_foodtruck) REFERENCES foodtrucks(id_foodtruck)
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
  CONSTRAINT fk_evento_food FOREIGN KEY (id_foodtruck) REFERENCES foodtrucks(id_foodtruck),
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

/* Usuarios */
INSERT INTO usuario (username,password,nombre,apellidos,correo,telefono,activo) VALUES 
('cliente1','clavecliente1','Julian','Castillo Mendez','jcastillo@gmail.com','4556-8978',TRUE),
('dueno1','clavefoodtr1','Pedro','Sanchez Valle','pedro.sv@gmail.com','4789-1245',TRUE),
('dueno2','clavefoodtr2','Abigail','Villalobos Vega','abigail.vv@gmail.com','4789-1246',TRUE),
('dueno3','clavefoodtr3','Miguel','Solano Lopez','miguel.sl@gmail.com','4789-1247',TRUE),
('admin1','claveadmin1','Karla','Flores Vega','kflores@gmail.com','4574-8756',TRUE);

/* Mapear roles */
INSERT INTO usuario_rol (id_usuario, id_rol) VALUES
(1,1),(2,2),(3,2),(4,2),(5,3);

/* Foodtrucks (dueños = usuarios 2,3,4) */
INSERT INTO foodtrucks (id_dueno,nombre,descripcion,telefono,email,porcentaje_puntos,activo) VALUES 
(2,'Grill_Go','Hamburguesas Artesanales','2222-8745','burgersgrillgo@gmail.com',5,TRUE),
(3,'La Taqueria 506','Tacos','2756-8125','tacos@gmail.com',4,TRUE),
(4,'Frosty Van','Helados','2468-1032','frosty@gmail.com',7,TRUE);

/* Menús */
INSERT INTO menu (id_foodtruck,nombre,descripcion,orden,activo) VALUES
(1,'Hamburguesas Artesanales','Diferentes tipos',1,TRUE),
(2,'Tacos','Diferentes tipos de carne',1,TRUE),
(3,'Helados','Diferentes sabores',1,TRUE);

/* Productos */
INSERT INTO productos (id_foodtruck,id_menu,nombre,descripcion,precio,disponible) VALUES
(1,1,'Burger Tica','Torta de res, tomate, lechuga, queso',3500,TRUE),
(2,2,'Tacos de Birria','Tortilla de maíz, birria, cebolla, culantro',4500,TRUE),
(3,3,'Copa de Helados','Fresa con topping de fresas',2300,TRUE);

INSERT INTO fotos_productos (id_producto,url,alt_text,formato,bytes,activo) VALUES
(1,'https://cdn.example.com/img/burger_tica.jpg','Burger Tica','jpg',125000,TRUE),
(2,'https://cdn.example.com/img/tacos_birria.jpg','Tacos de Birria','jpg',112000,TRUE),
(3,'https://cdn.example.com/img/copa_helados.webp','Copa de Helados','webp',98000,TRUE);

/* Carrito y detalles */
INSERT INTO carritos (id_usuario,estado) VALUES (1,'abierto');
INSERT INTO detalle_carrito (id_carrito,id_producto,cantidad,precio_unit,notas) VALUES
(1,1,2,3500,'Sin cebolla'),
(1,2,1,4500,'Sin culantro');

/* Pedido + detalle */
INSERT INTO pedidos (id_usuario,id_foodtruck,estado,eta_minutos,total_bruto,descuento,total_neto,notas)
VALUES (1,2,'recibido',15,9000,0,9000,'Sin nota');

INSERT INTO detalle_pedido (id_pedido,id_producto,nombre_producto,cantidad,precio_unit,subtotal)
VALUES (1,2,'Tacos de Birria',2,4500,9000);

/* Horarios */
INSERT INTO horarios_foodtruck (id_foodtruck,dia_semana,direccion,latitud,longitud,hora_apertura,hora_cierre,activo) VALUES
(3,2,'Sabana Urbanites',9.933883,-84.100830,'10:00:00','18:00:00',TRUE),
(2,3,'Sabana Urbanites',9.933883,-84.100830,'09:00:00','18:00:00',TRUE),
(1,5,'Sabana Urbanites',9.933883,-84.100830,'11:00:00','19:00:00',TRUE);

/* Promociones */
INSERT INTO promociones (id_foodtruck,tipo_descuento,valor,fecha_inicio,fecha_fin,activo) VALUES
(3,'porcentaje',15.00,'2025-11-15 00:00:00','2025-11-21 23:59:59',TRUE),
(2,'porcentaje',5.00,'2025-11-01 00:00:00','2025-12-01 23:59:59',TRUE),
(1,'porcentaje',10.00,'2025-11-15 00:00:00','2025-11-30 23:59:59',TRUE);

/* Regla de puntos */
INSERT INTO reglas_puntos (id_foodtruck,porcentaje,fecha_inicio,fecha_fin,activo) VALUES
(2,6,'2025-11-01 00:00:00','2025-12-31 23:59:59',TRUE);

/* Entregar pedido (sin trigger; luego insertas puntos manuales) */
UPDATE pedidos SET estado='entregado' WHERE id_pedido=1;

/* Reseña */
INSERT INTO resenas (id_usuario,id_foodtruck,id_pedido,calificacion,comentario,estado)
VALUES (1,2,1,5,'Excelente comida y servicio rápido','aprobada');

/* Puntos manuales */
INSERT INTO puntos_cliente (id_usuario,id_foodtruck,id_pedido,tipo,puntos,motivo)
VALUES (1,2,1,'acumulados',8,'Promo bienvenida');

/* Evento*/
INSERT INTO eventos (
  id_foodtruck, id_solicitante, id_dueno_cotizador, estado, tipo_servicio,
  nombre, descripcion, direccion, invitados,
  fecha_inicio, fecha_fin, latitud, longitud,
  monto_cotizado, detalles_cotizacion, fecha_cotizacion
)
VALUES (
  1, 1, 2, 'cotizado', 'catering',
  'Evento Empresa BAC','Almuerzo corporativo con 25 personas',
  'Oficinas BAC ESCAZÚ, San José',25,
  '2025-12-05 14:00:00','2025-12-05 17:00:00',9.920000,-84.082400,
  275000.00,'Menú premium con bebidas incluidas', NOW()
);

/* ============================================================
   FIN DEL SCRIPT
   ============================================================ */
