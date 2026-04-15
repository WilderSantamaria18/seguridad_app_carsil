package com.example.appcarsilauth.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.util.Calendar
import java.util.Locale
import java.util.Properties

data class ProformaActivitySeries(
    val labels: List<String>,
    val values: List<Int>
)

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
                userMap["Estado"] = rs.getInt("Estado")
                return@withContext userMap
            }
        } catch (e: Throwable) { throw e } finally { try { conn?.close() } catch (e: Exception) {} }
        return@withContext null
    }

    /**
     * Busca al usuario por correo SIN filtrar por Estado.
     * Retorna el mapa con campo "Estado" (1=activo, 0=inactivo).
     * Se usa para distinguir entre usuario inexistente vs usuario inactivo.
     */
    suspend fun getUserByEmailAnyStatus(email: String): Map<String, Any>? = withContext(Dispatchers.IO) {
        var conn: Connection? = null
        try {
            conn = getConnection()
            val query = "SELECT u.IdUsuario, u.Nombres, u.Apellidos, u.Correo, u.Estado, u.IdRol " +
                        "FROM USUARIO u WHERE u.Correo = ? LIMIT 1"
            val pstmt = conn.prepareStatement(query)
            pstmt.setString(1, email)
            val rs: ResultSet = pstmt.executeQuery()
            if (rs.next()) {
                return@withContext mapOf(
                    "IdUsuario" to rs.getInt("IdUsuario"),
                    "Nombres"   to rs.getString("Nombres"),
                    "Apellidos" to rs.getString("Apellidos"),
                    "Correo"    to rs.getString("Correo"),
                    "Estado"    to rs.getInt("Estado"),
                    "IdRol"     to rs.getInt("IdRol")
                )
            }
        } catch (e: Throwable) { throw e } finally { try { conn?.close() } catch (e: Exception) {} }
        return@withContext null
    }

    // --- USUARIOS / ROLES ---
    suspend fun getRoles(): List<Map<String, Any>> = withContext(Dispatchers.IO) {
        var conn: Connection? = null
        val roles = mutableListOf<Map<String, Any>>()
        try {
            conn = getConnection()
            val query = "SELECT IdRol, Descripcion FROM ROL ORDER BY IdRol ASC"
            val pstmt = conn.prepareStatement(query)
            val rs = pstmt.executeQuery()
            while (rs.next()) {
                roles.add(
                    mapOf(
                        "IdRol" to rs.getInt("IdRol"),
                        "Descripcion" to rs.getString("Descripcion")
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("RailwayDB", "Error roles: ${e.message}")
        } finally { conn?.close() }
        return@withContext roles
    }

    suspend fun getUsers(search: String = "", estadoFiltro: String = "TODOS"): List<Map<String, Any>> = withContext(Dispatchers.IO) {
        var conn: Connection? = null
        val users = mutableListOf<Map<String, Any>>()
        try {
            conn = getConnection()

            val estadoCondition = when (estadoFiltro.uppercase()) {
                "ACTIVOS" -> " AND u.Estado = 1"
                "INACTIVOS" -> " AND u.Estado = 0"
                else -> ""
            }

            val query = """
                SELECT
                    u.IdUsuario,
                    u.Nombres,
                    u.Apellidos,
                    u.TipoDocumento,
                    u.NumeroDocumento,
                    COALESCE(u.Correo, '') AS Correo,
                    COALESCE(u.Telefono, '') AS Telefono,
                    COALESCE(u.Direccion, '') AS Direccion,
                    u.IdRol,
                    u.Estado,
                    r.Descripcion AS RolNombre
                FROM USUARIO u
                INNER JOIN ROL r ON u.IdRol = r.IdRol
                WHERE (
                    u.Nombres LIKE ? OR
                    u.Apellidos LIKE ? OR
                    u.NumeroDocumento LIKE ? OR
                    u.Correo LIKE ? OR
                    r.Descripcion LIKE ?
                )$estadoCondition
                ORDER BY u.IdUsuario DESC
            """.trimIndent()

            val likeSearch = "%${search.trim()}%"
            val pstmt = conn.prepareStatement(query)
            pstmt.setString(1, likeSearch)
            pstmt.setString(2, likeSearch)
            pstmt.setString(3, likeSearch)
            pstmt.setString(4, likeSearch)
            pstmt.setString(5, likeSearch)

            val rs = pstmt.executeQuery()
            while (rs.next()) {
                users.add(
                    mapOf(
                        "IdUsuario" to rs.getInt("IdUsuario"),
                        "Nombres" to rs.getString("Nombres"),
                        "Apellidos" to rs.getString("Apellidos"),
                        "TipoDocumento" to (rs.getString("TipoDocumento") ?: "DNI"),
                        "NumeroDocumento" to rs.getString("NumeroDocumento"),
                        "Correo" to (rs.getString("Correo") ?: ""),
                        "Telefono" to (rs.getString("Telefono") ?: ""),
                        "Direccion" to (rs.getString("Direccion") ?: ""),
                        "IdRol" to rs.getInt("IdRol"),
                        "RolNombre" to rs.getString("RolNombre"),
                        "Estado" to rs.getInt("Estado")
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("RailwayDB", "Error usuarios: ${e.message}")
        } finally { conn?.close() }
        return@withContext users
    }

    suspend fun isUserDocumentInUse(numeroDocumento: String, excludeId: Int? = null): Boolean = withContext(Dispatchers.IO) {
        var conn: Connection? = null
        try {
            conn = getConnection()
            val query = if (excludeId != null) {
                "SELECT IdUsuario FROM USUARIO WHERE NumeroDocumento = ? AND IdUsuario <> ? LIMIT 1"
            } else {
                "SELECT IdUsuario FROM USUARIO WHERE NumeroDocumento = ? LIMIT 1"
            }
            val pstmt = conn.prepareStatement(query)
            pstmt.setString(1, numeroDocumento.trim())
            if (excludeId != null) {
                pstmt.setInt(2, excludeId)
            }
            val rs = pstmt.executeQuery()
            return@withContext rs.next()
        } catch (e: Exception) {
            Log.e("RailwayDB", "Error validar documento usuario: ${e.message}")
            false
        } finally { conn?.close() }
    }

    suspend fun isUserEmailInUse(correo: String, excludeId: Int? = null): Boolean = withContext(Dispatchers.IO) {
        var conn: Connection? = null
        try {
            conn = getConnection()
            val query = if (excludeId != null) {
                "SELECT IdUsuario FROM USUARIO WHERE Correo = ? AND IdUsuario <> ? LIMIT 1"
            } else {
                "SELECT IdUsuario FROM USUARIO WHERE Correo = ? LIMIT 1"
            }
            val pstmt = conn.prepareStatement(query)
            pstmt.setString(1, correo.trim())
            if (excludeId != null) {
                pstmt.setInt(2, excludeId)
            }
            val rs = pstmt.executeQuery()
            return@withContext rs.next()
        } catch (e: Exception) {
            Log.e("RailwayDB", "Error validar correo usuario: ${e.message}")
            false
        } finally { conn?.close() }
    }

    suspend fun insertUser(user: Map<String, Any?>): Int = withContext(Dispatchers.IO) {
        var conn: Connection? = null
        try {
            conn = getConnection()
            val query = """
                INSERT INTO USUARIO (
                    Nombres,
                    Apellidos,
                    TipoDocumento,
                    NumeroDocumento,
                    Correo,
                    Clave,
                    IdRol,
                    Estado,
                    Telefono,
                    Direccion,
                    FechaRegistro
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())
            """.trimIndent()

            val pstmt = conn.prepareStatement(query, java.sql.Statement.RETURN_GENERATED_KEYS)
            pstmt.setString(1, user["Nombres"] as? String)
            pstmt.setString(2, user["Apellidos"] as? String)
            pstmt.setString(3, user["TipoDocumento"] as? String ?: "DNI")
            pstmt.setString(4, user["NumeroDocumento"] as? String)
            pstmt.setString(5, user["Correo"] as? String)
            pstmt.setString(6, user["Clave"] as? String)
            pstmt.setInt(7, user["IdRol"] as Int)
            pstmt.setInt(8, user["Estado"] as? Int ?: 1)
            pstmt.setString(9, user["Telefono"] as? String)
            pstmt.setString(10, user["Direccion"] as? String)
            pstmt.executeUpdate()

            val rs = pstmt.generatedKeys
            if (rs.next()) return@withContext rs.getInt(1)
        } catch (e: Exception) {
            Log.e("RailwayDB", "Error insert usuario: ${e.message}")
        } finally { conn?.close() }
        return@withContext -1
    }

    suspend fun updateUser(
        idUsuario: Int,
        nombres: String,
        apellidos: String,
        tipoDocumento: String,
        numeroDocumento: String,
        correo: String,
        telefono: String,
        direccion: String,
        idRol: Int,
        estado: Int,
        newPasswordHash: String?
    ): Boolean = withContext(Dispatchers.IO) {
        var conn: Connection? = null
        try {
            conn = getConnection()
            val query = if (newPasswordHash != null) {
                """
                    UPDATE USUARIO
                    SET Nombres = ?, Apellidos = ?, TipoDocumento = ?, NumeroDocumento = ?, Correo = ?,
                        Telefono = ?, Direccion = ?, IdRol = ?, Estado = ?, Clave = ?
                    WHERE IdUsuario = ?
                """.trimIndent()
            } else {
                """
                    UPDATE USUARIO
                    SET Nombres = ?, Apellidos = ?, TipoDocumento = ?, NumeroDocumento = ?, Correo = ?,
                        Telefono = ?, Direccion = ?, IdRol = ?, Estado = ?
                    WHERE IdUsuario = ?
                """.trimIndent()
            }

            val pstmt = conn.prepareStatement(query)
            pstmt.setString(1, nombres)
            pstmt.setString(2, apellidos)
            pstmt.setString(3, tipoDocumento)
            pstmt.setString(4, numeroDocumento)
            pstmt.setString(5, correo)
            pstmt.setString(6, telefono)
            pstmt.setString(7, direccion)
            pstmt.setInt(8, idRol)
            pstmt.setInt(9, estado)

            if (newPasswordHash != null) {
                pstmt.setString(10, newPasswordHash)
                pstmt.setInt(11, idUsuario)
            } else {
                pstmt.setInt(10, idUsuario)
            }

            return@withContext pstmt.executeUpdate() > 0
        } catch (e: Exception) {
            Log.e("RailwayDB", "Error update usuario: ${e.message}")
            false
        } finally { conn?.close() }
    }

    suspend fun updateUserStatus(idUsuario: Int, estado: Int): Boolean = withContext(Dispatchers.IO) {
        var conn: Connection? = null
        try {
            conn = getConnection()
            val query = """
                UPDATE USUARIO
                SET Estado = ?, IntentosFallidos = 0, UltimoIntentoFallido = NULL
                WHERE IdUsuario = ?
            """.trimIndent()
            val pstmt = conn.prepareStatement(query)
            pstmt.setInt(1, estado)
            pstmt.setInt(2, idUsuario)
            return@withContext pstmt.executeUpdate() > 0
        } catch (e: Exception) {
            Log.e("RailwayDB", "Error estado usuario: ${e.message}")
            false
        } finally { conn?.close() }
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
                WHERE (p.Codigo LIKE ? OR c.RazonSocial LIKE ?)
                AND p.IdProforma IN (SELECT MAX(IdProforma) FROM PROFORMA GROUP BY Codigo)
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
    
    suspend fun getProformaById(idProf: Int): Map<String, Any>? = withContext(Dispatchers.IO) {
        var conn: Connection? = null
        try {
            conn = getConnection()
            val query = """
                SELECT p.*, c.RazonSocial, c.Documento as ClientRUC, c.Direccion as ClientDir, 
                       c.Contacto as ClientContact, c.Email as ClientEmail,
                       u.Nombres as UserName, u.Apellidos as UserLast
                FROM PROFORMA p
                INNER JOIN CLIENTE c ON p.IdCliente = c.IdCliente
                INNER JOIN USUARIO u ON p.IdUsuario = u.IdUsuario
                WHERE p.IdProforma = ?
            """.trimIndent()
            val pstmt = conn.prepareStatement(query)
            pstmt.setInt(1, idProf)
            val rs = pstmt.executeQuery()
            if (rs.next()) {
                val map = mutableMapOf<String, Any>()
                map["IdProforma"] = rs.getInt("IdProforma")
                map["Codigo"] = rs.getString("Codigo")
                map["FechaEmision"] = rs.getString("FechaEmision")
                map["Estado"] = rs.getString("Estado")
                map["SubTotal"] = rs.getDouble("SubTotal")
                map["TotalIGV"] = rs.getDouble("TotalIGV")
                map["Total"] = rs.getDouble("Total")
                map["FormaPago"] = rs.getString("FormaPago") ?: "CONTADO"
                map["ValidezOferta"] = rs.getInt("ValidezOferta")
                map["TiempoEntrega"] = rs.getString("TiempoEntrega") ?: "3-5 DIAS"
                map["Garantia"] = rs.getString("Garantia") ?: "12 MESES"
                map["Observaciones"] = rs.getString("Observaciones") ?: ""
                
                // Cliente
                map["ClientName"] = rs.getString("RazonSocial")
                map["ClientRUC"] = rs.getString("ClientRUC")
                map["ClientDir"] = rs.getString("ClientDir") ?: "-"
                map["ClientContact"] = rs.getString("ClientContact") ?: "-"
                map["ClientEmail"] = rs.getString("ClientEmail") ?: "-"
                
                // Usuario
                map["UserName"] = rs.getString("UserName") ?: ""
                map["UserLast"] = rs.getString("UserLast") ?: ""
                
                return@withContext map
            }
        } catch (e: Exception) { Log.e("RailwayDB", "Error getProformaById: ${e.message}") }
        finally { conn?.close() }
        return@withContext null
    }

    suspend fun getProformaDetails(idProf: Int): List<Map<String, Any>> = withContext(Dispatchers.IO) {
        var conn: Connection? = null
        val list = mutableListOf<Map<String, Any>>()
        try {
            conn = getConnection()
            val query = "SELECT dp.*, p.Nombre as ProductoNombre FROM DETALLE_PROFORMA dp INNER JOIN PRODUCTO p ON dp.IdProducto = p.IdProducto WHERE dp.IdProforma = ?"
            val pstmt = conn.prepareStatement(query)
            pstmt.setInt(1, idProf)
            val rs = pstmt.executeQuery()
            while (rs.next()) {
                list.add(mapOf(
                    "Cantidad" to rs.getDouble("Cantidad"),
                    "Unidad" to rs.getString("UnidadMedida"),
                    "Descripcion" to rs.getString("ProductoNombre"),
                    "PrecioUnitario" to rs.getDouble("PrecioUnitario"),
                    "Total" to rs.getDouble("Total")
                ))
            }
        } catch (e: Exception) { Log.e("RailwayDB", "Error details: ${e.message}") }
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

    // Usar solo cuando se registra una factura/venta confirmada.
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

    // --- DASHBOARD ANALYTICS ---
    suspend fun getDashboardStats(): Map<String, Int> = withContext(Dispatchers.IO) {
        var conn: Connection? = null
        val stats = mutableMapOf<String, Int>()
        try {
            conn = getConnection()
            val query = """
                SELECT 
                    (SELECT COUNT(*) FROM CLIENTE) as clientes,
                    (SELECT COUNT(*) FROM CLIENTE WHERE FechaRegistro >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)) as clientes_30d,
                    (SELECT COUNT(*) FROM PRODUCTO) as productos,
                    (SELECT COUNT(*) FROM PRODUCTO WHERE FechaRegistro >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)) as productos_30d,
                    (SELECT COUNT(*) FROM PROFORMA) as proformas_total,
                        (SELECT COUNT(*) FROM PROFORMA WHERE DATE(COALESCE(FechaRegistro, FechaEmision)) = CURDATE()) as proformas_hoy,
                        (SELECT COUNT(*) FROM PROFORMA WHERE DATE(COALESCE(FechaRegistro, FechaEmision)) = DATE_SUB(CURDATE(), INTERVAL 1 DAY)) as proformas_ayer,
                    (SELECT COUNT(*) FROM EMPLEADO) as empleados,
                    (SELECT COUNT(*) FROM EMPLEADO WHERE FechaRegistro >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)) as empleados_30d
            """.trimIndent()
            val pstmt = conn.prepareStatement(query)
            val rs = pstmt.executeQuery()
            if (rs.next()) {
                stats["clientes"] = rs.getInt("clientes")
                stats["clientes_30d"] = rs.getInt("clientes_30d")
                stats["productos"] = rs.getInt("productos")
                stats["productos_30d"] = rs.getInt("productos_30d")
                stats["proformas"] = rs.getInt("proformas_total")
                stats["proformas_hoy"] = rs.getInt("proformas_hoy")
                stats["proformas_ayer"] = rs.getInt("proformas_ayer")
                stats["empleados"] = rs.getInt("empleados")
                stats["empleados_30d"] = rs.getInt("empleados_30d")
            }
        } catch (e: Exception) { Log.e("RailwayDB", "Error stats: ${e.message}") }
        finally { conn?.close() }
        return@withContext stats
    }

    suspend fun getProformaActivityWeekly(days: Int = 7): ProformaActivitySeries = withContext(Dispatchers.IO) {
        var conn: Connection? = null
        val safeDays = days.coerceIn(1, 31)
        val values = MutableList(safeDays) { 0 }
        val labels = mutableListOf<String>()

        try {
            conn = getConnection()
            val query = """
                SELECT DATE_FORMAT(DATE(COALESCE(FechaRegistro, FechaEmision)), '%Y-%m-%d') AS fecha,
                       COUNT(*) AS cantidad
                FROM PROFORMA
                WHERE DATE(COALESCE(FechaRegistro, FechaEmision))
                      BETWEEN DATE_SUB(CURDATE(), INTERVAL ${safeDays - 1} DAY) AND CURDATE()
                GROUP BY DATE(COALESCE(FechaRegistro, FechaEmision))
                ORDER BY fecha ASC
            """.trimIndent()
            val rs = conn.prepareStatement(query).executeQuery()

            val countsMap = mutableMapOf<String, Int>()
            while (rs.next()) {
                countsMap[rs.getString("fecha")] = rs.getInt("cantidad")
            }

            val baseDateQuery = "SELECT DATE_FORMAT(CURDATE(), '%Y-%m-%d') AS fechaBase"
            val baseDateRs = conn.prepareStatement(baseDateQuery).executeQuery()

            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val baseCalendar = Calendar.getInstance()
            if (baseDateRs.next()) {
                val baseDate = sdf.parse(baseDateRs.getString("fechaBase"))
                if (baseDate != null) {
                    baseCalendar.time = baseDate
                }
            }

            baseCalendar.set(Calendar.HOUR_OF_DAY, 0)
            baseCalendar.set(Calendar.MINUTE, 0)
            baseCalendar.set(Calendar.SECOND, 0)
            baseCalendar.set(Calendar.MILLISECOND, 0)
            baseCalendar.add(Calendar.DAY_OF_YEAR, -(safeDays - 1))

            val dayNames = arrayOf("Dom", "Lun", "Mar", "Mie", "Jue", "Vie", "Sab")
            repeat(safeDays) { index ->
                val key = sdf.format(baseCalendar.time)
                values[index] = countsMap[key] ?: 0

                val dayIndex = (baseCalendar.get(Calendar.DAY_OF_WEEK) - 1).coerceIn(0, 6)
                labels.add(dayNames[dayIndex])

                baseCalendar.add(Calendar.DAY_OF_YEAR, 1)
            }
        } catch (e: Exception) {
            Log.e("RailwayDB", "Error weekly activity: ${e.message}")
        } finally {
            conn?.close()
        }

        if (labels.isEmpty()) {
            val fallback = arrayOf("Lun", "Mar", "Mie", "Jue", "Vie", "Sab", "Dom")
            repeat(safeDays) { labels.add(fallback[it % fallback.size]) }
        }

        return@withContext ProformaActivitySeries(labels = labels, values = values)
    }

    suspend fun getProformaActivityMonthly(months: Int = 6): ProformaActivitySeries = withContext(Dispatchers.IO) {
        var conn: Connection? = null
        val safeMonths = months.coerceIn(1, 24)
        val labels = mutableListOf<String>()
        val values = mutableListOf<Int>()

        try {
            conn = getConnection()
            val query = """
                SELECT DATE_FORMAT(DATE(COALESCE(FechaRegistro, FechaEmision)), '%Y-%m') AS mes,
                       COUNT(*) AS cantidad
                FROM PROFORMA
                WHERE DATE(COALESCE(FechaRegistro, FechaEmision))
                      >= DATE_SUB(DATE_FORMAT(CURDATE(), '%Y-%m-01'), INTERVAL ${safeMonths - 1} MONTH)
                GROUP BY DATE_FORMAT(DATE(COALESCE(FechaRegistro, FechaEmision)), '%Y-%m')
                ORDER BY mes ASC
            """.trimIndent()
            val rs = conn.prepareStatement(query).executeQuery()

            val countByMonth = mutableMapOf<String, Int>()
            while (rs.next()) {
                countByMonth[rs.getString("mes")] = rs.getInt("cantidad")
            }

            val baseMonthQuery = "SELECT DATE_FORMAT(CURDATE(), '%Y-%m-01') AS mesBase"
            val baseMonthRs = conn.prepareStatement(baseMonthQuery).executeQuery()

            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val baseCalendar = Calendar.getInstance()
            if (baseMonthRs.next()) {
                val baseDate = sdf.parse(baseMonthRs.getString("mesBase"))
                if (baseDate != null) {
                    baseCalendar.time = baseDate
                }
            }

            baseCalendar.set(Calendar.DAY_OF_MONTH, 1)
            baseCalendar.set(Calendar.HOUR_OF_DAY, 0)
            baseCalendar.set(Calendar.MINUTE, 0)
            baseCalendar.set(Calendar.SECOND, 0)
            baseCalendar.set(Calendar.MILLISECOND, 0)

            val monthNames = arrayOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")

            for (i in (safeMonths - 1) downTo 0) {
                val monthCalendar = Calendar.getInstance().apply {
                    time = baseCalendar.time
                    add(Calendar.MONTH, -i)
                }

                val year = monthCalendar.get(Calendar.YEAR)
                val month = monthCalendar.get(Calendar.MONTH) + 1
                val key = String.format(Locale.US, "%04d-%02d", year, month)

                labels.add(monthNames[monthCalendar.get(Calendar.MONTH)])
                values.add(countByMonth[key] ?: 0)
            }
        } catch (e: Exception) {
            Log.e("RailwayDB", "Error monthly activity: ${e.message}")
        } finally {
            conn?.close()
        }

        if (labels.isEmpty()) {
            val fallback = arrayOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")
            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_MONTH, 1)
            for (i in (safeMonths - 1) downTo 0) {
                val monthCal = Calendar.getInstance().apply {
                    time = cal.time
                    add(Calendar.MONTH, -i)
                }
                labels.add(fallback[monthCal.get(Calendar.MONTH)])
                values.add(0)
            }
        }

        return@withContext ProformaActivitySeries(labels = labels, values = values)
    }

    suspend fun getProformaActivity(days: Int): List<Int> {
        return getProformaActivityWeekly(days).values
    }

    // --- REPORTES ANALÍTICOS ---
    suspend fun getReportKPIs(): Map<String, Any> = withContext(Dispatchers.IO) {
        var conn: Connection? = null
        val kpis = mutableMapOf<String, Any>()
        try {
            conn = getConnection()
            val query = """
                SELECT 
                    (SELECT COUNT(*) FROM PROFORMA) as totalProformas,
                    (SELECT COUNT(*) FROM PROFORMA WHERE Estado = 'PENDIENTE') as proformasPendientes,
                    (SELECT COUNT(*) FROM PROFORMA WHERE Estado IN ('VENDIDA', 'APROBADA')) as proformasVendidas,
                    (SELECT COUNT(*) FROM FACTURA) as totalFacturas,
                    (SELECT COALESCE(SUM(Total), 0) FROM FACTURA WHERE Estado != 'ANULADA') as totalIngresos,
                    (SELECT COUNT(*) FROM CLIENTE) as totalClientes,
                    (SELECT COUNT(*) FROM PRODUCTO) as totalProductos
            """.trimIndent()
            val pstmt = conn.prepareStatement(query)
            val rs = pstmt.executeQuery()
            if (rs.next()) {
                val totalProf = rs.getInt("totalProformas")
                val totalFact = rs.getInt("totalFacturas")
                kpis["totalProformas"] = totalProf
                kpis["proformasPendientes"] = rs.getInt("proformasPendientes")
                kpis["proformasVendidas"] = rs.getInt("proformasVendidas")
                kpis["totalFacturas"] = totalFact
                kpis["totalIngresos"] = rs.getDouble("totalIngresos")
                kpis["totalClientes"] = rs.getInt("totalClientes")
                kpis["totalProductos"] = rs.getInt("totalProductos")
                
                // Tasa de conversión: Facturas / Proformas válidas
                kpis["tasaConversion"] = if (totalProf > 0) (totalFact * 100.0 / totalProf) else 0.0
            }
        } catch (e: Exception) { Log.e("RailwayDB", "Error report KPIs: ${e.message}") }
        finally { conn?.close() }
        return@withContext kpis
    }

    suspend fun getProformasByState(): List<Map<String, Any>> = withContext(Dispatchers.IO) {
        var conn: Connection? = null
        val list = mutableListOf<Map<String, Any>>()
        try {
            conn = getConnection()
            val query = "SELECT Estado, COUNT(*) as cantidad FROM PROFORMA GROUP BY Estado ORDER BY cantidad DESC"
            val pstmt = conn.prepareStatement(query)
            val rs = pstmt.executeQuery()
            while (rs.next()) {
                list.add(mapOf(
                    "estado" to rs.getString("Estado"),
                    "cantidad" to rs.getInt("cantidad")
                ))
            }
        } catch (e: Exception) { Log.e("RailwayDB", "Error proformas by state: ${e.message}") }
        finally { conn?.close() }
        return@withContext list
    }

    suspend fun getTopClients(limit: Int = 7): List<Map<String, Any>> = withContext(Dispatchers.IO) {
        var conn: Connection? = null
        val list = mutableListOf<Map<String, Any>>()
        try {
            conn = getConnection()
            val query = """
                SELECT c.RazonSocial, COUNT(f.IdFactura) as cantidadVentas, 
                       COALESCE(SUM(f.Total), 0) as totalMonto
                FROM FACTURA f 
                INNER JOIN CLIENTE c ON f.IdCliente = c.IdCliente 
                WHERE f.Estado != 'ANULADA'
                GROUP BY c.IdCliente, c.RazonSocial 
                ORDER BY totalMonto DESC 
                LIMIT ?
            """.trimIndent()
            val pstmt = conn.prepareStatement(query)
            pstmt.setInt(1, limit)
            val rs = pstmt.executeQuery()
            while (rs.next()) {
                list.add(mapOf(
                    "cliente" to rs.getString("RazonSocial"),
                    "cantidad" to rs.getInt("cantidadVentas"),
                    "monto" to rs.getBigDecimal("totalMonto").toDouble()
                ))
            }
        } catch (e: Exception) { Log.e("RailwayDB", "Error top clients: ${e.message}") }
        finally { conn?.close() }
        return@withContext list
    }

    suspend fun getSalesByMonth(): List<Map<String, Any>> = withContext(Dispatchers.IO) {
        var conn: Connection? = null
        val list = mutableListOf<Map<String, Any>>()
        try {
            conn = getConnection()
            val query = """
                SELECT DATE_FORMAT(FechaEmision, '%Y-%m') as mes, 
                       COUNT(*) as cantidadVentas,
                       COALESCE(SUM(Total), 0) as totalVentas
                FROM FACTURA 
                WHERE Estado != 'ANULADA' AND FechaEmision >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH)
                GROUP BY DATE_FORMAT(FechaEmision, '%Y-%m') 
                ORDER BY mes ASC
            """.trimIndent()
            val pstmt = conn.prepareStatement(query)
            val rs = pstmt.executeQuery()
            while (rs.next()) {
                list.add(mapOf(
                    "mes" to rs.getString("mes"),
                    "cantidad" to rs.getInt("cantidadVentas"),
                    "total" to rs.getDouble("totalVentas")
                ))
            }
        } catch (e: Exception) { Log.e("RailwayDB", "Error sales by month: ${e.message}") }
        finally { conn?.close() }
        return@withContext list
    }

    suspend fun getAttendances(
        fecha: String? = null,
        search: String = "",
        estadoFiltro: String = "TODOS"
    ): List<Map<String, Any>> = withContext(Dispatchers.IO) {
        var conn: Connection? = null
        val list = mutableListOf<Map<String, Any>>()
        try {
            conn = getConnection()

            val normalizedFilter = estadoFiltro.trim().uppercase()
            val hasSearch = search.trim().isNotBlank()

            val queryBuilder = StringBuilder(
                """
                SELECT
                    e.IdEmpleado,
                    e.Cargo,
                    u.Nombres,
                    u.Apellidos,
                    u.Correo,
                    a.IdAsistencia,
                    a.Fecha,
                    a.HoraEntrada,
                    a.HoraSalida,
                    CASE
                        WHEN a.IdAsistencia IS NULL THEN 'SIN REGISTRO'
                        WHEN a.HoraSalida IS NULL THEN 'EN JORNADA'
                        ELSE 'COMPLETADA'
                    END as EstadoAsistencia
                FROM EMPLEADO e
                INNER JOIN USUARIO u ON e.IdUsuario = u.IdUsuario
                LEFT JOIN ASISTENCIA a ON a.IdEmpleado = e.IdEmpleado
                """.trimIndent()
            )

            if (!fecha.isNullOrBlank()) {
                queryBuilder.append(" AND DATE(a.Fecha) = ?")
            }

            queryBuilder.append(" WHERE u.Estado = 1")

            if (hasSearch) {
                queryBuilder.append(" AND (")
                queryBuilder.append("CONCAT(u.Nombres, ' ', u.Apellidos) LIKE ? ")
                queryBuilder.append("OR e.Cargo LIKE ? ")
                queryBuilder.append("OR u.Correo LIKE ?")
                queryBuilder.append(")")
            }

            when (normalizedFilter) {
                "EN JORNADA" -> queryBuilder.append(" AND a.IdAsistencia IS NOT NULL AND a.HoraSalida IS NULL")
                "COMPLETADA" -> queryBuilder.append(" AND a.IdAsistencia IS NOT NULL AND a.HoraSalida IS NOT NULL")
                "SIN REGISTRO" -> queryBuilder.append(" AND a.IdAsistencia IS NULL")
            }

            queryBuilder.append(" ORDER BY u.Nombres ASC, u.Apellidos ASC")

            val query = queryBuilder.toString()
            val pstmt = conn.prepareStatement(query)

            var index = 1
            if (!fecha.isNullOrBlank()) {
                pstmt.setString(index++, fecha)
            }

            if (hasSearch) {
                val searchPattern = "%${search.trim()}%"
                pstmt.setString(index++, searchPattern)
                pstmt.setString(index++, searchPattern)
                pstmt.setString(index++, searchPattern)
            }

            val rs = pstmt.executeQuery()
            while (rs.next()) {
                val idAsistencia = rs.getObject("IdAsistencia") as? Number
                val estadoAsistencia = rs.getString("EstadoAsistencia") ?: "SIN REGISTRO"

                list.add(mapOf(
                    "IdEmpleado" to rs.getInt("IdEmpleado"),
                    "IdAsistencia" to (idAsistencia?.toInt() ?: 0),
                    "Empleado" to "${rs.getString("Nombres")} ${rs.getString("Apellidos")}",
                    "Cargo" to rs.getString("Cargo"),
                    "Correo" to (rs.getString("Correo") ?: ""),
                    "Fecha" to (rs.getString("Fecha") ?: (fecha ?: "")),
                    "HoraEntrada" to (rs.getString("HoraEntrada") ?: "--:--"),
                    "HoraSalida" to (rs.getString("HoraSalida") ?: "--:--"),
                    "Estado" to estadoAsistencia
                ))
            }
        } catch (e: Exception) { Log.e("RailwayDB", "Error getAttendances: ${e.message}") }
        finally { conn?.close() }
        return@withContext list
    }

    // --- FACTURAS ---
    suspend fun getFacturas(search: String = "", estadoFiltro: String = "TODOS"): List<Map<String, Any>> = withContext(Dispatchers.IO) {
        var conn: Connection? = null
        val list = mutableListOf<Map<String, Any>>()
        try {
            conn = getConnection()
            val whereEstado = if (estadoFiltro != "TODOS") "AND f.Estado = '$estadoFiltro'" else ""
            val query = """
                SELECT f.IdFactura, f.Codigo, f.FechaEmision, f.FechaVencimiento,
                       f.SubTotal, f.TotalIGV, f.Total, f.Estado, f.FormaPago,
                       c.RazonSocial as ClienteNombre
                FROM FACTURA f
                INNER JOIN CLIENTE c ON f.IdCliente = c.IdCliente
                WHERE (f.Codigo LIKE ? OR c.RazonSocial LIKE ?)
                $whereEstado
                ORDER BY f.FechaRegistro DESC
            """.trimIndent()
            val pstmt = conn.prepareStatement(query)
            pstmt.setString(1, "%$search%")
            pstmt.setString(2, "%$search%")
            val rs = pstmt.executeQuery()
            while (rs.next()) {
                list.add(mapOf(
                    "IdFactura"        to rs.getInt("IdFactura"),
                    "Codigo"           to rs.getString("Codigo"),
                    "Cliente"          to rs.getString("ClienteNombre"),
                    "Total"            to rs.getDouble("Total"),
                    "Estado"           to rs.getString("Estado"),
                    "FechaEmision"     to (rs.getString("FechaEmision") ?: ""),
                    "FechaVencimiento" to (rs.getString("FechaVencimiento") ?: ""),
                    "FormaPago"        to (rs.getString("FormaPago") ?: "")
                ))
            }
        } catch (e: Exception) { Log.e("RailwayDB", "Error facturas: ${e.message}") }
        finally { conn?.close() }
        return@withContext list
    }

    suspend fun getFacturaById(idFact: Int): Map<String, Any>? = withContext(Dispatchers.IO) {
        var conn: Connection? = null
        try {
            conn = getConnection()
            val query = """
                SELECT f.*,
                       c.RazonSocial, c.Documento as ClientRUC,
                       c.Direccion as ClientDir, c.Contacto as ClientContact,
                       c.Email as ClientEmail,
                       u.Nombres as UserName, u.Apellidos as UserLast,
                       e.Nombre as EmpresaNombre, e.RUC as EmpresaRUC,
                       e.Direccion as EmpresaDir, e.Telefono as EmpresaTel
                FROM FACTURA f
                INNER JOIN CLIENTE c ON f.IdCliente = c.IdCliente
                INNER JOIN USUARIO u ON f.IdUsuario = u.IdUsuario
                INNER JOIN EMPRESA e ON f.IdEmpresa = e.IdEmpresa
                WHERE f.IdFactura = ?
            """.trimIndent()
            val pstmt = conn.prepareStatement(query)
            pstmt.setInt(1, idFact)
            val rs = pstmt.executeQuery()
            if (rs.next()) {
                val map = mutableMapOf<String, Any>()
                map["IdFactura"]       = rs.getInt("IdFactura")
                map["Codigo"]          = rs.getString("Codigo")
                map["FechaEmision"]    = rs.getString("FechaEmision") ?: ""
                map["FechaVencimiento"]= rs.getString("FechaVencimiento") ?: ""
                map["Estado"]          = rs.getString("Estado") ?: "PENDIENTE"
                map["SubTotal"]        = rs.getDouble("SubTotal")
                map["TotalIGV"]        = rs.getDouble("TotalIGV")
                map["Total"]           = rs.getDouble("Total")
                map["FormaPago"]       = rs.getString("FormaPago") ?: "CONTADO"
                map["Observaciones"]   = rs.getString("Observaciones") ?: ""
                // Cliente
                map["ClientName"]    = rs.getString("RazonSocial") ?: ""
                map["ClientRUC"]     = rs.getString("ClientRUC") ?: ""
                map["ClientDir"]     = rs.getString("ClientDir") ?: "-"
                map["ClientContact"] = rs.getString("ClientContact") ?: "-"
                map["ClientEmail"]   = rs.getString("ClientEmail") ?: "-"
                // Emisor
                map["UserName"]      = rs.getString("UserName") ?: ""
                map["UserLast"]      = rs.getString("UserLast") ?: ""
                // Empresa
                map["EmpresaNombre"] = rs.getString("EmpresaNombre") ?: "CARSIL SAC"
                map["EmpresaRUC"]    = rs.getString("EmpresaRUC") ?: ""
                map["EmpresaDir"]    = rs.getString("EmpresaDir") ?: ""
                map["EmpresaTel"]    = rs.getString("EmpresaTel") ?: ""
                return@withContext map
            }
        } catch (e: Exception) { Log.e("RailwayDB", "Error getFacturaById: ${e.message}") }
        finally { conn?.close() }
        return@withContext null
    }

    suspend fun getFacturaDetails(idFact: Int): List<Map<String, Any>> = withContext(Dispatchers.IO) {
        var conn: Connection? = null
        val list = mutableListOf<Map<String, Any>>()
        try {
            conn = getConnection()
            val query = """
                SELECT df.*, p.Nombre as ProductoNombre, p.Codigo as ProductoCodigo
                FROM DETALLE_FACTURA df
                INNER JOIN PRODUCTO p ON df.IdProducto = p.IdProducto
                WHERE df.IdFactura = ?
            """.trimIndent()
            val pstmt = conn.prepareStatement(query)
            pstmt.setInt(1, idFact)
            val rs = pstmt.executeQuery()
            while (rs.next()) {
                list.add(mapOf(
                    "Cantidad"       to rs.getDouble("Cantidad"),
                    "Unidad"         to (rs.getString("UnidadMedida") ?: "UNID"),
                    "Descripcion"    to rs.getString("ProductoNombre"),
                    "PrecioUnitario" to rs.getDouble("PrecioUnitario"),
                    "Total"          to rs.getDouble("Total")
                ))
            }
        } catch (e: Exception) { Log.e("RailwayDB", "Error factura details: ${e.message}") }
        finally { conn?.close() }
        return@withContext list
    }
}
