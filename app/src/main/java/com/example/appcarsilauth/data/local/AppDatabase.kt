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
        AsistenciaEntity::class,
        Sale::class
    ],
    version = 11,
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
                    "carsil_secure_v3"
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
            // Usamos un hilo separado para la inserción inicial
            CoroutineScope(Dispatchers.IO).launch {
                INSTANCE?.let { database ->
                    try {
                        val dao = database.intranetDao()
                        // Verificación rápida para evitar inserciones dobles
                        if (dao.getUserByEmail("csilva@carsil.com") == null) {
                            seedDatabase(dao)
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("DATABASE", "Error seeding database: ${e.message}")
                    }
                }
            }
        }

        private suspend fun seedDatabase(dao: IntranetDao) {
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
                EmpresaEntity(1, "CARSIL Bombas Hidraulicas S.A.C.", "20601234571", "Av. Industrial 456, Ate, Lima", "014567890", "987321654", "ventas@carsil.com.pe")
            ))
            
            val passHash = "$2a$10$/.wQt2cdTHlYmJ0kj0Xdm.hW9UYeZ7lzJxW3dZTQyM0SZOsZiQtAy"
            
            dao.insertUsuarios(listOf(
                UsuarioEntity(1, "Carlos Enrique", "Silva Romero", "DNI", "42187634", "csilva@carsil.com", passHash, 1, 1, 0, null, "987321654", "Av. Industrial 456, Ate, Lima"),
                UsuarioEntity(2, "Rosa Amelia", "Paredes Quispe", "DNI", "47263819", "rparedes@carsil.com", passHash, 3, 1, 0, null, "986452371", "Jr. Los Alamos 238, Ate, Lima"),
                UsuarioEntity(3, "Miguel Angel", "Fuentes Cardenas", "DNI", "43572810", "mfuentes@carsil.com", passHash, 3, 1, 0, null, "998761234", "Calle Moquegua 112, San Luis, Lima"),
                UsuarioEntity(4, "Jean Pierre", "Enriquez Solano", "DNI", "72384910", "jenriquez@carsil.com", passHash, 2, 1, 0, null, "975638241", "Av. Primavera 567, La Molina, Lima")
            ))
            
            dao.insertClientes(listOf(
                ClienteEntity(1, "20501234567", "Constructora Los Andes S.A.C.", "Av. Javier Prado 1234, San Isidro, Lima", "016543210", "912345678", "compras@losandes.com", "Roberto Sanchez", 1),
                ClienteEntity(2, "20602345678", "Minera El Dorado S.A.", "Carretera Central Km 45, Junin", "064543211", "912345679", "logistica@eldorado.com", "Fernando Quispe", 1),
                ClienteEntity(3, "20703456789", "Agroindustrial del Pacifico S.A.C.", "Fundo La Victoria, Ica", "056543212", "912345680", "compras@agropacifico.com", "Patricia Gomez", 1)
            ))

            dao.insertProductos(listOf(
                ProductoEntity(1, "BOM-SUM-001", "Bomba Sumergible 1HP", "Bomba sumergible para pozo profundo, acero inoxidable, 1HP, 220V", "Pedrollo", "4SR1m/13", "Sumergible", "UNID", 850.00, 25, 5, 1),
                ProductoEntity(2, "BOM-SUM-002", "Bomba Sumergible 2HP", "Bomba sumergible para aguas limpias, 2HP, 380V", "Grundfos", "SP 5-50", "Sumergible", "UNID", 1200.00, 15, 5, 1),
                ProductoEntity(3, "BOM-SUM-003", "Bomba Sumergible 3HP", "Bomba sumergible para aguas residuales con solidos", "Ebara", "Drainage DVS", "Sumergible", "UNID", 1800.00, 10, 3, 1)
            ))

            dao.insertEmpleados(listOf(
                EmpleadoEntity(1, 1, "Administrador del Sistema", "Administracion", "2022-03-01", "INDEFINIDO", 500.00),
                EmpleadoEntity(2, 2, "Supervisor de Operaciones", "Operaciones", "2022-06-15", "INDEFINIDO", 420.00),
                EmpleadoEntity(3, 3, "Supervisor Comercial", "Ventas", "2023-01-10", "INDEFINIDO", 400.00),
                EmpleadoEntity(4, 4, "Tecnico en Bombas Hidraulicas", "Mantenimiento", "2023-04-01", "INDEFINIDO", 280.00)
            ))
        }
    }
}
