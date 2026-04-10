-- =============================================
-- SCRIPT COMPLETO - BASE DE DATOS DBVENTASDEMO
-- Crea tablas, inserta 10 registros por tabla,
-- procedimientos, funciones, vistas y triggers
-- =============================================

-- Crear base de datos
--CREATE DATABASE IF NOT EXISTS DBVENTASDEMO;
--USE DBVENTASDEMO;

-- =============================================
-- LIMPIEZA: Eliminar objetos existentes
-- =============================================
SET FOREIGN_KEY_CHECKS = 0;

DROP TRIGGER IF EXISTS after_factura_insert;
DROP TRIGGER IF EXISTS after_factura_update;
DROP TRIGGER IF EXISTS after_detalle_factura_insert;
DROP TRIGGER IF EXISTS after_detalle_factura_delete;
DROP VIEW IF EXISTS VistaAsistenciaSemanal;
DROP PROCEDURE IF EXISTS CalcularPagoSemanal;
DROP PROCEDURE IF EXISTS RegistrarAsistencia;
DROP FUNCTION IF EXISTS ObtenerHorasTrabajadasRango;

DROP TABLE IF EXISTS notificaciones;
DROP TABLE IF EXISTS PAGO;
DROP TABLE IF EXISTS ASISTENCIA;
DROP TABLE IF EXISTS EMPLEADO;
DROP TABLE IF EXISTS VENTA;
DROP TABLE IF EXISTS DETALLE_FACTURA;
DROP TABLE IF EXISTS FACTURA;
DROP TABLE IF EXISTS CONDICIONES_PRODUCTO;
DROP TABLE IF EXISTS DETALLE_PROFORMA;
DROP TABLE IF EXISTS PROFORMA;
DROP TABLE IF EXISTS PRODUCTO;
DROP TABLE IF EXISTS CLIENTE;
DROP TABLE IF EXISTS EMPRESA;
DROP TABLE IF EXISTS USUARIO;
DROP TABLE IF EXISTS PERMISO;
DROP TABLE IF EXISTS ROL;

SET FOREIGN_KEY_CHECKS = 1;

-- =============================================
-- 1. CREACIÓN DE TABLAS
-- =============================================

-- Tabla para roles de usuario
CREATE TABLE ROL(
    IdRol INT PRIMARY KEY AUTO_INCREMENT,
    Descripcion VARCHAR(50),
    FechaRegistro DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabla para permisos
CREATE TABLE PERMISO(
    IdPermiso INT PRIMARY KEY AUTO_INCREMENT,
    IdRol INT,
    NombreMenu VARCHAR(100),
    FechaRegistro DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (IdRol) REFERENCES ROL(IdRol)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabla para usuarios
CREATE TABLE USUARIO(
    IdUsuario INT PRIMARY KEY AUTO_INCREMENT,
    Nombres VARCHAR(50) NOT NULL,
    Apellidos VARCHAR(50) NOT NULL,
    TipoDocumento VARCHAR(20) DEFAULT 'DNI',
    NumeroDocumento VARCHAR(20) NOT NULL UNIQUE,
    Correo VARCHAR(50) UNIQUE,
    Clave VARCHAR(255) NOT NULL,
    IdRol INT NOT NULL,
    Estado TINYINT(1) DEFAULT 1,
    IntentosFallidos INT DEFAULT 0,
    UltimoIntentoFallido DATETIME NULL,
    Telefono VARCHAR(20),
    Direccion VARCHAR(200),
    FechaRegistro DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (IdRol) REFERENCES ROL(IdRol)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabla para información de la empresa
CREATE TABLE EMPRESA(
    IdEmpresa INT PRIMARY KEY AUTO_INCREMENT,
    Nombre VARCHAR(100) NOT NULL,
    RUC VARCHAR(11) NOT NULL,
    Direccion VARCHAR(200) NOT NULL,
    Telefono VARCHAR(20),
    Celular VARCHAR(20),
    Email VARCHAR(100),
    Logo LONGBLOB,
    TextoPresentacion VARCHAR(500) DEFAULT 'En atención a vuestra solicitud, tenemos el agrado de cotizarles lo siguiente:',
    CuentaBancaria VARCHAR(100),
    NombreCuentaBancaria VARCHAR(100),
    CuentaBancaria2 VARCHAR(100),
    NombreCuentaBancaria2 VARCHAR(100),
    Estado TINYINT(1) DEFAULT 1,
    FechaRegistro DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabla para clientes
CREATE TABLE CLIENTE(
    IdCliente INT PRIMARY KEY AUTO_INCREMENT,
    Documento VARCHAR(20) NOT NULL UNIQUE,
    RazonSocial VARCHAR(100) NOT NULL,
    Direccion VARCHAR(200),
    Telefono VARCHAR(20),
    Celular VARCHAR(20),
    Email VARCHAR(100),
    Contacto VARCHAR(100),
    Estado TINYINT(1) DEFAULT 1,
    FechaRegistro DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabla para productos/servicios (especializado en bombas hidráulicas)
CREATE TABLE PRODUCTO(
    IdProducto INT PRIMARY KEY AUTO_INCREMENT,
    Codigo VARCHAR(50) UNIQUE,
    Nombre VARCHAR(100) NOT NULL,
    Descripcion TEXT,
    Marca VARCHAR(100),
    Modelo VARCHAR(100),
    Tipo VARCHAR(100),
    UnidadMedida VARCHAR(20) DEFAULT 'UNID',
    PrecioUnitario DECIMAL(12,2) NOT NULL,
    Stock INT DEFAULT 0 COMMENT 'Stock actual disponible',
    StockMinimo INT DEFAULT 5 COMMENT 'Stock mínimo para alerta',
    Estado TINYINT(1) DEFAULT 1,
    FechaRegistro DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabla principal de proformas
CREATE TABLE PROFORMA(
    IdProforma INT PRIMARY KEY AUTO_INCREMENT,
    Codigo VARCHAR(20) NOT NULL,
    IdUsuario INT NOT NULL,
    IdCliente INT NOT NULL,
    IdEmpresa INT NOT NULL,
    FechaEmision DATE NOT NULL,
    Referencia VARCHAR(100),
    ValidezOferta INT DEFAULT 10,
    TiempoEntrega VARCHAR(100),
    LugarEntrega VARCHAR(200),
    Garantia VARCHAR(100),
    FormaPago VARCHAR(200),
    PorcentajeIGV DECIMAL(5,2) DEFAULT 18.00,
    SubTotal DECIMAL(12,2) NOT NULL,
    TotalIGV DECIMAL(12,2) NOT NULL,
    Total DECIMAL(12,2) NOT NULL,
    Estado VARCHAR(20) DEFAULT 'PENDIENTE',
    Observaciones TEXT,
    FechaRegistro DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (IdUsuario) REFERENCES USUARIO(IdUsuario),
    FOREIGN KEY (IdCliente) REFERENCES CLIENTE(IdCliente),
    FOREIGN KEY (IdEmpresa) REFERENCES EMPRESA(IdEmpresa)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabla de detalle de proformas
CREATE TABLE DETALLE_PROFORMA(
    IdDetalleProforma INT PRIMARY KEY AUTO_INCREMENT,
    IdProforma INT NOT NULL,
    IdProducto INT NOT NULL,
    Cantidad DECIMAL(10,2) NOT NULL,
    UnidadMedida VARCHAR(20) DEFAULT 'UNID',
    PrecioUnitario DECIMAL(12,2) NOT NULL,
    Total DECIMAL(12,2) NOT NULL,
    DescripcionAdicional TEXT,
    FechaRegistro DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (IdProforma) REFERENCES PROFORMA(IdProforma),
    FOREIGN KEY (IdProducto) REFERENCES PRODUCTO(IdProducto)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabla para condiciones específicas de productos en proformas
CREATE TABLE CONDICIONES_PRODUCTO(
    IdCondicion INT PRIMARY KEY AUTO_INCREMENT,
    IdDetalleProforma INT NOT NULL,
    NombreCondicion VARCHAR(100) NOT NULL,
    ValorCondicion VARCHAR(500) NOT NULL,
    FechaRegistro DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (IdDetalleProforma) REFERENCES DETALLE_PROFORMA(IdDetalleProforma)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabla para facturas
CREATE TABLE FACTURA(
    IdFactura INT PRIMARY KEY AUTO_INCREMENT,
    Codigo VARCHAR(20) NOT NULL,
    IdProforma INT,
    IdUsuario INT NOT NULL,
    IdCliente INT NOT NULL,
    IdEmpresa INT NOT NULL,
    FechaEmision DATE NOT NULL,
    FechaVencimiento DATE,
    SubTotal DECIMAL(12,2) NOT NULL,
    TotalIGV DECIMAL(12,2) NOT NULL,
    Total DECIMAL(12,2) NOT NULL,
    Estado VARCHAR(20) DEFAULT 'PENDIENTE',
    FormaPago VARCHAR(100),
    Observaciones TEXT,
    FechaRegistro DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (IdProforma) REFERENCES PROFORMA(IdProforma),
    FOREIGN KEY (IdUsuario) REFERENCES USUARIO(IdUsuario),
    FOREIGN KEY (IdCliente) REFERENCES CLIENTE(IdCliente),
    FOREIGN KEY (IdEmpresa) REFERENCES EMPRESA(IdEmpresa)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabla de detalle de facturas (incluye campos para relación con proforma)
CREATE TABLE DETALLE_FACTURA(
    IdDetalleFactura INT PRIMARY KEY AUTO_INCREMENT,
    IdFactura INT NOT NULL,
    IdProducto INT NOT NULL,
    Cantidad DECIMAL(10,2) NOT NULL,
    UnidadMedida VARCHAR(20) DEFAULT 'UNID',
    PrecioUnitario DECIMAL(12,2) NOT NULL,
    Total DECIMAL(12,2) NOT NULL,
    DescripcionAdicional TEXT,
    IdDetalleProforma INT NULL,
    TipoDetalle VARCHAR(20) DEFAULT 'ORIGINAL' COMMENT 'ORIGINAL (de proforma) o ADICIONAL (agregado después)',
    FechaRegistro DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (IdFactura) REFERENCES FACTURA(IdFactura),
    FOREIGN KEY (IdProducto) REFERENCES PRODUCTO(IdProducto),
    FOREIGN KEY (IdDetalleProforma) REFERENCES DETALLE_PROFORMA(IdDetalleProforma),
    INDEX idx_detalle_factura_proforma (IdDetalleProforma),
    INDEX idx_detalle_factura_tipo (TipoDetalle)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabla para ventas (resumen de facturas)
CREATE TABLE VENTA(
    IdVenta INT PRIMARY KEY AUTO_INCREMENT,
    IdFactura INT NOT NULL,
    FechaVenta DATE NOT NULL,
    Total DECIMAL(12,2) NOT NULL,
    Estado VARCHAR(20),
    FechaRegistro DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (IdFactura) REFERENCES FACTURA(IdFactura)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabla para empleados (extiende USUARIO con datos laborales)
CREATE TABLE EMPLEADO(
    IdEmpleado INT PRIMARY KEY AUTO_INCREMENT,
    IdUsuario INT NOT NULL UNIQUE,
    Cargo VARCHAR(100) NOT NULL,
    Area VARCHAR(100),
    FechaContratacion DATE NOT NULL,
    TipoContrato VARCHAR(50) DEFAULT 'INDEFINIDO',
    SueldoBase DECIMAL(12,2) NOT NULL COMMENT 'Sueldo base semanal del empleado',
    Banco VARCHAR(100),
    NumeroCuenta VARCHAR(50),
    TipoCuenta VARCHAR(30) DEFAULT 'AHORROS',
    Estado VARCHAR(20) DEFAULT 'ACTIVO',
    FechaRegistro DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (IdUsuario) REFERENCES USUARIO(IdUsuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabla para asistencias del personal
CREATE TABLE ASISTENCIA(
    IdAsistencia INT PRIMARY KEY AUTO_INCREMENT,
    IdEmpleado INT NOT NULL,
    Fecha DATE NOT NULL,
    JornadaLaboral VARCHAR(20) DEFAULT 'COMPLETA' COMMENT 'COMPLETA, MEDIO_MANANA, MEDIO_TARDE, NOCTURNO, PERSONALIZADO',
    HoraEntrada TIME,
    HoraSalida TIME,
    HorasTrabajadas DECIMAL(5,2) GENERATED ALWAYS AS (
        CASE 
            WHEN HoraEntrada IS NOT NULL AND HoraSalida IS NOT NULL 
            THEN ROUND(TIME_TO_SEC(TIMEDIFF(HoraSalida, HoraEntrada)) / 3600, 2)
            ELSE 0 
        END
    ) STORED,
    Estado VARCHAR(20) DEFAULT 'PRESENTE',
    TipoAsistencia VARCHAR(20) DEFAULT 'REGULAR' COMMENT 'REGULAR, FERIADO, EXTRA',
    Observaciones TEXT,
    FechaRegistro DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_empleado_fecha (IdEmpleado, Fecha),
    INDEX idx_asistencia_empleado_fecha (IdEmpleado, Fecha),
    FOREIGN KEY (IdEmpleado) REFERENCES EMPLEADO(IdEmpleado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabla para pagos a empleados
CREATE TABLE PAGO(
    IdPago INT PRIMARY KEY AUTO_INCREMENT,
    IdEmpleado INT NOT NULL,
    Semana INT NOT NULL,
    Anio INT NOT NULL,
    FechaInicio DATE NOT NULL,
    FechaFin DATE NOT NULL,
    HorasTrabajadas DECIMAL(10,2) NOT NULL,
    SueldoSemanal DECIMAL(12,2) NOT NULL,
    Bonificaciones DECIMAL(12,2) DEFAULT 0,
    Descuentos DECIMAL(12,2) DEFAULT 0,
    TotalPago DECIMAL(12,2) NOT NULL,
    Estado VARCHAR(20) DEFAULT 'PENDIENTE',
    FechaPago DATE,
    MetodoPago VARCHAR(50) DEFAULT 'TRANSFERENCIA',
    Comentarios TEXT,
    FechaRegistro DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_empleado_semana_anio (IdEmpleado, Semana, Anio),
    INDEX idx_pago_empleado_semana (IdEmpleado, Semana, Anio),
    FOREIGN KEY (IdEmpleado) REFERENCES EMPLEADO(IdEmpleado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabla para notificaciones
CREATE TABLE notificaciones (
    IdNotificacion INT AUTO_INCREMENT PRIMARY KEY,
    Tipo VARCHAR(20) NOT NULL COMMENT 'CREAR, ACTUALIZAR, ELIMINAR',
    Modulo VARCHAR(50) NOT NULL COMMENT 'clientes, proformas, productos, etc.',
    Mensaje VARCHAR(255) NOT NULL,
    Detalle VARCHAR(255) DEFAULT NULL,
    Usuario VARCHAR(100) DEFAULT NULL,
    Leida TINYINT(1) DEFAULT 0,
    FechaCreacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_leida (Leida),
    INDEX idx_fecha (FechaCreacion DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Índices adicionales para rendimiento
CREATE INDEX idx_empleado_estado ON EMPLEADO(Estado);




-- Tabla para auditoría de operaciones del sistema
CREATE TABLE IF NOT EXISTS AUDITORIA (
    IdAuditoria INT PRIMARY KEY AUTO_INCREMENT,
    IdUsuario INT NULL,
    NombreUsuario VARCHAR(120) NOT NULL DEFAULT 'Sistema',
    Modulo VARCHAR(50) NOT NULL,
    Accion VARCHAR(30) NOT NULL COMMENT 'CREAR | ACTUALIZAR | ELIMINAR | LOGIN | LOGOUT | ERROR',
    Descripcion TEXT NULL,
    IP VARCHAR(50) NULL,
    FechaHora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (IdUsuario) REFERENCES USUARIO(IdUsuario) ON DELETE SET NULL,
    INDEX idx_auditoria_fecha (FechaHora),
    INDEX idx_auditoria_modulo (Modulo),
    INDEX idx_auditoria_usuario (IdUsuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- =============================================
-- 2. INSERCIÓN DE DATOS (10 registros por tabla)
-- =============================================

-- -----------------------------------------------
-- 2.1 ROL (4 roles reales del sistema)
-- IdRol 1 = Administrador (acceso total - AI-009)
-- IdRol 2 = Empleado     (solo marcado de asistencia - AI-006)
-- IdRol 3 = Supervisor   (acceso operativo - AI-002, AI-003, AI-006, AI-008)
-- IdRol 4 = Vendedor     (proformas y clientes - AI-001, AI-003)
-- -----------------------------------------------
INSERT INTO ROL (Descripcion) VALUES 
('Administrador'),
('Empleado'),
('Supervisor'),
('Vendedor');

-- -----------------------------------------------
-- 2.2 PERMISO — Matriz completa por rol
-- Administrador (1): acceso total (AI-009)
-- Supervisor    (3): acceso operativo
-- Empleado      (2): solo asistencia
-- -----------------------------------------------
INSERT INTO PERMISO (IdRol, NombreMenu) VALUES
-- Administrador: todo el sistema
(1, 'Dashboard'),
(1, 'Clientes'),
(1, 'Productos'),
(1, 'Proformas'),
(1, 'Facturas'),
(1, 'Empleados'),
(1, 'Asistencia'),
(1, 'Pagos'),
(1, 'Reportes'),
(1, 'Usuarios'),
(1, 'Empresa'),
(1, 'Auditoria'),
-- Supervisor: operativo (sin Usuarios, Empresa, Auditoria)
(3, 'Dashboard'),
(3, 'Clientes'),
(3, 'Productos'),
(3, 'Proformas'),
(3, 'Facturas'),
(3, 'Empleados'),
(3, 'Asistencia'),
(3, 'Pagos'),
(3, 'Reportes'),
-- Empleado: solo marcado de asistencia
(2, 'Asistencia'),
-- Vendedor: crea proformas, consulta clientes y productos (AI-001, AI-002, AI-003)
(4, 'Dashboard'),
(4, 'Clientes'),
(4, 'Productos'),
(4, 'Proformas');

-- -----------------------------------------------
-- 2.3 USUARIO — Personal real de CARSIL
-- Contraseña por defecto: carsil2024
-- Hash bcrypt: $2b$10$qb/OBcDiKog7VNClJFXdbukdiQqFiJbXkzMKHUxcvgQihE7zVuLdS
-- IdRol: 1=Administrador | 2=Empleado | 3=Supervisor
-- -----------------------------------------------
INSERT INTO USUARIO (Nombres, Apellidos, TipoDocumento, NumeroDocumento, Correo, Clave, IdRol, Estado, Telefono, Direccion) VALUES
-- Administrador principal
('Carlos Enrique',   'Silva Romero',      'DNI', '42187634', 'csilva@carsil.com',      '$2b$10$qb/OBcDiKog7VNClJFXdbukdiQqFiJbXkzMKHUxcvgQihE7zVuLdS', 1, 1, '987321654', 'Av. Industrial 456, Ate, Lima'),
-- Supervisor de operaciones
('Rosa Amelia',      'Paredes Quispe',    'DNI', '47263819', 'rparedes@carsil.com',    '$2b$10$qb/OBcDiKog7VNClJFXdbukdiQqFiJbXkzMKHUxcvgQihE7zVuLdS', 3, 1, '986452371', 'Jr. Los Álamos 238, Ate, Lima'),
-- Supervisor comercial
('Miguel Ángel',     'Fuentes Cardenas',  'DNI', '43572810', 'mfuentes@carsil.com',   '$2b$10$qb/OBcDiKog7VNClJFXdbukdiQqFiJbXkzMKHUxcvgQihE7zVuLdS', 3, 1, '998761234', 'Calle Moquegua 112, San Luis, Lima'),
-- Empleados técnicos y administrativos
('Jean Pierre',      'Enriquez Solano',   'DNI', '72384910', 'jenriquez@carsil.com',   '$2b$10$qb/OBcDiKog7VNClJFXdbukdiQqFiJbXkzMKHUxcvgQihE7zVuLdS', 2, 1, '975638241', 'Av. Primavera 567, La Molina, Lima'),
('Lucia Fernanda',   'Torres Vargas',     'DNI', '48920371', 'ltorres@carsil.com',     '$2b$10$qb/OBcDiKog7VNClJFXdbukdiQqFiJbXkzMKHUxcvgQihE7zVuLdS', 2, 1, '962847531', 'Jr. Piura 843, San Martín de Porres, Lima'),
('Roberto Carlos',   'Mendoza Huanca',    'DNI', '41837264', 'rmendoza@carsil.com',    '$2b$10$qb/OBcDiKog7VNClJFXdbukdiQqFiJbXkzMKHUxcvgQihE7zVuLdS', 2, 1, '984732610', 'Calle Las Cucardas 321, ATE, Lima'),
('Patricia Elena',   'Villanueva Salas',  'DNI', '46391827', 'pvillanueva@carsil.com', '$2b$10$qb/OBcDiKog7VNClJFXdbukdiQqFiJbXkzMKHUxcvgQihE7zVuLdS', 2, 1, '951736482', 'Av. Angamos 760, Surquillo, Lima'),
('Diego Alonso',     'Castillo Reyes',    'DNI', '74829013', 'dcastillo@carsil.com',   '$2b$10$qb/OBcDiKog7VNClJFXdbukdiQqFiJbXkzMKHUxcvgQihE7zVuLdS', 2, 1, '978345621', 'Jr. Cusco 198, Miraflores, Lima'),
('Gabriela Inés',    'Chavez Pinedo',     'DNI', '45218736', 'gchavez@carsil.com',     '$2b$10$qb/OBcDiKog7VNClJFXdbukdiQqFiJbXkzMKHUxcvgQihE7zVuLdS', 2, 1, '963524178', 'Calle Los Sauces 54, Chorrillos, Lima'),
('Josue Ezequiel',   'Suarez Navarrete',  'DNI', '73648291', 'jsuarez@carsil.com',     '$2b$10$qb/OBcDiKog7VNClJFXdbukdiQqFiJbXkzMKHUxcvgQihE7zVuLdS', 2, 1, '954182763', 'Av. Benavides 1342, Surco, Lima');

-- -----------------------------------------------
-- 2.4 EMPRESA — Datos reales de CARSIL (registro único)
-- -----------------------------------------------
INSERT INTO EMPRESA (Nombre, RUC, Direccion, Telefono, Celular, Email, CuentaBancaria, NombreCuentaBancaria, CuentaBancaria2, NombreCuentaBancaria2, TextoPresentacion) VALUES
('CARSIL Bombas Hidráulicas S.A.C.', '20601234571', 'Av. Industrial 456, Zona Industrial, Ate, Lima', '014567890', '987321654', 'ventas@carsil.com.pe', '19100459031095', 'CARSIL S.A.C. - Cta. Corriente BCP', '00113622940100', 'CARSIL S.A.C. - BBVA', 'En atención a su amable solicitud, tenemos el agrado de presentarles la siguiente cotización de equipos hidráulicos y servicios especializados:');

-- -----------------------------------------------
-- 2.5 CLIENTE (10 registros)
-- -----------------------------------------------
INSERT INTO CLIENTE (Documento, RazonSocial, Direccion, Telefono, Celular, Email, Contacto) VALUES
('20501234567', 'Constructora Los Andes S.A.C.', 'Av. Javier Prado 1234, San Isidro, Lima', '016543210', '912345678', 'compras@losandes.com', 'Roberto Sanchez'),
('20602345678', 'Minera El Dorado S.A.', 'Carretera Central Km 45, Junin', '064543211', '912345679', 'logistica@eldorado.com', 'Fernando Quispe'),
('20703456789', 'Agroindustrial del Pacifico S.A.C.', 'Fundo La Victoria, Ica', '056543212', '912345680', 'compras@agropacifico.com', 'Patricia Gomez'),
('20804567890', 'Municipalidad de Miraflores', 'Av. Larco 400, Miraflores, Lima', '016543213', '912345681', 'obras@munimiraflores.gob.pe', 'Carlos Medina'),
('20905678901', 'Hotel Costa del Sol S.A.', 'Malecón Cisneros 1244, Miraflores, Lima', '016543214', '912345682', 'mantenimiento@costadelsol.com', 'Lucia Fernandez'),
('20106789012', 'Textil San Cristobal S.A.C.', 'Zona Industrial, Av. Argentina 3456, Lima', '016543215', '912345683', 'planta@sancristobal.com', 'Miguel Torres'),
('20207890123', 'Pesquera del Norte S.A.', 'Puerto Salaverry, Trujillo', '044543216', '912345684', 'operaciones@pesqueranorte.com', 'Andrea Lopez'),
('20308901234', 'Cerveceria Artesanal del Sur E.I.R.L.', 'Jr. Industria 567, Arequipa', '054543217', '912345685', 'produccion@cerveartesur.com', 'Diego Ramirez'),
('20409012345', 'Condominio Las Palmeras', 'Av. El Sol 890, Surco, Lima', '016543218', '912345686', 'administracion@laspalmeras.com', 'Gabriela Vargas'),
('20510123456', 'Universidad Nacional de Ingenieria', 'Av. Tupac Amaru 210, Rimac, Lima', '016543219', '912345687', 'laboratorio@uni.edu.pe', 'Profesor Huaman');

-- -----------------------------------------------
-- 2.6 PRODUCTO (10 registros)
-- -----------------------------------------------
INSERT INTO PRODUCTO (Codigo, Nombre, Descripcion, Marca, Modelo, Tipo, UnidadMedida, PrecioUnitario, Stock, StockMinimo) VALUES
('BOM-SUM-001', 'Bomba Sumergible 1HP', 'Bomba sumergible para pozo profundo, acero inoxidable, 1HP, 220V', 'Pedrollo', '4SR1m/13', 'Sumergible', 'UNID', 850.00, 8, 5),
('BOM-SUM-002', 'Bomba Sumergible 2HP', 'Bomba sumergible para aguas limpias, 2HP, 380V, caudal 5m3/h', 'Grundfos', 'SP 5-50', 'Sumergible', 'UNID', 1200.00, 3, 5),
('BOM-SUM-003', 'Bomba Sumergible 3HP', 'Bomba sumergible para aguas residuales con solidos hasta 50mm', 'Ebara', 'Drainage DVS', 'Sumergible', 'UNID', 1800.00, 0, 3),
('ELE-001', 'Electrobomba Centrifuga 1HP', 'Electrobomba horizontal monofasica, 1HP, caudal 1.5m3/h', 'Pedrollo', 'PKm 60', 'Centrifuga', 'UNID', 480.00, 12, 5),
('ELE-002', 'Electrobomba Autocebante 2HP', 'Electrobomba autocebante para riego, 2HP, 380V', 'Lowara', 'EASY', 'Autocebante', 'UNID', 750.00, 2, 4),
('ELE-003', 'Electrobomba Presurizadora 0.75HP', 'Electrobomba para sistema de presion constante, 0.75HP', 'Grundfos', 'SCALA2', 'Presurizadora', 'UNID', 920.00, 6, 3),
('BOM-SUP-001', 'Bomba Superficie 1HP', 'Bomba de superficie para riego, 1HP, caudal 3m3/h', 'Pedrollo', 'CPm 158', 'Superficie', 'UNID', 420.00, 15, 5),
('BOM-RES-001', 'Bomba Aguas Residuales 2HP', 'Bomba sumergible para aguas residuales con triturador', 'Franklin', 'SEWAGE', 'Residuales', 'UNID', 2100.00, 1, 3),
('SIS-BOM-001', 'Sistema Hidroneumatico 500L', 'Tanque hidroneumatico + bomba 2HP + controles, capacidad 500L', 'Grundfos', 'Hydro 500', 'Sistema', 'UNID', 2800.00, 4, 2),
('ACC-CON-002', 'Controlador de Bomba', 'Controlador electronico para bomba con proteccion por sequia', 'Franklin', 'Control Pro', 'Accesorio', 'UNID', 120.00, 0, 10);

-- -----------------------------------------------
-- 2.7 PROFORMA (10 registros)
-- -----------------------------------------------
INSERT INTO PROFORMA (Codigo, IdUsuario, IdCliente, IdEmpresa, FechaEmision, Referencia, ValidezOferta, TiempoEntrega, LugarEntrega, Garantia, FormaPago, PorcentajeIGV, SubTotal, TotalIGV, Total, Estado, Observaciones) VALUES
('PRO-2026-001', 1, 1, 1, '2026-04-01', 'Cotización bombas para obra', 15, '5 días hábiles', 'Obra San Isidro', '12 meses', 'Contado', 18.00, 1940.00, 349.20, 2289.20, 'APROBADA', 'Cliente requiere entrega urgente'),
('PRO-2026-002', 1, 2, 1, '2026-04-02', 'Sistema de bombeo para minería', 10, '10 días hábiles', 'Campamento minero Junín', '24 meses', '50% adelanto, 50% contra entrega', 18.00, 5700.00, 1026.00, 6726.00, 'APROBADA', 'Incluye instalación'),
('PRO-2026-003', 4, 3, 1, '2026-04-03', 'Electrobombas para riego', 10, '7 días hábiles', 'Fundo Ica', '12 meses', 'Crédito 30 días', 18.00, 1500.00, 270.00, 1770.00, 'PENDIENTE', NULL),
('PRO-2026-004', 1, 4, 1, '2026-03-20', 'Bombas para parques', 15, '15 días hábiles', 'Parque Kennedy', '12 meses', 'Contado', 18.00, 3600.00, 648.00, 4248.00, 'APROBADA', 'Proyecto municipal'),
('PRO-2026-005', 8, 5, 1, '2026-03-15', 'Sistema presurización hotel', 10, '8 días hábiles', 'Hotel Costa del Sol', '24 meses', '30% adelanto', 18.00, 4640.00, 835.20, 5475.20, 'PENDIENTE', NULL),
('PRO-2026-006', 1, 6, 1, '2026-03-10', 'Bombas industriales textil', 10, '12 días hábiles', 'Planta San Cristobal', '12 meses', 'Crédito 45 días', 18.00, 2400.00, 432.00, 2832.00, 'APROBADA', 'Requiere certificado de calidad'),
('PRO-2026-007', 4, 7, 1, '2026-02-15', 'Bombeo para pesquera', 15, '10 días hábiles', 'Puerto Salaverry', '18 meses', '50% adelanto', 18.00, 3600.00, 648.00, 4248.00, 'RECHAZADA', 'Cliente optó por otra cotización'),
('PRO-2026-008', 8, 8, 1, '2026-02-10', 'Bomba para cervecería', 10, '5 días hábiles', 'Planta Arequipa', '12 meses', 'Contado', 18.00, 920.00, 165.60, 1085.60, 'APROBADA', NULL),
('PRO-2026-009', 1, 9, 1, '2026-01-15', 'Sistema hidroneumático condominio', 10, '15 días hábiles', 'Condominio Surco', '24 meses', 'Crédito 60 días', 18.00, 2800.00, 504.00, 3304.00, 'APROBADA', 'Incluye capacitación'),
('PRO-2026-010', 4, 10, 1, '2026-01-05', 'Equipos laboratorio UNI', 15, '20 días hábiles', 'Lab. Hidráulica UNI', '12 meses', 'Orden de compra', 18.00, 1700.00, 306.00, 2006.00, 'PENDIENTE', 'Sujeto a presupuesto universitario');

-- -----------------------------------------------
-- 2.8 DETALLE_PROFORMA (10 registros)
-- -----------------------------------------------
INSERT INTO DETALLE_PROFORMA (IdProforma, IdProducto, Cantidad, UnidadMedida, PrecioUnitario, Total, DescripcionAdicional) VALUES
(1, 1, 2.00, 'UNID', 850.00, 1700.00, 'Bombas para pozo de 30m de profundidad'),
(1, 10, 2.00, 'UNID', 120.00, 240.00, 'Controladores para las bombas sumergibles'),
(2, 3, 2.00, 'UNID', 1800.00, 3600.00, 'Bombas para drenaje de mina'),
(2, 8, 1.00, 'UNID', 2100.00, 2100.00, 'Bomba de aguas residuales del campamento'),
(3, 5, 2.00, 'UNID', 750.00, 1500.00, 'Electrobombas para sistema de riego por goteo'),
(4, 3, 2.00, 'UNID', 1800.00, 3600.00, 'Bombas sumergibles para fuentes ornamentales'),
(5, 6, 2.00, 'UNID', 920.00, 1840.00, 'Presurizadoras para pisos superiores'),
(5, 9, 1.00, 'UNID', 2800.00, 2800.00, 'Sistema hidroneumático para lobby'),
(6, 2, 2.00, 'UNID', 1200.00, 2400.00, 'Bombas para proceso de teñido'),
(7, 3, 2.00, 'UNID', 1800.00, 3600.00, 'Bombas para descarga de embarcaciones'),
(8, 6, 1.00, 'UNID', 920.00, 920.00, 'Bomba para embotellado'),
(9, 9, 1.00, 'UNID', 2800.00, 2800.00, 'Hidroneumático 500L'),
(10, 1, 2.00, 'UNID', 850.00, 1700.00, 'Equipos para pruebas de laboratorio');

-- -----------------------------------------------
-- 2.9 CONDICIONES_PRODUCTO (10 registros)
-- -----------------------------------------------
INSERT INTO CONDICIONES_PRODUCTO (IdDetalleProforma, NombreCondicion, ValorCondicion) VALUES
(1, 'Voltaje', '220V monofásico'),
(1, 'Garantía extendida', '24 meses con mantenimiento preventivo incluido'),
(2, 'Compatibilidad', 'Compatible con bombas Pedrollo serie 4SR'),
(3, 'Material carcasa', 'Acero inoxidable AISI 304'),
(4, 'Certificación', 'ISO 9001 - Apta para uso en minería'),
(5, 'Presión máxima', '6 bar - Apto para riego tecnificado'),
(6, 'Instalación', 'Incluye instalación y puesta en marcha'),
(7, 'Nivel de ruido', 'Menor a 55 dB - Apto para uso en hoteles'),
(8, 'Capacidad tanque', '500 litros - Acero galvanizado'),
(9, 'Temperatura máxima', 'Hasta 60°C - Apto para proceso textil');

-- -----------------------------------------------
-- 2.10 FACTURA (10 registros) - SIN triggers aún
-- -----------------------------------------------
INSERT INTO FACTURA (Codigo, IdProforma, IdUsuario, IdCliente, IdEmpresa, FechaEmision, FechaVencimiento, SubTotal, TotalIGV, Total, Estado, FormaPago, Observaciones) VALUES
('FAC-2026-001', 1, 1, 1, 1, '2026-04-01', '2026-05-01', 1940.00, 349.20, 2289.20, 'PAGADA', 'Contado', 'Pago al contado en oficina'),
('FAC-2026-002', 2, 1, 2, 1, '2026-04-02', '2026-05-02', 5700.00, 1026.00, 6726.00, 'PAGADA', 'Transferencia', 'Primer pago 50% recibido'),
('FAC-2026-003', 4, 1, 4, 1, '2026-03-20', '2026-04-19', 3600.00, 648.00, 4248.00, 'PENDIENTE', 'Crédito 30 días', 'Factura municipal'),
('FAC-2026-004', 6, 1, 6, 1, '2026-03-10', '2026-04-09', 2400.00, 432.00, 2832.00, 'PENDIENTE', 'Crédito 45 días', NULL),
('FAC-2026-005', 8, 8, 8, 1, '2026-02-10', '2026-03-12', 920.00, 165.60, 1085.60, 'PAGADA', 'Contado', 'Pago inmediato'),
('FAC-2026-006', 9, 1, 9, 1, '2026-01-15', '2026-03-15', 2800.00, 504.00, 3304.00, 'PENDIENTE', 'Crédito 60 días', 'Incluye instalación'),
('FAC-2026-007', NULL, 1, 1, 1, '2026-04-15', '2026-05-15', 480.00, 86.40, 566.40, 'PAGADA', 'Contado', 'Venta directa sin proforma'),
('FAC-2026-008', NULL, 4, 3, 1, '2026-04-20', '2026-05-20', 1500.00, 270.00, 1770.00, 'PENDIENTE', 'Crédito 30 días', 'Venta directa'),
('FAC-2026-009', NULL, 1, 5, 1, '2026-05-01', '2026-05-31', 2100.00, 378.00, 2478.00, 'ANULADA', 'Transferencia', 'Factura anulada por error en datos'),
('FAC-2026-010', NULL, 8, 10, 1, '2026-05-10', '2026-06-09', 850.00, 153.00, 1003.00, 'PENDIENTE', 'Orden de compra', 'Pendiente orden de compra UNI');

-- -----------------------------------------------
-- 2.11 DETALLE_FACTURA (10 registros)
-- -----------------------------------------------
INSERT INTO DETALLE_FACTURA (IdFactura, IdProducto, Cantidad, UnidadMedida, PrecioUnitario, Total, DescripcionAdicional, IdDetalleProforma, TipoDetalle) VALUES
(1, 1, 2.00, 'UNID', 850.00, 1700.00, 'Bombas sumergibles 1HP para obra', 1, 'ORIGINAL'),
(1, 10, 2.00, 'UNID', 120.00, 240.00, 'Controladores electrónicos', 2, 'ORIGINAL'),
(2, 3, 2.00, 'UNID', 1800.00, 3600.00, 'Bombas 3HP para mina', 3, 'ORIGINAL'),
(2, 8, 1.00, 'UNID', 2100.00, 2100.00, 'Bomba aguas residuales', 4, 'ORIGINAL'),
(3, 3, 2.00, 'UNID', 1800.00, 3600.00, 'Bombas para fuentes', 6, 'ORIGINAL'),
(4, 2, 2.00, 'UNID', 1200.00, 2400.00, 'Bombas para teñido', 9, 'ORIGINAL'),
(5, 6, 1.00, 'UNID', 920.00, 920.00, 'Presurizadora cervecería', 11, 'ORIGINAL'),
(6, 9, 1.00, 'UNID', 2800.00, 2800.00, 'Sistema hidroneumático', 12, 'ORIGINAL'),
(7, 4, 1.00, 'UNID', 480.00, 480.00, 'Electrobomba venta directa', NULL, 'ADICIONAL'),
(8, 5, 2.00, 'UNID', 750.00, 1500.00, 'Electrobombas riego', NULL, 'ADICIONAL'),
(10, 1, 1.00, 'UNID', 850.00, 850.00, 'Bomba laboratorio', 13, 'ORIGINAL');

-- -----------------------------------------------
-- 2.12 VENTA (10 registros - vinculados a facturas)
-- -----------------------------------------------
INSERT INTO VENTA (IdFactura, FechaVenta, Total, Estado) VALUES
(1, '2026-04-01', 2289.20, 'COMPLETADA'),
(2, '2026-04-02', 6726.00, 'COMPLETADA'),
(3, '2026-03-20', 4248.00, 'PENDIENTE'),
(4, '2026-03-10', 2832.00, 'PENDIENTE'),
(5, '2026-02-10', 1085.60, 'COMPLETADA'),
(6, '2026-01-15', 3304.00, 'PENDIENTE'),
(7, '2026-04-15', 566.40, 'COMPLETADA'),
(8, '2026-04-20', 1770.00, 'PENDIENTE'),
(9, '2026-05-01', 2478.00, 'ANULADA'),
(10, '2026-05-10', 1003.00, 'PENDIENTE');

-- -----------------------------------------------
-- 2.14 EMPLEADO — Personal real de CARSIL
-- SueldoBase = sueldo base SEMANAL (en soles)
-- -----------------------------------------------
INSERT INTO EMPLEADO (IdUsuario, Cargo, Area, FechaContratacion, TipoContrato, SueldoBase, Banco, NumeroCuenta, TipoCuenta) VALUES
(1,  'Administrador del Sistema',      'Administración',  '2022-03-01', 'INDEFINIDO',  500.00, 'BCP',        '19100459031095', 'CORRIENTE'),
(2,  'Supervisor de Operaciones',      'Operaciones',     '2022-06-15', 'INDEFINIDO',  420.00, 'BBVA',       '00113622940100', 'CORRIENTE'),
(3,  'Supervisor Comercial',           'Ventas',          '2023-01-10', 'INDEFINIDO',  400.00, 'Scotiabank', '00019387641200', 'CORRIENTE'),
(4,  'Técnico en Bombas Hidráulicas', 'Mantenimiento',   '2023-04-01', 'INDEFINIDO',  280.00, 'BCP',        '19104381762095', 'AHORROS'),
(5,  'Asistente Administrativa',       'Administración',  '2023-07-01', 'INDEFINIDO',  250.00, 'Interbank',  '00083726419100', 'AHORROS'),
(6,  'Operario de Instalaciones',      'Instalaciones',   '2023-09-15', 'INDEFINIDO',  240.00, 'BCP',        '19107361842095', 'AHORROS'),
(7,  'Secretaria Ejecutiva',           'Administración',  '2024-01-08', 'INDEFINIDO',  245.00, 'BBVA',       '00114832760100', 'AHORROS'),
(8,  'Técnico Electricista',           'Mantenimiento',   '2024-03-01', 'PLAZO FIJO',  260.00, 'Interbank',  '00087261349100', 'AHORROS'),
(9,  'Vendedora Comercial',            'Ventas',          '2024-05-06', 'PLAZO FIJO',  230.00, 'BCP',        '19108274631095', 'AHORROS'),
(10, 'Técnico de Almacén',             'Logística',       '2024-08-01', 'PLAZO FIJO',  220.00, 'Scotiabank', '00019384762100', 'AHORROS');

-- -----------------------------------------------
-- 2.15 ASISTENCIA (10 registros - columna HorasTrabajadas es GENERATED, no se incluye)
-- -----------------------------------------------
INSERT INTO ASISTENCIA (IdEmpleado, Fecha, JornadaLaboral, HoraEntrada, HoraSalida, Estado, TipoAsistencia, Observaciones) VALUES
(1, '2025-02-24', 'COMPLETA', '08:00:00', '17:00:00', 'PRESENTE', 'REGULAR', NULL),
(2, '2025-02-24', 'COMPLETA', '08:15:00', '17:00:00', 'PRESENTE', 'REGULAR', NULL),
(3, '2025-02-24', 'COMPLETA', '08:00:00', '17:30:00', 'PRESENTE', 'REGULAR', 'Horas extra por instalación urgente'),
(4, '2025-02-24', 'COMPLETA', '08:45:00', '17:00:00', 'TARDANZA', 'REGULAR', 'Tardanza por tráfico'),
(5, '2025-02-24', 'COMPLETA', '08:00:00', '17:00:00', 'PRESENTE', 'REGULAR', NULL),
(1, '2025-02-25', 'COMPLETA', '08:00:00', '17:00:00', 'PRESENTE', 'REGULAR', NULL),
(2, '2025-02-25', 'COMPLETA', '08:00:00', '17:00:00', 'PRESENTE', 'REGULAR', NULL),
(3, '2025-02-25', 'MEDIO_MANANA', '08:00:00', '12:00:00', 'PRESENTE', 'REGULAR', 'Media jornada por cita médica'),
(6, '2025-02-24', 'COMPLETA', '08:00:00', '17:00:00', 'PRESENTE', 'REGULAR', NULL),
(7, '2025-02-24', 'COMPLETA', '09:00:00', '17:00:00', 'TARDANZA', 'REGULAR', 'Llegó tarde');

-- -----------------------------------------------
-- 2.16 PAGO (10 registros - combinación única IdEmpleado+Semana+Anio)
-- -----------------------------------------------
INSERT INTO PAGO (IdEmpleado, Semana, Anio, FechaInicio, FechaFin, HorasTrabajadas, SueldoSemanal, Bonificaciones, Descuentos, TotalPago, Estado, FechaPago, MetodoPago, Comentarios) VALUES
(1, 8, 2025, '2025-02-17', '2025-02-23', 43.00, 500.00, 0.00, 0.00, 500.00, 'PAGADO', '2025-02-23', 'TRANSFERENCIA', 'Semana completa'),
(2, 8, 2025, '2025-02-17', '2025-02-23', 43.00, 280.00, 20.00, 0.00, 300.00, 'PAGADO', '2025-02-23', 'TRANSFERENCIA', 'Bonificación por rendimiento'),
(3, 8, 2025, '2025-02-17', '2025-02-23', 40.00, 222.79, 0.00, 10.00, 212.79, 'PAGADO', '2025-02-23', 'TRANSFERENCIA', 'Descuento por préstamo'),
(4, 8, 2025, '2025-02-17', '2025-02-23', 43.00, 350.00, 50.00, 0.00, 400.00, 'PAGADO', '2025-02-23', 'TRANSFERENCIA', 'Bonificación por supervisión'),
(5, 8, 2025, '2025-02-17', '2025-02-23', 43.00, 450.00, 0.00, 0.00, 450.00, 'PAGADO', '2025-02-23', 'TRANSFERENCIA', 'Semana completa'),
(6, 8, 2025, '2025-02-17', '2025-02-23', 43.00, 320.00, 0.00, 0.00, 320.00, 'PENDIENTE', NULL, 'TRANSFERENCIA', 'Pendiente de aprobación'),
(7, 8, 2025, '2025-02-17', '2025-02-23', 38.00, 230.23, 0.00, 15.00, 215.23, 'PENDIENTE', NULL, 'TRANSFERENCIA', 'Descuento por tardanzas'),
(8, 8, 2025, '2025-02-17', '2025-02-23', 43.00, 300.00, 30.00, 0.00, 330.00, 'PAGADO', '2025-02-23', 'TRANSFERENCIA', 'Bonificación por ventas'),
(9, 8, 2025, '2025-02-17', '2025-02-23', 43.00, 220.00, 0.00, 0.00, 220.00, 'PENDIENTE', NULL, 'EFECTIVO', 'Pago en efectivo'),
(10, 8, 2025, '2025-02-17', '2025-02-23', 43.00, 250.00, 0.00, 0.00, 250.00, 'PAGADO', '2025-02-23', 'TRANSFERENCIA', 'Semana completa');

-- -----------------------------------------------
-- 2.17 NOTIFICACIONES — Eventos reales del sistema
-- -----------------------------------------------
INSERT INTO notificaciones (Tipo, Modulo, Mensaje, Detalle, Usuario, Leida) VALUES
('CREAR',      'clientes',   'Se registró un nuevo cliente',          'Cliente: Constructora Los Andes S.A.C.',                  'Carlos Enrique Silva Romero',   1),
('CREAR',      'proformas',  'Se creó una nueva proforma',            'Proforma: PRO-2025-001 por S/ 2,419.00',                  'Carlos Enrique Silva Romero',   1),
('ACTUALIZAR', 'proformas',  'Se aprobó una proforma',                'Proforma: PRO-2025-001 cambió a APROBADA',                'Carlos Enrique Silva Romero',   1),
('CREAR',      'facturas',   'Se generó una nueva factura',           'Factura: FAC-2025-001 por S/ 2,419.00',                   'Carlos Enrique Silva Romero',   1),
('CREAR',      'productos',  'Se registró un nuevo producto',         'Producto: Bomba Sumergible 1HP Pedrollo 4SR1m/13',        'Carlos Enrique Silva Romero',   0),
('ACTUALIZAR', 'facturas',   'Se actualizó estado de factura',        'Factura: FAC-2025-001 cambió a PAGADA',                   'Carlos Enrique Silva Romero',   0),
('CREAR',      'empleados',  'Se registró un nuevo empleado',         'Empleado: Jean Pierre Enriquez Solano - Técnico',         'Carlos Enrique Silva Romero',   0),
('CREAR',      'asistencia', 'Nuevo registro de asistencia',          'Empleado: Lucia Fernanda Torres Vargas marcó entrada',    'Rosa Amelia Paredes Quispe',    0),
('ACTUALIZAR', 'empleados',  'Se actualizó información de empleado',  'Empleado: Roberto Carlos Mendoza Huanca - Cambio de área','Rosa Amelia Paredes Quispe',    0);

-- -----------------------------------------------
-- 2.18 AUDITORIA — Historial de operaciones (AI-010)
-- -----------------------------------------------
INSERT INTO AUDITORIA (IdUsuario, NombreUsuario, Modulo, Accion, Descripcion, IP) VALUES
(1, 'Carlos Enrique Silva Romero',  'SISTEMA',    'LOGIN',      'Administrador inició sesión en el sistema',                              '192.168.1.10'),
(1, 'Carlos Enrique Silva Romero',  'CLIENTE',    'CREAR',      'Se registró el cliente Constructora Los Andes S.A.C. (RUC 20501234567)', '192.168.1.10'),
(1, 'Carlos Enrique Silva Romero',  'PRODUCTO',   'CREAR',      'Se registró el producto Bomba Sumergible 1HP - Pedrollo 4SR1m/13',       '192.168.1.10'),
(2, 'Rosa Amelia Paredes Quispe',   'PROFORMA',   'CREAR',      'Se creó la proforma PRO-2025-001 por S/ 2,419.00',                       '192.168.1.11'),
(2, 'Rosa Amelia Paredes Quispe',   'PROFORMA',   'ACTUALIZAR', 'Proforma PRO-2025-001 cambió de PENDIENTE a APROBADA',                   '192.168.1.11'),
(1, 'Carlos Enrique Silva Romero',  'FACTURA',    'CREAR',      'Se generó la factura FAC-2025-001 por S/ 2,419.00',                      '192.168.1.10'),
(1, 'Carlos Enrique Silva Romero',  'FACTURA',    'ACTUALIZAR', 'Factura FAC-2025-001 cambió de PENDIENTE a PAGADA',                      '192.168.1.10'),
(1, 'Carlos Enrique Silva Romero',  'EMPLEADO',   'CREAR',      'Se registró el empleado Jean Pierre Enriquez Solano',                    '192.168.1.10'),
(3, 'Miguel Ángel Fuentes Cardenas','ASISTENCIA', 'ACTUALIZAR', 'Se cerró registros de asistencia pendientes del mes anterior',           '192.168.1.12'),
(1, 'Carlos Enrique Silva Romero',  'USUARIO',    'CREAR',      'Se creó el usuario jsuarez@carsil.com con rol Empleado',                  '192.168.1.10');



-- =============================================
-- 3. PROCEDIMIENTOS ALMACENADOS
-- =============================================

-- -----------------------------------------------
-- 3.1 Procedimiento: CalcularPagoSemanal
-- -----------------------------------------------
DELIMITER //
CREATE PROCEDURE CalcularPagoSemanal(
    IN p_IdEmpleado INT,
    IN p_Semana INT,
    IN p_Anio INT,
    IN p_FechaInicio DATE,
    IN p_FechaFin DATE,
    IN p_Bonificaciones DECIMAL(12,2),
    IN p_Descuentos DECIMAL(12,2)
)
BEGIN
    DECLARE v_HorasTrabajadas DECIMAL(10,2) DEFAULT 0;
    DECLARE v_SueldoBase DECIMAL(12,2) DEFAULT 0;
    DECLARE v_SueldoCalculado DECIMAL(12,2) DEFAULT 0;
    DECLARE v_TotalPago DECIMAL(12,2) DEFAULT 0;
    DECLARE v_HorasSemanaNormal DECIMAL(5,2) DEFAULT 43;
    DECLARE v_ValorPorHora DECIMAL(10,4) DEFAULT 0;
    
    -- Manejar valores NULL para bonificaciones y descuentos
    IF p_Bonificaciones IS NULL THEN
        SET p_Bonificaciones = 0;
    END IF;
    
    IF p_Descuentos IS NULL THEN
        SET p_Descuentos = 0;
    END IF;
    
    -- Calcular total de horas trabajadas del empleado en el rango de fechas
    SELECT COALESCE(SUM(HorasTrabajadas), 0) INTO v_HorasTrabajadas
    FROM ASISTENCIA
    WHERE IdEmpleado = p_IdEmpleado
      AND Fecha BETWEEN p_FechaInicio AND p_FechaFin
      AND Estado IN ('PRESENTE', 'TARDANZA');
    
    -- Obtener sueldo base semanal del empleado
    SELECT COALESCE(SueldoBase, 0) INTO v_SueldoBase
    FROM EMPLEADO
    WHERE IdEmpleado = p_IdEmpleado;
    
    -- Calcular valor por hora y sueldo proporcional
    IF v_SueldoBase > 0 THEN
        SET v_ValorPorHora = v_SueldoBase / v_HorasSemanaNormal;
        SET v_SueldoCalculado = ROUND(v_ValorPorHora * v_HorasTrabajadas, 2);
    END IF;
    
    -- Calcular el total a pagar
    SET v_TotalPago = v_SueldoCalculado + p_Bonificaciones - p_Descuentos;
    
    -- Insertar o actualizar el registro de pago
    INSERT INTO PAGO(
        IdEmpleado, Semana, Anio, FechaInicio, FechaFin,
        HorasTrabajadas, SueldoSemanal, Bonificaciones,
        Descuentos, TotalPago, Estado
    )
    VALUES(
        p_IdEmpleado, p_Semana, p_Anio, p_FechaInicio, p_FechaFin,
        v_HorasTrabajadas, v_SueldoCalculado, p_Bonificaciones,
        p_Descuentos, v_TotalPago, 'PENDIENTE'
    )
    ON DUPLICATE KEY UPDATE
        HorasTrabajadas = v_HorasTrabajadas,
        SueldoSemanal = v_SueldoCalculado,
        Bonificaciones = p_Bonificaciones,
        Descuentos = p_Descuentos,
        TotalPago = v_TotalPago;
        
    -- Retornar los valores calculados
    SELECT 
        v_HorasTrabajadas AS HorasCalculadas, 
        v_SueldoCalculado AS SueldoCalculado, 
        v_TotalPago AS TotalCalculado;
END //
DELIMITER ;

-- -----------------------------------------------
-- 3.2 Procedimiento: RegistrarAsistencia
-- -----------------------------------------------
DELIMITER //
CREATE PROCEDURE RegistrarAsistencia(
    IN p_IdEmpleado INT,
    IN p_Fecha DATE,
    IN p_JornadaLaboral VARCHAR(20),
    IN p_HoraEntrada TIME,
    IN p_HoraSalida TIME,
    IN p_TipoAsistencia VARCHAR(20),
    IN p_Observaciones TEXT
)
BEGIN
    DECLARE v_Estado VARCHAR(20) DEFAULT 'PRESENTE';
    
    -- Manejar valores NULL
    IF p_TipoAsistencia IS NULL THEN
        SET p_TipoAsistencia = 'REGULAR';
    END IF;
    
    IF p_JornadaLaboral IS NULL THEN
        SET p_JornadaLaboral = 'COMPLETA';
    END IF;
    
    -- Determinar el estado basándose en la jornada y hora de entrada
    IF p_HoraEntrada IS NULL THEN
        SET v_Estado = 'AUSENTE';
    ELSE
        CASE p_JornadaLaboral
            WHEN 'COMPLETA' THEN
                IF TIME(p_HoraEntrada) > '08:30:00' THEN
                    SET v_Estado = 'TARDANZA';
                END IF;
            WHEN 'MEDIO_MANANA' THEN
                IF TIME(p_HoraEntrada) > '08:30:00' THEN
                    SET v_Estado = 'TARDANZA';
                END IF;
            WHEN 'MEDIO_TARDE' THEN
                IF TIME(p_HoraEntrada) > '13:30:00' THEN
                    SET v_Estado = 'TARDANZA';
                END IF;
            WHEN 'NOCTURNO' THEN
                IF TIME(p_HoraEntrada) > '22:30:00' THEN
                    SET v_Estado = 'TARDANZA';
                END IF;
            ELSE
                SET v_Estado = 'PRESENTE';
        END CASE;
    END IF;
    
    INSERT INTO ASISTENCIA(
        IdEmpleado, Fecha, JornadaLaboral,
        HoraEntrada, HoraSalida, TipoAsistencia,
        Estado, Observaciones
    )
    VALUES(
        p_IdEmpleado, p_Fecha, p_JornadaLaboral,
        p_HoraEntrada, p_HoraSalida, p_TipoAsistencia,
        v_Estado, p_Observaciones
    )
    ON DUPLICATE KEY UPDATE
        JornadaLaboral = p_JornadaLaboral,
        HoraEntrada = p_HoraEntrada,
        HoraSalida = p_HoraSalida,
        TipoAsistencia = p_TipoAsistencia,
        Estado = v_Estado,
        Observaciones = p_Observaciones;
END //
DELIMITER ;

-- =============================================
-- 4. FUNCIÓN
-- =============================================

DELIMITER //
CREATE FUNCTION ObtenerHorasTrabajadasRango(
    p_IdEmpleado INT,
    p_FechaInicio DATE,
    p_FechaFin DATE
) RETURNS DECIMAL(10,2)
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE v_TotalHoras DECIMAL(10,2) DEFAULT 0;
    
    SELECT COALESCE(SUM(HorasTrabajadas), 0) INTO v_TotalHoras
    FROM ASISTENCIA
    WHERE IdEmpleado = p_IdEmpleado
      AND Fecha BETWEEN p_FechaInicio AND p_FechaFin
      AND Estado IN ('PRESENTE', 'TARDANZA');
    
    RETURN v_TotalHoras;
END //
DELIMITER ;

-- =============================================
-- 5. VISTA
-- =============================================

CREATE VIEW VistaAsistenciaSemanal AS
SELECT 
    e.IdEmpleado,
    CONCAT(u.Nombres, ' ', u.Apellidos) AS NombreEmpleado,
    YEAR(a.Fecha) AS Anio,
    WEEK(a.Fecha, 1) AS Semana,
    DATE(DATE_SUB(a.Fecha, INTERVAL WEEKDAY(a.Fecha) DAY)) AS FechaInicioSemana,
    DATE(DATE_ADD(DATE_SUB(a.Fecha, INTERVAL WEEKDAY(a.Fecha) DAY), INTERVAL 6 DAY)) AS FechaFinSemana,
    COUNT(CASE WHEN a.Estado = 'PRESENTE' THEN 1 END) AS DiasPresente,
    COUNT(CASE WHEN a.Estado = 'TARDANZA' THEN 1 END) AS DiasTardanza,
    COUNT(CASE WHEN a.Estado = 'AUSENTE' THEN 1 END) AS DiasAusente,
    COALESCE(SUM(a.HorasTrabajadas), 0) AS TotalHorasTrabajadas,
    e.SueldoBase
FROM EMPLEADO e
INNER JOIN USUARIO u ON e.IdUsuario = u.IdUsuario
LEFT JOIN ASISTENCIA a ON e.IdEmpleado = a.IdEmpleado
WHERE e.Estado = 'ACTIVO'
GROUP BY e.IdEmpleado, YEAR(a.Fecha), WEEK(a.Fecha, 1), 
         u.Nombres, u.Apellidos, e.SueldoBase;

-- =============================================
-- 6. TRIGGERS (creados después de los datos)
-- =============================================

DELIMITER //

-- Trigger: Al insertar una factura, crear registro en VENTA
CREATE TRIGGER after_factura_insert
AFTER INSERT ON FACTURA
FOR EACH ROW
BEGIN
    IF NEW.Estado = 'PAGADA' THEN
        INSERT INTO VENTA (IdFactura, FechaVenta, Total, Estado)
        VALUES (NEW.IdFactura, NEW.FechaEmision, NEW.Total, 'COMPLETADA');
    ELSE
        INSERT INTO VENTA (IdFactura, FechaVenta, Total, Estado)
        VALUES (NEW.IdFactura, NEW.FechaEmision, NEW.Total, NEW.Estado);
    END IF;
END//

-- Trigger: Al actualizar una factura, actualizar registro en VENTA
CREATE TRIGGER after_factura_update
AFTER UPDATE ON FACTURA
FOR EACH ROW
BEGIN
    DECLARE venta_count INT;
    
    IF NEW.Estado = 'PAGADA' AND OLD.Estado != 'PAGADA' THEN
        SELECT COUNT(*) INTO venta_count FROM VENTA WHERE IdFactura = NEW.IdFactura;
        
        IF venta_count > 0 THEN
            UPDATE VENTA 
            SET Estado = 'COMPLETADA', FechaVenta = NEW.FechaEmision, Total = NEW.Total
            WHERE IdFactura = NEW.IdFactura;
        ELSE
            INSERT INTO VENTA (IdFactura, FechaVenta, Total, Estado)
            VALUES (NEW.IdFactura, NEW.FechaEmision, NEW.Total, 'COMPLETADA');
        END IF;
    ELSE
        UPDATE VENTA 
        SET Estado = NEW.Estado, Total = NEW.Total
        WHERE IdFactura = NEW.IdFactura;
    END IF;
END//

-- Trigger: Al insertar un detalle de factura, descontar stock del producto
CREATE TRIGGER after_detalle_factura_insert
AFTER INSERT ON DETALLE_FACTURA
FOR EACH ROW
BEGIN
    UPDATE PRODUCTO 
    SET Stock = GREATEST(Stock - CAST(NEW.Cantidad AS SIGNED), 0)
    WHERE IdProducto = NEW.IdProducto;
END//

-- Trigger: Al eliminar un detalle de factura, restaurar stock del producto
CREATE TRIGGER after_detalle_factura_delete
AFTER DELETE ON DETALLE_FACTURA
FOR EACH ROW
BEGIN
    UPDATE PRODUCTO 
    SET Stock = Stock + CAST(OLD.Cantidad AS SIGNED)
    WHERE IdProducto = OLD.IdProducto;
END//

DELIMITER ;

-- =============================================
-- FIN DEL SCRIPT
-- =============================================

-- Verificar la creación de tablas
SELECT 'TABLAS CREADAS' AS Resultado, COUNT(*) AS Total 
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'DBVENTASDEMO';

-- Verificar registros insertados
SELECT 'ROL' AS Tabla, COUNT(*) AS Registros FROM ROL
UNION ALL SELECT 'PERMISO', COUNT(*) FROM PERMISO
UNION ALL SELECT 'USUARIO', COUNT(*) FROM USUARIO
UNION ALL SELECT 'EMPRESA', COUNT(*) FROM EMPRESA
UNION ALL SELECT 'CLIENTE', COUNT(*) FROM CLIENTE
UNION ALL SELECT 'PRODUCTO', COUNT(*) FROM PRODUCTO
UNION ALL SELECT 'PROFORMA', COUNT(*) FROM PROFORMA
UNION ALL SELECT 'DETALLE_PROFORMA', COUNT(*) FROM DETALLE_PROFORMA
UNION ALL SELECT 'CONDICIONES_PRODUCTO', COUNT(*) FROM CONDICIONES_PRODUCTO
UNION ALL SELECT 'FACTURA', COUNT(*) FROM FACTURA
UNION ALL SELECT 'DETALLE_FACTURA', COUNT(*) FROM DETALLE_FACTURA
UNION ALL SELECT 'VENTA', COUNT(*) FROM VENTA
UNION ALL SELECT 'EMPLEADO', COUNT(*) FROM EMPLEADO
UNION ALL SELECT 'ASISTENCIA', COUNT(*) FROM ASISTENCIA
UNION ALL SELECT 'PAGO', COUNT(*) FROM PAGO
UNION ALL SELECT 'notificaciones', COUNT(*) FROM notificaciones
UNION ALL SELECT 'auditoria', COUNT(*) FROM auditoria;
