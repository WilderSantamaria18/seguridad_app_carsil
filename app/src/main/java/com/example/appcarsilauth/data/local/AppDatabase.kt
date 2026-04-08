package com.example.appcarsilauth.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.appcarsilauth.data.local.dao.AuditDao
import com.example.appcarsilauth.data.local.dao.IntranetDao
import com.example.appcarsilauth.data.local.dao.SecurityDao
import com.example.appcarsilauth.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        SecurityControl::class,
        AuditLog::class,
        RolEntity::class,
        PermisoEntity::class,
        UsuarioEntity::class,
        EmpresaEntity::class,
        ClienteEntity::class,
        ProductoEntity::class,
        ProformaEntity::class,
        DetalleProformaEntity::class,
        EmpleadoEntity::class,
        AsistenciaEntity::class
    ],
    version = 9,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun securityDao(): SecurityDao
    abstract fun auditDao(): AuditDao
    abstract fun intranetDao(): IntranetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "carsil_secure_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    val dao = database.intranetDao()
                    
                    dao.insertRoles(listOf(
                        RolEntity(1, "Administrador"),
                        RolEntity(2, "Empleado"),
                        RolEntity(3, "Supervisor"),
                        RolEntity(4, "Vendedor")
                    ))

                    dao.insertPermisos(listOf(
                        PermisoEntity(IdRol = 1, NombreMenu = "Dashboard"),
                        PermisoEntity(IdRol = 1, NombreMenu = "Clientes"),
                        PermisoEntity(IdRol = 1, NombreMenu = "Productos"),
                        PermisoEntity(IdRol = 1, NombreMenu = "Proformas"),
                        PermisoEntity(IdRol = 1, NombreMenu = "Asistencia"),
                        PermisoEntity(IdRol = 3, NombreMenu = "Dashboard"),
                        PermisoEntity(IdRol = 3, NombreMenu = "Clientes"),
                        PermisoEntity(IdRol = 3, NombreMenu = "Productos"),
                        PermisoEntity(IdRol = 3, NombreMenu = "Proformas"),
                        PermisoEntity(IdRol = 3, NombreMenu = "Asistencia"),
                        PermisoEntity(IdRol = 2, NombreMenu = "Asistencia"),
                        PermisoEntity(IdRol = 4, NombreMenu = "Dashboard"),
                        PermisoEntity(IdRol = 4, NombreMenu = "Clientes"),
                        PermisoEntity(IdRol = 4, NombreMenu = "Productos"),
                        PermisoEntity(IdRol = 4, NombreMenu = "Proformas")
                    ))

                    dao.insertEmpresas(listOf(
                        EmpresaEntity(
                            IdEmpresa = 1,
                            Nombre = "CARSIL Bombas Hidraulicas S.A.C.",
                            RUC = "20601234571",
                            Direccion = "Av. Industrial 456, Zona Industrial, Ate, Lima",
                            Telefono = "014567890",
                            Celular = "987321654",
                            Email = "ventas@carsil.com.pe"
                        )
                    ))
                    
                    dao.insertUsuarios(listOf(
                        UsuarioEntity(
                            IdUsuario = 1,
                            Nombres = "Carlos Enrique",
                            Apellidos = "Silva Romero",
                            TipoDocumento = "DNI",
                            NumeroDocumento = "42187634",
                            Correo = "csilva@carsil.com",
                            Clave = "\$2a\$10\$qb/OBcDiKog7VNClJFXdbukdiQqFiJbXkzMKHUxcvgQihE7zVuLdS",
                            IdRol = 1,
                            Estado = 1,
                            Telefono = "987321654",
                            Direccion = "Av. Industrial 456, Ate, Lima"
                        ),
                        UsuarioEntity(
                            IdUsuario = 2,
                            Nombres = "Rosa Amelia",
                            Apellidos = "Paredes Quispe",
                            TipoDocumento = "DNI",
                            NumeroDocumento = "47263819",
                            Correo = "rparedes@carsil.com",
                            Clave = "\$2a\$10\$qb/OBcDiKog7VNClJFXdbukdiQqFiJbXkzMKHUxcvgQihE7zVuLdS",
                            IdRol = 3,
                            Estado = 1,
                            Telefono = "986452371",
                            Direccion = "Jr. Los Alamos 238, Ate, Lima"
                        ),
                        UsuarioEntity(
                            IdUsuario = 3,
                            Nombres = "Miguel Angel",
                            Apellidos = "Fuentes Cardenas",
                            TipoDocumento = "DNI",
                            NumeroDocumento = "43572810",
                            Correo = "mfuentes@carsil.com",
                            Clave = "\$2a\$10\$qb/OBcDiKog7VNClJFXdbukdiQqFiJbXkzMKHUxcvgQihE7zVuLdS",
                            IdRol = 3,
                            Estado = 1,
                            Telefono = "998761234",
                            Direccion = "Calle Moquegua 112, San Luis, Lima"
                        ),
                        UsuarioEntity(
                            IdUsuario = 4,
                            Nombres = "Jean Pierre",
                            Apellidos = "Enriquez Solano",
                            TipoDocumento = "DNI",
                            NumeroDocumento = "72384910",
                            Correo = "jenriquez@carsil.com",
                            Clave = "\$2a\$10\$qb/OBcDiKog7VNClJFXdbukdiQqFiJbXkzMKHUxcvgQihE7zVuLdS",
                            IdRol = 2,
                            Estado = 1,
                            Telefono = "975638241",
                            Direccion = "Av. Primavera 567, La Molina, Lima"
                        )
                    ))
                    
                    dao.insertClientes(listOf(
                        ClienteEntity(
                            IdCliente = 1,
                            Documento = "20501234567",
                            RazonSocial = "Constructora Los Andes S.A.C.",
                            Direccion = "Av. Javier Prado 1234, San Isidro, Lima",
                            Telefono = "016543210",
                            Celular = "912345678",
                            Email = "compras@losandes.com",
                            Contacto = "Roberto Sanchez",
                            Estado = 1
                        ),
                        ClienteEntity(
                            IdCliente = 2,
                            Documento = "20602345678",
                            RazonSocial = "Minera El Dorado S.A.",
                            Direccion = "Carretera Central Km 45, Junin",
                            Telefono = "064543211",
                            Celular = "912345679",
                            Email = "logistica@eldorado.com",
                            Contacto = "Fernando Quispe",
                            Estado = 1
                        ),
                        ClienteEntity(
                            IdCliente = 3,
                            Documento = "20703456789",
                            RazonSocial = "Agroindustrial del Pacifico S.A.C.",
                            Direccion = "Fundo La Victoria, Ica",
                            Telefono = "056543212",
                            Celular = "912345680",
                            Email = "compras@agropacifico.com",
                            Contacto = "Patricia Gomez",
                            Estado = 1
                        )
                    ))

                    dao.insertProductos(listOf(
                        ProductoEntity(
                            IdProducto = 1,
                            Codigo = "BOM-SUM-001",
                            Nombre = "Bomba Sumergible 1HP",
                            Descripcion = "Bomba sumergible para pozo profundo, acero inoxidable, 1HP, 220V",
                            Marca = "Pedrollo",
                            Modelo = "4SR1m/13",
                            Tipo = "Sumergible",
                            UnidadMedida = "UNID",
                            PrecioUnitario = 850.00,
                            Stock = 25,
                            StockMinimo = 5,
                            Estado = 1
                        ),
                        ProductoEntity(
                            IdProducto = 2,
                            Codigo = "BOM-SUM-002",
                            Nombre = "Bomba Sumergible 2HP",
                            Descripcion = "Bomba sumergible para aguas limpias, 2HP, 380V",
                            Marca = "Grundfos",
                            Modelo = "SP 5-50",
                            Tipo = "Sumergible",
                            UnidadMedida = "UNID",
                            PrecioUnitario = 1200.00,
                            Stock = 15,
                            StockMinimo = 5,
                            Estado = 1
                        ),
                        ProductoEntity(
                            IdProducto = 3,
                            Codigo = "BOM-SUM-003",
                            Nombre = "Bomba Sumergible 3HP",
                            Descripcion = "Bomba sumergible para aguas residuales con solidos",
                            Marca = "Ebara",
                            Modelo = "Drainage DVS",
                            Tipo = "Sumergible",
                            UnidadMedida = "UNID",
                            PrecioUnitario = 1800.00,
                            Stock = 10,
                            StockMinimo = 3,
                            Estado = 1
                        ),
                        ProductoEntity(
                            IdProducto = 4,
                            Codigo = "ELE-001",
                            Nombre = "Electrobomba Centrifuga 1HP",
                            Descripcion = "Electrobomba horizontal monofasica, 1HP",
                            Marca = "Pedrollo",
                            Modelo = "PKm 60",
                            Tipo = "Centrifuga",
                            UnidadMedida = "UNID",
                            PrecioUnitario = 480.00,
                            Stock = 12,
                            StockMinimo = 5,
                            Estado = 1
                        ),
                        ProductoEntity(
                            IdProducto = 5,
                            Codigo = "ACC-CON-002",
                            Nombre = "Controlador de Bomba",
                            Descripcion = "Controlador electronico para bomba con proteccion por sequia",
                            Marca = "Franklin",
                            Modelo = "Control Pro",
                            Tipo = "Accesorio",
                            UnidadMedida = "UNID",
                            PrecioUnitario = 120.00,
                            Stock = 50,
                            StockMinimo = 10,
                            Estado = 1
                        )
                    ))

                    // Empleados de CARSIL (vinculados al Usuario)
                    dao.insertEmpleados(listOf(
                        EmpleadoEntity(1, 1, "Administrador del Sistema", "Administracion", "2022-03-01", "INDEFINIDO", 500.00),
                        EmpleadoEntity(2, 2, "Supervisor de Operaciones", "Operaciones", "2022-06-15", "INDEFINIDO", 420.00),
                        EmpleadoEntity(3, 3, "Supervisor Comercial", "Ventas", "2023-01-10", "INDEFINIDO", 400.00),
                        EmpleadoEntity(4, 4, "Tecnico en Bombas Hidraulicas", "Mantenimiento", "2023-04-01", "INDEFINIDO", 280.00)
                    ))
                }
            }
        }
    }
}
