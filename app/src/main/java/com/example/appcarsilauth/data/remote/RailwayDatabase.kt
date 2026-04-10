package com.example.appcarsilauth.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.util.Properties

object RailwayDatabase {
    private const val HOST = "mainline.proxy.rlwy.net"
    private const val PORT = "29947"
    private const val DATABASE = "railway"
    private const val USER = "root"
    private const val PASS = "WEuNqnwDTYuIcBzOHuMNSOuBISKsTjEC"
    
    private const val URL = "jdbc:mysql://$HOST:$PORT/$DATABASE?useSSL=false&autoReconnect=true"

    init {
        try {
            Class.forName("com.mysql.jdbc.Driver")
        } catch (e: Throwable) {
            Log.e("RailwayDatabase", "Error driver: ${e.message}")
        }
    }

    private suspend fun getConnection(): Connection = withContext(Dispatchers.IO) {
        val props = Properties()
        props.setProperty("user", USER)
        props.setProperty("password", PASS)
        DriverManager.getConnection(URL, props)
    }

    suspend fun getUserByEmail(email: String): Map<String, Any>? = withContext(Dispatchers.IO) {
        var conn: Connection? = null
        try {
            conn = getConnection()
            val query = "SELECT u.*, r.Descripcion as RolNombre FROM USUARIO u " +
                        "INNER JOIN ROL r ON u.IdRol = r.IdRol " +
                        "WHERE u.Correo = ? AND u.Estado = 1 LIMIT 1"
            val pstmt = conn.prepareStatement(query)
            pstmt.setString(1, email)
            val rs: ResultSet = pstmt.executeQuery()
            if (rs.next()) {
                val userMap = mutableMapOf<String, Any>()
                userMap["IdUsuario"] = rs.getInt("IdUsuario")
                userMap["Nombres"] = rs.getString("Nombres")
                userMap["Apellidos"] = rs.getString("Apellidos")
                userMap["Correo"] = rs.getString("Correo")
                userMap["Clave"] = rs.getString("Clave") 
                userMap["Rol"] = rs.getString("RolNombre")
                return@withContext userMap
            }
        } catch (e: Throwable) { throw e } finally { try { conn?.close() } catch (e: Exception) {} }
        return@withContext null
    }

    // --- CLIENTES (AMPLIADO) ---
    suspend fun getClients(search: String = ""): List<Map<String, Any>> = withContext(Dispatchers.IO) {
        var conn: Connection? = null
        val list = mutableListOf<Map<String, Any>>()
        try {
            conn = getConnection()
            val query = if (search.isEmpty()) "SELECT * FROM CLIENTE ORDER BY RazonSocial"
                        else "SELECT * FROM CLIENTE WHERE RazonSocial LIKE ? OR Documento LIKE ? ORDER BY RazonSocial"
            val pstmt = conn.prepareStatement(query)
            if (search.isNotEmpty()) {
                pstmt.setString(1, "%$search%")
                pstmt.setString(2, "%$search%")
            }
            val rs = pstmt.executeQuery()
            while (rs.next()) {
                list.add(mapOf(
                    "IdCliente" to rs.getInt("IdCliente"),
                    "Documento" to rs.getString("Documento"),
                    "RazonSocial" to rs.getString("RazonSocial"),
                    "Direccion" to (rs.getString("Direccion") ?: ""),
                    "Telefono" to (rs.getString("Telefono") ?: ""),
                    "Celular" to (rs.getString("Celular") ?: ""),
                    "Email" to (rs.getString("Email") ?: ""),
                    "NombreContacto" to (rs.getString("Contacto") ?: "")
                ))
            }
        } catch (e: Exception) { Log.e("RailwayDB", "Error clientes: ${e.message}") }
        finally { conn?.close() }
        return@withContext list
    }

    // --- PRODUCTOS ---
    suspend fun getProducts(search: String = ""): List<Map<String, Any>> = withContext(Dispatchers.IO) {
        var conn: Connection? = null
        val list = mutableListOf<Map<String, Any>>()
        try {
            conn = getConnection()
            val query = if (search.isEmpty()) "SELECT * FROM PRODUCTO ORDER BY Nombre"
                        else "SELECT * FROM PRODUCTO WHERE Nombre LIKE ? OR Codigo LIKE ? ORDER BY Nombre"
            val pstmt = conn.prepareStatement(query)
            if (search.isNotEmpty()) {
                pstmt.setString(1, "%$search%")
                pstmt.setString(2, "%$search%")
            }
            val rs = pstmt.executeQuery()
            while (rs.next()) {
                list.add(mapOf(
                    "IdProducto" to rs.getInt("IdProducto"),
                    "Codigo" to rs.getString("Codigo"),
                    "Nombre" to rs.getString("Nombre"),
                    "Marca" to (rs.getString("Marca") ?: ""),
                    "PrecioUnitario" to rs.getDouble("PrecioUnitario"),
                    "Stock" to rs.getInt("Stock")
                ))
            }
        } catch (e: Exception) { Log.e("RailwayDB", "Error productos: ${e.message}") }
        finally { conn?.close() }
        return@withContext list
    }

    // --- PROFORMAS ---
    suspend fun getProformas(search: String = ""): List<Map<String, Any>> = withContext(Dispatchers.IO) {
        var conn: Connection? = null
        val list = mutableListOf<Map<String, Any>>()
        try {
            conn = getConnection()
            val query = """
                SELECT p.*, c.RazonSocial as ClienteNombre 
                FROM PROFORMA p 
                INNER JOIN CLIENTE c ON p.IdCliente = c.IdCliente 
                WHERE p.Codigo LIKE ? OR c.RazonSocial LIKE ? 
                ORDER BY p.FechaRegistro DESC
            """.trimIndent()
            val pstmt = conn.prepareStatement(query)
            pstmt.setString(1, "%$search%")
            pstmt.setString(2, "%$search%")
            val rs = pstmt.executeQuery()
            while (rs.next()) {
                list.add(mapOf(
                    "IdProforma" to rs.getInt("IdProforma"),
                    "Codigo" to rs.getString("Codigo"),
                    "Cliente" to rs.getString("ClienteNombre"),
                    "Total" to rs.getDouble("Total"),
                    "Estado" to rs.getString("Estado"),
                    "Fecha" to rs.getString("FechaEmision")
                ))
            }
        } catch (e: Exception) { Log.e("RailwayDB", "Error proformas: ${e.message}") }
        finally { conn?.close() }
        return@withContext list
    }
}
