# Implementacion Android CARSIL - Paso a Paso

Fecha: 2026-04-08

## Alcance aplicado en esta iteracion (Paso 1)
Se implementaron solo los modulos solicitados y alineados a base de datos local:
- Seguridad de acceso (login, bloqueo, captcha, JWT)
- Proformas e inventario (con detalle de proforma y descuento de stock)
- Asistencia
- Roles y permisos (RBAC desde tabla local)

## Cambios tecnicos del Paso 1

### 1. Base de datos local alineada a modulos requeridos
Se ampliaron entidades Room para reflejar estructura operativa del sistema web en alcance movil:
- ROL
- PERMISO
- USUARIO
- EMPRESA
- CLIENTE
- PRODUCTO
- PROFORMA
- DETALLE_PROFORMA
- EMPLEADO
- ASISTENCIA

Notas:
- Se mantuvo enfoque local SQLite con Room.
- Se mantuvo flujo existente de app y se agregaron campos relevantes de negocio.

### 2. Seeder local coherente con el flujo actual
Se actualizo la precarga de datos con:
- Roles
- Permisos por rol
- Empresa principal
- Usuarios de prueba operativos
- Clientes
- Productos
- Empleados

### 3. Seguridad de acceso mejorada
- Se elimino bypass de demo en autenticacion.
- Se unifico el identificador usado para lockout (email normalizado con trim).
- Se reinician intentos fallidos despues de login exitoso.
- Se expone estado de autenticacion como StateFlow para estabilidad de UI.

### 4. RBAC real desde base local
- Dashboard y botones de modulos ahora respetan la tabla PERMISO.
- Al iniciar sesion se cargan menus permitidos por rol.
- Al cerrar sesion se limpia estado de sesion en ViewModel.

### 5. Proformas + inventario
- Al crear proforma se guardan campos completos de cabecera (subtotal, IGV, total, etc.).
- Se inserta DETALLE_PROFORMA por item.
- Se reduce stock de producto en transaccion de flujo de negocio actual.

### 6. Asistencia
- Se mantiene marcado de entrada/salida.
- Se registran campos de jornada, estado y tipo de asistencia.

### 7. PDF de proforma
- Se corrigio uso de campos de cliente para no romper con el modelo local.

## Validacion
Se validaron errores de los archivos modificados con analisis estatico del entorno y no se reportaron errores.

## Alcance aplicado en esta iteracion (Paso 2)
Se implementaron los modulos faltantes solicitados con diseno minimalista y logica sobre base local:
- Modulo de Clientes (registro + listado)
- Modulo de Productos (registro + listado)
- Navegacion por permisos para Clientes y Productos

## Cambios tecnicos del Paso 2

### 1. DAO y logica para CRUD operativo minimo
- Insercion individual de cliente y producto
- Obtencion de maximo IdCliente e IdProducto para IDs locales
- Reuso de carga global de datos para refresco inmediato de UI

### 2. Modulo Clientes minimalista
- Formulario basado en campos de tabla CLIENTE:
  - Documento
  - RazonSocial
  - Direccion
  - Telefono
  - Celular
  - Email
  - Contacto
  - Estado
- Listado compacto para validacion de registros

### 3. Modulo Productos minimalista
- Formulario basado en campos de tabla PRODUCTO:
  - Codigo
  - Nombre
  - Descripcion
  - Marca
  - Modelo
  - Tipo
  - PrecioUnitario
  - Stock
  - StockMinimo
  - Estado
- Listado compacto con datos clave de inventario

### 4. Navegacion y permisos
- Se agregaron rutas CLIENTS y PRODUCTS en MainActivity
- Dashboard muestra botones por permisos reales de tabla PERMISO

## Siguiente paso recomendado (Paso 3)
- Endurecimiento adicional SGSI:
  - Cerrar backup/extraccion de datos en manifest
  - Secretos JWT fuera de codigo fuente
  - Migraciones no destructivas (eliminar fallback destructivo)
  - Tests de seguridad (lockout, captcha, permisos por rol)
