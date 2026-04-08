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
        UsuarioEntity::class,
        ClienteEntity::class,
        ProductoEntity::class,
        ProformaEntity::class,
        EmpleadoEntity::class,
        AsistenciaEntity::class
    ],
    version = 8,
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
                    
                    dao.insertUsuarios(listOf(
                        UsuarioEntity(1, "Carlos Enrique", "csilva@carsil.com", "\$2a\$10\$qb/OBcDiKog7VNClJFXdbukdiQqFiJbXkzMKHUxcvgQihE7zVuLdS", 1),
                        UsuarioEntity(2, "Rosa Amelia", "rparedes@carsil.com", "\$2a\$10\$qb/OBcDiKog7VNClJFXdbukdiQqFiJbXkzMKHUxcvgQihE7zVuLdS", 3),
                        UsuarioEntity(3, "Miguel Ángel", "mfuentes@carsil.com", "\$2a\$10\$qb/OBcDiKog7VNClJFXdbukdiQqFiJbXkzMKHUxcvgQihE7zVuLdS", 3),
                        UsuarioEntity(4, "Jean Pierre", "jenriquez@carsil.com", "\$2a\$10\$qb/OBcDiKog7VNClJFXdbukdiQqFiJbXkzMKHUxcvgQihE7zVuLdS", 2)
                    ))
                    
                    dao.insertClientes(listOf(
                        ClienteEntity(1, "20501234567", "Constructora Los Andes S.A.C."),
                        ClienteEntity(2, "20602345678", "Minera El Dorado S.A."),
                        ClienteEntity(3, "20703456789", "Agroindustrial del Pacifico S.A.C.")
                    ))

                    dao.insertProductos(listOf(
                        ProductoEntity(1, "Bomba Sumergible 1HP", 850.00, 25),
                        ProductoEntity(2, "Bomba Sumergible 2HP", 1200.00, 15),
                        ProductoEntity(3, "Bomba Sumergible 3HP", 1800.00, 10),
                        ProductoEntity(4, "Tablero de Control CARSIL-V1", 450.00, 5),
                        ProductoEntity(5, "Sensor de Nivel Hidráulico", 120.00, 50)
                    ))

                    // Empleados de CARSIL (vinculados al Usuario)
                    dao.insertEmpleados(listOf(
                        EmpleadoEntity(1, 1, "Administrador del Sistema", "Administración"),
                        EmpleadoEntity(2, 2, "Supervisor de Operaciones", "Operaciones"),
                        EmpleadoEntity(3, 3, "Supervisor Comercial", "Ventas"),
                        EmpleadoEntity(4, 4, "Técnico en Bombas Hidráulicas", "Mantenimiento")
                    ))
                }
            }
        }
    }
}
