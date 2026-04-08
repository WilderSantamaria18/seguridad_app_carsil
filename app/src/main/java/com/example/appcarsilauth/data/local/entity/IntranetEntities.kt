package com.example.appcarsilauth.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ROL")
data class RolEntity(
    @PrimaryKey
    val IdRol: Int,
    val Descripcion: String
)

@Entity(tableName = "PERMISO")
data class PermisoEntity(
    @PrimaryKey(autoGenerate = true)
    val IdPermiso: Int = 0,
    val IdRol: Int,
    val NombreMenu: String
)

@Entity(tableName = "USUARIO")
data class UsuarioEntity(
    @PrimaryKey
    val IdUsuario: Int,
    val Nombres: String,
    val Apellidos: String = "",
    val TipoDocumento: String = "DNI",
    val NumeroDocumento: String = "",
    val Correo: String,
    val Clave: String,
    val IdRol: Int,
    val Estado: Int = 1,
    val IntentosFallidos: Int = 0,
    val UltimoIntentoFallido: String? = null,
    val Telefono: String? = null,
    val Direccion: String? = null
)

@Entity(tableName = "EMPRESA")
data class EmpresaEntity(
    @PrimaryKey
    val IdEmpresa: Int,
    val Nombre: String,
    val RUC: String,
    val Direccion: String,
    val Telefono: String? = null,
    val Celular: String? = null,
    val Email: String? = null
)

@Entity(tableName = "CLIENTE")
data class ClienteEntity(
    @PrimaryKey
    val IdCliente: Int,
    val Documento: String,
    val RazonSocial: String,
    val Direccion: String? = null,
    val Telefono: String? = null,
    val Celular: String? = null,
    val Email: String? = null,
    val Contacto: String? = null,
    val Estado: Int = 1
)

@Entity(tableName = "PRODUCTO")
data class ProductoEntity(
    @PrimaryKey
    val IdProducto: Int,
    val Codigo: String,
    val Nombre: String,
    val Descripcion: String? = null,
    val Marca: String? = null,
    val Modelo: String? = null,
    val Tipo: String? = null,
    val UnidadMedida: String = "UNID",
    val PrecioUnitario: Double,
    val Stock: Int,
    val StockMinimo: Int = 5,
    val Estado: Int = 1
)

@Entity(tableName = "PROFORMA")
data class ProformaEntity(
    @PrimaryKey(autoGenerate = true)
    val IdProforma: Int = 0,
    val Codigo: String,
    val IdUsuario: Int,
    val IdCliente: Int,
    val IdEmpresa: Int = 1,
    val FechaEmision: String,
    val Referencia: String? = null,
    val ValidezOferta: Int = 10,
    val TiempoEntrega: String? = null,
    val LugarEntrega: String? = null,
    val Garantia: String? = null,
    val FormaPago: String? = null,
    val PorcentajeIGV: Double = 18.0,
    val SubTotal: Double,
    val TotalIGV: Double,
    val Total: Double,
    val Estado: String = "PENDIENTE",
    val Observaciones: String? = null
)

@Entity(tableName = "DETALLE_PROFORMA")
data class DetalleProformaEntity(
    @PrimaryKey(autoGenerate = true)
    val IdDetalleProforma: Int = 0,
    val IdProforma: Int,
    val IdProducto: Int,
    val Cantidad: Double,
    val UnidadMedida: String = "UNID",
    val PrecioUnitario: Double,
    val Total: Double,
    val DescripcionAdicional: String? = null
)

@Entity(tableName = "EMPLEADO")
data class EmpleadoEntity(
    @PrimaryKey
    val IdEmpleado: Int,
    val IdUsuario: Int,
    val Cargo: String,
    val Area: String,
    val FechaContratacion: String = "2026-01-01",
    val TipoContrato: String = "INDEFINIDO",
    val SueldoBase: Double = 0.0,
    val Banco: String? = null,
    val NumeroCuenta: String? = null,
    val TipoCuenta: String = "AHORROS",
    val Estado: String = "ACTIVO"
)

@Entity(tableName = "ASISTENCIA")
data class AsistenciaEntity(
    @PrimaryKey(autoGenerate = true)
    val IdAsistencia: Int = 0,
    val IdEmpleado: Int,
    val Fecha: String,
    val JornadaLaboral: String = "COMPLETA",
    val HoraEntrada: String?,
    val HoraSalida: String?,
    val Estado: String = "PRESENTE",
    val TipoAsistencia: String = "REGULAR",
    val Observaciones: String? = null
)
