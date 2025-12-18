# Urban Bites 🚚🍔

## Proyecto de Desarrollo de Aplicaciones Web y Patrones
### Universidad Fidelitas

---

## 👥 Integrantes del Equipo

- **CALDERON BARAHONA JOSHUA IGNACIO**
- **CAMPOS VALVERDE FRANCINNY MARIA**
- **NUÑEZ SALAS ALEJANDRO**
- **VASQUEZ GONGORA ERICK JOSUE**

---

## 📋 Descripción del Proyecto

**Urban Bites** es una plataforma web desarrollada en Spring Boot para la gestión de una plaza de food trucks. El sistema permite a los clientes realizar pedidos, acumular puntos, dejar reseñas y gestionar sus compras, mientras que los dueños de food trucks pueden administrar sus menús, productos, pedidos y promociones. Los administradores tienen acceso completo para gestionar usuarios, roles y configuraciones del sistema.

---

## 🛠️ Tecnologías Utilizadas

- **Backend**: Spring Boot 3.x
- **Base de Datos**: MySQL
- **Frontend**: Thymeleaf + Bootstrap 5
- **Seguridad**: Spring Security
- **ORM**: JPA/Hibernate
- **Build Tool**: Maven
- **IDE**: Apache NetBeans
- **Almacenamiento**: Firebase Storage (para imágenes)

---

## 📦 Requisitos Previos

Antes de ejecutar el proyecto, asegúrate de tener instalado:

- **Java JDK 17** o superior
- **Apache NetBeans** (versión 14 o superior recomendada)
- **MySQL** (versión 8.0 o superior)
- **Maven** (incluido en NetBeans)
- **Git** (opcional, para control de versiones)

---

## 🚀 Instalación y Configuración

### 1. Clonar o Descargar el Proyecto

```bash
git clone <url-del-repositorio>
cd UrbanBitesApp
```

### 2. Configurar la Base de Datos

Ejecuta el script SQL ubicado en:
   ```
   src/main/resources/UrbanBites.sql
   ```
   Este script creará todas las tablas necesarias con sus relaciones y datos iniciales.

### 3. Configurar las Propiedades de la Aplicación

Edita el archivo `src/main/resources/application.properties` y configura:

```properties
# Configuración de la base de datos
spring.datasource.url=jdbc:mysql://localhost:3306/urbanbites?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=tu_usuario_mysql
spring.datasource.password=tu_contraseña_mysql
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Configuración de JPA
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
spring.jpa.properties.hibernate.format_sql=true

# Puerto del servidor
server.port=8085
```

### 4. Configurar Firebase Storage (Opcional)

Si deseas usar Firebase Storage para subir imágenes:

1. Coloca tu archivo JSON de credenciales de Firebase en:
   ```
   src/main/resources/firebase/
   ```

2. Actualiza la ruta en `FirebaseStorageService.java`:
   ```java
   final String archivoJsonFile = "tu-archivo-firebase.json";
   final String BucketName = "tu-bucket-name.firebasestorage.app";
   ```

---

## ▶️ Ejecutar el Proyecto en NetBeans

### Opción 1: Ejecutar desde NetBeans

1. Abre NetBeans
2. Selecciona **File → Open Project**
3. Navega hasta la carpeta del proyecto y selecciónala
4. Espera a que NetBeans indexe el proyecto y descargue las dependencias de Maven
5. Haz clic derecho en el proyecto → **Run**
6. La aplicación se iniciará en `http://localhost:8085`

### Opción 2: Ejecutar desde la Terminal de NetBeans

1. Abre la terminal integrada de NetBeans (View → Terminal)
2. Navega al directorio del proyecto:
   ```bash
   cd /ruta/al/proyecto/UrbanBitesApp
   ```
3. Ejecuta:
   ```bash
   mvn spring-boot:run
   ```

### Opción 3: Compilar y Ejecutar

1. En NetBeans, haz clic derecho en el proyecto → **Clean and Build**
2. Luego, **Run**

---

## 👤 Usuarios Iniciales

Después de ejecutar el script SQL, puedes iniciar sesión con los siguientes usuarios (contraseña: `1234`):

- **Cliente**: `cliente@urbanbites.com`
- **Dueño**: `dueno@urbanbites.com`
- **Administrador**: `admin@urbanbites.com`

---

## 📱 Funcionalidades Principales

### Para Clientes 👥
- ✅ Registro e inicio de sesión
- ✅ Visualización de menús de food trucks
- ✅ Agregar productos al carrito
- ✅ Canjear puntos en el carrito
- ✅ Realizar pedidos
- ✅ Ver historial de pedidos
- ✅ Dejar reseñas de pedidos entregados
- ✅ Ver saldo de puntos y movimientos
- ✅ Ver promociones activas
- ✅ Solicitar eventos (catering, delivery)
- ✅ Aceptar/rechazar cotizaciones de eventos

### Para Dueños de Food Trucks 🚚
- ✅ Panel de administración con estadísticas
- ✅ Gestión de food trucks (CRUD) con imágenes
- ✅ Gestión de productos/menú (CRUD) con imágenes
- ✅ Visualización y actualización de pedidos
- ✅ Establecer tiempo estimado (ETA)
- ✅ Ver y moderar reseñas
- ✅ Gestionar promociones
- ✅ Configurar reglas de puntos
- ✅ Gestionar horarios y ubicaciones
- ✅ Recibir y cotizar solicitudes de eventos
- ✅ Aceptar/rechazar eventos cotizados

### Para Administradores 🔐
- ✅ Dashboard con estadísticas del sistema
- ✅ Gestión de usuarios y roles
- ✅ Gestión de food trucks (CRUD)
- ✅ Gestión de horarios de food trucks
- ✅ Control completo del sistema

---

## 📂 Estructura del Proyecto

```
UrbanBitesApp/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/urbanbites/
│   │   │       ├── controller/      # Controladores MVC
│   │   │       ├── domain/         # Entidades JPA
│   │   │       ├── repository/     # Repositorios Spring Data JPA
│   │   │       ├── service/        # Lógica de negocio
│   │   │       └── UrbanBitesApplication.java
│   │   └── resources/
│   │       ├── static/
│   │       │   ├── css/            # Estilos CSS separados por módulo
│   │       │   ├── js/             # Scripts JavaScript separados
│   │       │   └── images/         # Imágenes estáticas
│   │       ├── templates/          # Plantillas Thymeleaf
│   │       │   ├── admin/         # Vistas de administrador
│   │       │   ├── owner/         # Vistas de dueño
│   │       │   ├── auth/          # Autenticación
│   │       │   ├── carrito/       # Carrito de compras
│   │       │   ├── eventos/       # Eventos (cliente)
│   │       │   ├── general/       # Fragmentos compartidos
│   │       │   └── ...           # Otras vistas
│   │       ├── firebase/          # Credenciales Firebase (no incluido en git)
│   │       ├── application.properties
│   │       └── UrbanBites.sql     # Script completo de base de datos
│   └── test/                      # Pruebas unitarias
├── pom.xml                        # Configuración Maven
├── .gitignore
└── README.md
```

---

## 🔒 Seguridad

El proyecto utiliza Spring Security para:
- Autenticación basada en formularios
- **Autorización dinámica basada en rutas desde base de datos** - Las rutas y permisos se gestionan desde la tabla `ruta`
- Autorización basada en roles (cliente, dueño, admin)
- Encriptación de contraseñas con BCrypt
- Protección CSRF en todos los formularios
- Redirección automática según rol después del login

---

## 🗄️ Base de Datos

El esquema de la base de datos incluye las siguientes entidades principales:

- `usuario` - Usuarios del sistema
- `rol` - Roles de usuario
- `usuario_rol` - Relación muchos a muchos entre usuarios y roles
- `ruta` - **Rutas y permisos dinámicos del sistema**
- `foodtrucks` - Food trucks registrados
- `productos` - Productos del menú
- `menu` - Menús de food trucks
- `fotos_productos` - Imágenes de productos
- `pedidos` - Pedidos realizados
- `detalle_pedido` - Detalles de cada pedido
- `carritos` - Carritos de compra
- `detalle_carrito` - Detalles del carrito
- `resenas` - Reseñas de clientes
- `promociones` - Promociones activas
- `puntos_cliente` - Sistema de puntos (acumulados y redimidos)
- `reglas_puntos` - Reglas de puntos por food truck
- `eventos` - Solicitudes de eventos con cotizaciones
- `horarios_foodtruck` - Horarios y ubicaciones de food trucks

**Nota**: Todas las relaciones tienen `ON DELETE CASCADE` para permitir eliminaciones en cascada.

---

## 🐛 Solución de Problemas

### Error: "Port 8085 already in use"
- Cambia el puerto en `application.properties`:
  ```properties
  server.port=8086
  ```
- O cierra la aplicación que está usando el puerto 8085

### Error: "Cannot connect to database"
- Verifica que MySQL esté corriendo
- Revisa las credenciales en `application.properties`
- Asegúrate de que la base de datos `urbanbites` exista

### Error al compilar en NetBeans
- Limpia y reconstruye el proyecto: **Clean and Build**
- Verifica que Maven haya descargado todas las dependencias
- Revisa que la versión de Java sea compatible (JDK 17+)

---

## 📝 Notas Importantes

- ⚠️ **No subir credenciales de Firebase** al repositorio (están en `.gitignore`)
- ⚠️ **No subir el archivo SQL** con datos sensibles al repositorio público
- ✅ El proyecto está configurado para desarrollo local
- ✅ Las imágenes se pueden subir a Firebase Storage o usar rutas locales
- ✅ **Rutas dinámicas**: Las rutas y permisos se gestionan desde la tabla `ruta` en la base de datos
- ✅ **Modularización**: CSS y JavaScript están separados en archivos externos
- ✅ **Fragmentos Thymeleaf**: Cada módulo tiene su propio archivo `fragmentos.html` para head y scripts
- ✅ **Mejores prácticas UX**: Modales Bootstrap para confirmaciones, filtros interactivos, estados visuales

## 🎨 Características de Diseño

- **Bootstrap 5** para componentes UI
- **Bootstrap Icons** para iconografía
- **CSS modular** - Estilos separados por funcionalidad
- **JavaScript modular** - Scripts separados por página
- **Responsive design** - Adaptable a diferentes tamaños de pantalla
- **Tema consistente** - Colores Urban Bites (#104E43 verde, #ED3101 rojo)

---
