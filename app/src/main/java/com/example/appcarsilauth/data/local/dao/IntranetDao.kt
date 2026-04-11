package com.example.appcarsilauth.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.appcarsilauth.data.local.entity.*

@Dao
interface IntranetDao {
    
    // Inserciones para el Seeder (Pre-carga)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoles(roles: List<RolEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPermisos(permisos: List<PermisoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmpresas(empresas: List<EmpresaEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsuarios(usuarios: List<UsuarioEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClientes(clientes: List<ClienteEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCliente(cliente: ClienteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProductos(productos: List<ProductoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducto(producto: ProductoEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmpleados(empleados: List<EmpleadoEntity>)
    
    @Insert
    suspend fun insertProforma(proforma: ProformaEntity): Long

    @Insert
    suspend fun insertDetalleProforma(detalle: DetalleProformaEntity): Long
    
    @Insert
    suspend fun insertAsistencia(asistencia: AsistenciaEntity): Long

    @Query("UPDATE ASISTENCIA SET HoraSalida = :horaSalida WHERE IdEmpleado = :idEmpleado AND Fecha = :fecha")
    suspend fun updateHoraSalida(idEmpleado: Int, fecha: String, horaSalida: String)
    
    @Query("SELECT * FROM EMPLEADO WHERE IdUsuario = :idUsuario LIMIT 1")
    suspend fun getEmpleadoByUsuario(idUsuario: Int): EmpleadoEntity?

    @Query("SELECT * FROM ASISTENCIA WHERE IdEmpleado = :idEmpleado AND Fecha = :fecha LIMIT 1")
    suspend fun getAsistenciaHoy(idEmpleado: Int, fecha: String): AsistenciaEntity?

    // Consultas Operativas
    @Query("SELECT * FROM USUARIO WHERE LOWER(Correo) = LOWER(:email) AND Estado = 1 LIMIT 1")
    suspend fun getUserByEmail(email: String): UsuarioEntity?

    @Query("SELECT Descripcion FROM ROL WHERE IdRol = :roleId LIMIT 1")
    suspend fun getRoleName(roleId: Int): String?

    @Query("SELECT NombreMenu FROM PERMISO WHERE IdRol = :roleId")
    suspend fun getMenusByRole(roleId: Int): List<String>

    @Query("SELECT * FROM EMPRESA WHERE IdEmpresa = :idEmpresa LIMIT 1")
    suspend fun getEmpresaById(idEmpresa: Int): EmpresaEntity?

    @Query("SELECT * FROM CLIENTE")
    suspend fun getAllClientes(): List<ClienteEntity>

    @Query("SELECT COALESCE(MAX(IdCliente), 0) FROM CLIENTE")
    suspend fun getMaxClienteId(): Int

    @Query("SELECT * FROM PRODUCTO")
    suspend fun getAllProductos(): List<ProductoEntity>

    @Query("SELECT COALESCE(MAX(IdProducto), 0) FROM PRODUCTO")
    suspend fun getMaxProductoId(): Int

    // Usar solo cuando se registra una factura/venta confirmada.
    @Query("UPDATE PRODUCTO SET Stock = Stock - :cantidad WHERE IdProducto = :idProducto AND Stock >= :cantidad")
    suspend fun updateStockReduction(idProducto: Int, cantidad: Int): Int

    @Query("SELECT * FROM PRODUCTO WHERE Stock < :minStock")
    suspend fun getLowStockProducts(minStock: Int): List<ProductoEntity>
}
