package com.example.appcarsilauth.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ROL")
data class RolEntity(
    @PrimaryKey
    val IdRol: Int,
    val Descripcion: String
)

@Entity(tableName = "USUARIO")
data class UsuarioEntity(
    @PrimaryKey
    val IdUsuario: Int,
    val Nombres: String,
    val Correo: String,
    val Clave: String,
    val IdRol: Int
)

@Entity(tableName = "CLIENTE")
data class ClienteEntity(
    @PrimaryKey
    val IdCliente: Int,
    val Documento: String,
    val RazonSocial: String
)

@Entity(tableName = "PRODUCTO")
data class ProductoEntity(
    @PrimaryKey
    val IdProducto: Int,
    val Nombre: String,
    val PrecioUnitario: Double,
    val Stock: Int
)

@Entity(tableName = "PROFORMA")
data class ProformaEntity(
    @PrimaryKey(autoGenerate = true)
    val IdProforma: Int = 0,
    val Codigo: String,
    val IdUsuario: Int,
    val IdCliente: Int,
    val Total: Double
)

@Entity(tableName = "EMPLEADO")
data class EmpleadoEntity(
    @PrimaryKey
    val IdEmpleado: Int,
    val IdUsuario: Int,
    val Cargo: String,
    val Area: String
)

@Entity(tableName = "ASISTENCIA")
data class AsistenciaEntity(
    @PrimaryKey(autoGenerate = true)
    val IdAsistencia: Int = 0,
    val IdEmpleado: Int,
    val Fecha: String,
    val HoraEntrada: String?,
    val HoraSalida: String?
)
