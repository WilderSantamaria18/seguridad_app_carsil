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
    // --- AUDITORÍA / SEGURIDAD ---
    suspend fun updateUserPassword(userId: Int, newHash: String): Boolean = withContext(Dispatchers.IO) {
        var conn: Connection? = null
        try {
            conn = getConnection()
            val query = "UPDATE USUARIO SET Clave = ? WHERE IdUsuario = ?"
            val pstmt = conn.prepareStatement(query)
            pstmt.setString(1, newHash)
            pstmt.setInt(2, userId)
            return@withContext pstmt.executeUpdate() > 0
        } catch (e: Exception) {
            Log.e("RailwayDB", "Error update password: ${e.message}")
            false
        } finally { conn?.close() }
    }

    // --- CLIENTES (ESCRITURA) ---
    suspend fun insertClient(client: Map<String, Any?>): Int = withContext(Dispatchers.IO) {
        var conn: Connection? = null
        try {
            conn = getConnection()
            val query = "INSERT INTO CLIENTE (Documento, RazonSocial, Direccion, Telefono, Celular, Email, Contacto, Estado) VALUES (?, ?, ?, ?, ?, ?, ?, 1)"
            val pstmt = conn.prepareStatement(query, java.sql.Statement.RETURN_GENERATED_KEYS)
            pstmt.setString(1, client["Documento"] as? String)
            pstmt.setString(2, client["RazonSocial"] as? String)
            pstmt.setString(3, client["Direccion"] as? String)
            pstmt.setString(4, client["Telefono"] as? String)
            pstmt.setString(5, client["Celular"] as? String)
            pstmt.setString(6, client["Email"] as? String)
            pstmt.setString(7, client["Contacto"] as? String)
            pstmt.executeUpdate()
            val rs = pstmt.generatedKeys
            if (rs.next()) return@withContext rs.getInt(1)
        } catch (e: Exception) { Log.e("RailwayDB", "Error insert client: ${e.message}") }
        finally { conn?.close() }
        return@withContext -1
    }

    // --- PRODUCTOS (ESCRITURA) ---
    suspend fun insertProduct(prod: Map<String, Any?>): Int = withContext(Dispatchers.IO) {
        var conn: Connection? = null
        try {
            conn = getConnection()
            val query = "INSERT INTO PRODUCTO (Codigo, Nombre, Descripcion, Marca, Modelo, Tipo, PrecioUnitario, Stock, StockMinimo, Estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 1)"
            val pstmt = conn.prepareStatement(query, java.sql.Statement.RETURN_GENERATED_KEYS)
            pstmt.setString(1, prod["Codigo"] as? String)
            pstmt.setString(2, prod["Nombre"] as? String)
            pstmt.setString(3, prod["Descripcion"] as? String)
            pstmt.setString(4, prod["Marca"] as? String)
            pstmt.setString(5, prod["Modelo"] as? String)
            pstmt.setString(6, prod["Tipo"] as? String)
            pstmt.setDouble(7, prod["PrecioUnitario"] as Double)
            pstmt.setInt(8, prod["Stock"] as Int)
            pstmt.setInt(9, prod["StockMinimo"] as? Int ?: 5)
            pstmt.executeUpdate()
            val rs = pstmt.generatedKeys
            if (rs.next()) return@withContext rs.getInt(1)
        } catch (e: Exception) { Log.e("RailwayDB", "Error insert product: ${e.message}") }
        finally { conn?.close() }
        return@withContext -1
    }

    suspend fun updateProductStock(idProducto: Int, reduction: Int): Boolean = withContext(Dispatchers.IO) {
        var conn: Connection? = null
        try {
            conn = getConnection()
            val query = "UPDATE PRODUCTO SET Stock = Stock - ? WHERE IdProducto = ? AND Stock >= ?"
            val pstmt = conn.prepareStatement(query)
            pstmt.setInt(1, reduction)
            pstmt.setInt(2, idProducto)
            pstmt.setInt(3, reduction)
            return@withContext pstmt.executeUpdate() > 0
        } catch (e: Exception) {
            Log.e("RailwayDB", "Error update stock: ${e.message}")
            false
        } finally { conn?.close() }
    }

    // --- PROFORMAS (ESCRITURA) ---
    suspend fun insertProforma(prof: Map<String, Any?>): Int = withContext(Dispatchers.IO) {
        var conn: Connection? = null
        try {
            conn = getConnection()
            val query = """
                INSERT INTO PROFORMA (Codigo, IdUsuario, IdCliente, IdEmpresa, FechaEmision, Referencia, 
                ValidezOferta, TiempoEntrega, LugarEntrega, Garantia, FormaPago, PorcentajeIGV, 
                SubTotal, TotalIGV, Total, Estado) 
                VALUES (?, ?, ?, 1, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'PENDIENTE')
            """.trimIndent()
            val pstmt = conn.prepareStatement(query, java.sql.Statement.RETURN_GENERATED_KEYS)
            pstmt.setString(1, prof["Codigo"] as? String)
            pstmt.setInt(2, prof["IdUsuario"] as Int)
            pstmt.setInt(3, prof["IdCliente"] as Int)
            pstmt.setString(4, prof["FechaEmision"] as? String)
            pstmt.setString(5, prof["Referencia"] as? String)
            pstmt.setInt(6, prof["ValidezOferta"] as? Int ?: 10)
            pstmt.setString(7, prof["TiempoEntrega"] as? String)
            pstmt.setString(8, prof["LugarEntrega"] as? String)
            pstmt.setString(9, prof["Garantia"] as? String)
            pstmt.setString(10, prof["FormaPago"] as? String)
            pstmt.setDouble(11, prof["PorcentajeIGV"] as? Double ?: 18.0)
            pstmt.setDouble(12, prof["SubTotal"] as Double)
            pstmt.setDouble(13, prof["TotalIGV"] as Double)
            pstmt.setDouble(14, prof["Total"] as Double)
            pstmt.executeUpdate()
            val rs = pstmt.generatedKeys
            if (rs.next()) return@withContext rs.getInt(1)
        } catch (e: Exception) { Log.e("RailwayDB", "Error insert proforma: ${e.message}") }
        finally { conn?.close() }
        return@withContext -1
    }

    suspend fun insertProformaDetail(det: Map<String, Any?>): Boolean = withContext(Dispatchers.IO) {
        var conn: Connection? = null
        try {
            conn = getConnection()
            val query = "INSERT INTO DETALLE_PROFORMA (IdProforma, IdProducto, Cantidad, UnidadMedida, PrecioUnitario, Total, DescripcionAdicional) VALUES (?, ?, ?, 'UNID', ?, ?, ?)"
            val pstmt = conn.prepareStatement(query)
            pstmt.setInt(1, det["IdProforma"] as Int)
            pstmt.setInt(2, det["IdProducto"] as Int)
            pstmt.setDouble(3, det["Cantidad"] as Double)
            pstmt.setDouble(4, det["PrecioUnitario"] as Double)
            pstmt.setDouble(5, det["Total"] as Double)
            pstmt.setString(6, det["Descripcion"] as? String)
            return@withContext pstmt.executeUpdate() > 0
        } catch (e: Exception) {
            Log.e("RailwayDB", "Error insert detail: ${e.message}")
            false
        } finally { conn?.close() }
    }

    // --- ASISTENCIA ---
    suspend fun getEmpleadoByUserId(userId: Int): Int = withContext(Dispatchers.IO) {
        var conn: Connection? = null
        try {
            conn = getConnection()
            val query = "SELECT IdEmpleado FROM EMPLEADO WHERE IdUsuario = ? LIMIT 1"
            val pstmt = conn.prepareStatement(query)
            pstmt.setInt(1, userId)
            val rs = pstmt.executeQuery()
            if (rs.next()) return@withContext rs.getInt("IdEmpleado")
        } catch (e: Exception) { Log.e("RailwayDB", "Error get empleado: ${e.message}") }
        finally { conn?.close() }
        return@withContext -1
    }

    suspend fun getAsistenciaHoy(idEmpleado: Int, fecha: String): Map<String, Any?>? = withContext(Dispatchers.IO) {
        var conn: Connection? = null
        try {
            conn = getConnection()
            val query = "SELECT * FROM ASISTENCIA WHERE IdEmpleado = ? AND Fecha = ? LIMIT 1"
            val pstmt = conn.prepareStatement(query)
            pstmt.setInt(1, idEmpleado)
            pstmt.setString(2, fecha)
            val rs = pstmt.executeQuery()
            if (rs.next()) {
                return@withContext mapOf(
                    "IdAsistencia" to rs.getInt("IdAsistencia"),
                    "HoraEntrada" to rs.getString("HoraEntrada"),
                    "HoraSalida" to rs.getString("HoraSalida")
                )
            }
        } catch (e: Exception) { Log.e("RailwayDB", "Error get asistencia: ${e.message}") }
        finally { conn?.close() }
        return@withContext null
    }

    suspend fun insertAsistenciaEntry(idEmpleado: Int, fecha: String, hora: String): Boolean = withContext(Dispatchers.IO) {
        var conn: Connection? = null
        try {
            conn = getConnection()
            val query = "INSERT INTO ASISTENCIA (IdEmpleado, Fecha, HoraEntrada, JornadaLaboral, Estado, TipoAsistencia) VALUES (?, ?, ?, 'COMPLETA', 'PRESENTE', 'REGULAR')"
            val pstmt = conn.prepareStatement(query)
            pstmt.setInt(1, idEmpleado)
            pstmt.setString(2, fecha)
            pstmt.setString(3, hora)
            return@withContext pstmt.executeUpdate() > 0
        } catch (e: Exception) {
            Log.e("RailwayDB", "Error insert entry: ${e.message}")
            false
        } finally { conn?.close() }
    }

    suspend fun updateAsistenciaExit(idEmpleado: Int, fecha: String, hora: String): Boolean = withContext(Dispatchers.IO) {
        var conn: Connection? = null
        try {
            conn = getConnection()
            val query = "UPDATE ASISTENCIA SET HoraSalida = ? WHERE IdEmpleado = ? AND Fecha = ?"
            val pstmt = conn.prepareStatement(query)
            pstmt.setString(1, hora)
            pstmt.setInt(2, idEmpleado)
            pstmt.setString(3, fecha)
            return@withContext pstmt.executeUpdate() > 0
        } catch (e: Exception) {
            Log.e("RailwayDB", "Error update exit: ${e.message}")
            false
        } finally { conn?.close() }
    }
}
