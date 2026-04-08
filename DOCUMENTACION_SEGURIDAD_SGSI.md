# Documentación de Seguridad (SGSI) - App Android CARSIL

Esta aplicación ha sido diseñada siguiendo estándares de seguridad de la información para entornos móviles, integrando múltiples capas de protección contra ataques comunes y accesos no autorizados.

## 1. Autenticación y Sesión
### JSON Web Token (JWT) local
- Se utiliza un sistema de generación y validación de tokens JWT (HS256) de forma local.
- **Payload**: Incluye `userId`, `email`, `roleId`, `iat` (fecha de emisión) y `exp` (fecha de expiración).
- **Vigencia**: Los tokens tienen una duración de 8 horas.
- **Seguridad**: Se utiliza una clave de firma de grado militar almacenada en el gestor de secretos de la app.

### Bloqueo de Fuerza Bruta (Lockout Policy)
- **Umbral**: 3 intentos fallidos consecutivos.
- **Penalización**: Bloqueo automático del usuario durante 10 minutos.
- **Persistencia**: El estado de bloqueo se almacena en la tabla local `SecurityControl`.

## 2. Protección en Tiempo de Ejecución (Runtime Security)
### Detección de Integridad (Root Detection)
- La aplicación verifica rutas y propiedades del sistema en busca de binarios `su` o aplicaciones de superusuario.
- Previene que el APK se ejecute en dispositivos comprometidos donde la base de datos local SQLite sea vulnerable a extracción.

### Prevención de Fugas de Información (FLAG_SECURE)
- Se ha implementado `FLAG_SECURE` en la ventana principal.
- Bloquea capturas de pantalla, grabaciones y previsualizaciones en el gestor de tareas (multitarea).
- Protege los datos de proformas y clientes de ser capturados por malware de terceros.

## 3. Control de Acceso (RBAC)
### Roles y Permisos
- **Administrador**: Acceso total (Dashboard, Clientes, Productos, Proformas, Asistencia).
- **Supervisor**: Acceso a gestión operativa y reportes.
- **Vendedor**: Acceso a ventas y proformas.
- **Empleado**: Acceso limitado únicamente al marcado de Asistencia.
- La navegación es dinámica y se reconstruye en cada login basándose en la tabla `PERMISO`.

## 4. Endurecimiento del Artefacto (APK Hardening)
- **No-Backup Policy**: Se ha configurado `allowBackup="false"` para evitar que los datos de la base de datos local sean extraídos mediante comandos de depuración (`adb backup`).
- **Ofuscación R8/ProGuard**: Se ha habilitado la minificación y ofuscación de código para dificultar la ingeniería inversa de la lógica de seguridad y el `JwtTokenManager`.

## 5. Validación Visual
### CAPTCHA Nativo
- Antes de procesar el login, se requiere la resolución de un reto matemático dinámico.
- Protege contra scripts de automatización local que intenten vulnerar el PIN/Clave del usuario.

---
*Este documento forma parte del archivo técnico requerido para la Auditoría de Seguridad de Sistemas.*
