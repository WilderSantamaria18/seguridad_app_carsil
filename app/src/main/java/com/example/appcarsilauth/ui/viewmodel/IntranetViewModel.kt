package com.example.appcarsilauth.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appcarsilauth.data.local.dao.IntranetDao
import com.example.appcarsilauth.data.local.entity.ClienteEntity
import com.example.appcarsilauth.data.local.entity.ProductoEntity
import com.example.appcarsilauth.data.local.entity.ProformaEntity
import com.example.appcarsilauth.data.local.entity.AsistenciaEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class IntranetViewModel(private val intranetDao: IntranetDao) : ViewModel() {

    private val _clientes = MutableStateFlow<List<ClienteEntity>>(emptyList())
    val clientes: StateFlow<List<ClienteEntity>> = _clientes.asStateFlow()

    private val _productos = MutableStateFlow<List<ProductoEntity>>(emptyList())
    val productos: StateFlow<List<ProductoEntity>> = _productos.asStateFlow()

    private val _lastProforma = MutableStateFlow<ProformaEntity?>(null)
    val lastProforma: StateFlow<ProformaEntity?> = _lastProforma.asStateFlow()

    private val _proformaGenerada = MutableStateFlow<Boolean>(false)
    val proformaGenerada: StateFlow<Boolean> = _proformaGenerada.asStateFlow()

    fun loadIntranetData() {
        viewModelScope.launch {
            _clientes.value = intranetDao.getAllClientes()
            _productos.value = intranetDao.getAllProductos()
        }
    }

    // CARSIL-POL-INT: Precisión Financiera y Control de Inventario
    fun generarProforma(idUsuario: Int, idCliente: Int, idProducto: Int, cantidad: Int) {
        viewModelScope.launch {
            val producto = intranetDao.getAllProductos().find { it.IdProducto == idProducto }
            if (producto != null) {
                if (producto.Stock >= cantidad) {
                    val subtotal = producto.PrecioUnitario * cantidad
                    val totalIGV = subtotal * 1.18
                    
                    val result = intranetDao.updateStockReduction(idProducto, cantidad)
                    if (result > 0) {
                        val newProforma = ProformaEntity(
                            Codigo = "PRO-${System.currentTimeMillis()}",
                            IdUsuario = idUsuario,
                            IdCliente = idCliente,
                            Total = totalIGV
                        )
                        val proformaId = intranetDao.insertProforma(newProforma)
                        
                        // Obtenemos la proforma con su ID generado para el PDF
                        _lastProforma.value = newProforma.copy(IdProforma = proformaId.toInt())
                        _proformaGenerada.value = true
                        loadIntranetData() 
                    }
                } else {
                    // Aquí se podría emitir un error de stock insuficiente
                }
            }
        }
    }

    // CARSIL-POL-ASIS: Control de asistencia nativo
    private val _asistenciaState = MutableStateFlow<AsistenciaEntity?>(null)
    val asistenciaState: StateFlow<AsistenciaEntity?> = _asistenciaState.asStateFlow()

    fun cargarAsistenciaHoy(idUsuario: Int) {
        viewModelScope.launch {
            val empleado = intranetDao.getEmpleadoByUsuario(idUsuario)
            if (empleado != null) {
                val fechaDeHoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                _asistenciaState.value = intranetDao.getAsistenciaHoy(empleado.IdEmpleado, fechaDeHoy)
            }
        }
    }

    fun registrarAsistencia(idUsuario: Int) {
        viewModelScope.launch {
            val empleado = intranetDao.getEmpleadoByUsuario(idUsuario)
            if (empleado != null) {
                val fechaDeHoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val horaActual = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                
                val asistenciaActual = intranetDao.getAsistenciaHoy(empleado.IdEmpleado, fechaDeHoy)
                
                if (asistenciaActual == null) {
                    // Marcar Entrada
                    intranetDao.insertAsistencia(
                        AsistenciaEntity(
                            IdEmpleado = empleado.IdEmpleado,
                            Fecha = fechaDeHoy,
                            HoraEntrada = horaActual,
                            HoraSalida = null
                        )
                    )
                } else if (asistenciaActual.HoraSalida == null) {
                    // Marcar Salida
                    intranetDao.updateHoraSalida(empleado.IdEmpleado, fechaDeHoy, horaActual)
                }
                
                // Recargar estado
                _asistenciaState.value = intranetDao.getAsistenciaHoy(empleado.IdEmpleado, fechaDeHoy)
            }
        }
    }

    fun resetState() {
        _proformaGenerada.value = false
    }
}
