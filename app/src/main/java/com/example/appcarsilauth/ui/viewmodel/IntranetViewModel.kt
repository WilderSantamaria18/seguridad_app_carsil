package com.example.appcarsilauth.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appcarsilauth.data.local.dao.IntranetDao
import com.example.appcarsilauth.data.local.entity.ClienteEntity
import com.example.appcarsilauth.data.local.entity.ProductoEntity
import com.example.appcarsilauth.data.local.entity.ProformaEntity
import com.example.appcarsilauth.data.local.entity.DetalleProformaEntity
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

    private val _allowedMenus = MutableStateFlow<List<String>>(emptyList())
    val allowedMenus: StateFlow<List<String>> = _allowedMenus.asStateFlow()

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    fun loadAllowedMenus(roleId: Int) {
        viewModelScope.launch {
            _allowedMenus.value = intranetDao.getMenusByRole(roleId)
        }
    }

    fun loadIntranetData() {
        viewModelScope.launch {
            _clientes.value = intranetDao.getAllClientes()
            _productos.value = intranetDao.getAllProductos()
        }
    }

    fun clearUiMessage() {
        _uiMessage.value = null
    }

    fun registrarCliente(
        documento: String,
        razonSocial: String,
        direccion: String,
        telefono: String,
        celular: String,
        email: String,
        contacto: String
    ) {
        viewModelScope.launch {
            val doc = documento.trim()
            val razon = razonSocial.trim()

            if (doc.isEmpty() || razon.isEmpty()) {
                _uiMessage.value = "Documento y razon social son obligatorios."
                return@launch
            }

            val nextId = intranetDao.getMaxClienteId() + 1
            intranetDao.insertCliente(
                ClienteEntity(
                    IdCliente = nextId,
                    Documento = doc,
                    RazonSocial = razon,
                    Direccion = direccion.trim().ifEmpty { null },
                    Telefono = telefono.trim().ifEmpty { null },
                    Celular = celular.trim().ifEmpty { null },
                    Email = email.trim().ifEmpty { null },
                    Contacto = contacto.trim().ifEmpty { null },
                    Estado = 1
                )
            )

            _uiMessage.value = "Cliente registrado correctamente."
            loadIntranetData()
        }
    }

    fun registrarProducto(
        codigo: String,
        nombre: String,
        descripcion: String,
        marca: String,
        modelo: String,
        tipo: String,
        precioUnitario: String,
        stock: String,
        stockMinimo: String
    ) {
        viewModelScope.launch {
            val cod = codigo.trim()
            val nom = nombre.trim()
            val precio = precioUnitario.trim().toDoubleOrNull()
            val stockActual = stock.trim().toIntOrNull()
            val stockMin = stockMinimo.trim().toIntOrNull() ?: 5

            if (cod.isEmpty() || nom.isEmpty() || precio == null || stockActual == null) {
                _uiMessage.value = "Codigo, nombre, precio y stock son obligatorios."
                return@launch
            }

            val nextId = intranetDao.getMaxProductoId() + 1
            intranetDao.insertProducto(
                ProductoEntity(
                    IdProducto = nextId,
                    Codigo = cod,
                    Nombre = nom,
                    Descripcion = descripcion.trim().ifEmpty { null },
                    Marca = marca.trim().ifEmpty { null },
                    Modelo = modelo.trim().ifEmpty { null },
                    Tipo = tipo.trim().ifEmpty { null },
                    UnidadMedida = "UNID",
                    PrecioUnitario = precio,
                    Stock = stockActual,
                    StockMinimo = stockMin,
                    Estado = 1
                )
            )

            _uiMessage.value = "Producto registrado correctamente."
            loadIntranetData()
        }
    }

    // CARSIL-POL-INT: Precisión Financiera y Control de Inventario
    fun generarProforma(idUsuario: Int, idCliente: Int, idProducto: Int, cantidad: Int) {
        viewModelScope.launch {
            val producto = intranetDao.getAllProductos().find { it.IdProducto == idProducto }
            if (producto != null) {
                if (producto.Stock >= cantidad) {
                    val subtotal = producto.PrecioUnitario * cantidad
                    val totalIgv = subtotal * 0.18
                    val total = subtotal + totalIgv
                    val fechaEmision = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    
                    val result = intranetDao.updateStockReduction(idProducto, cantidad)
                    if (result > 0) {
                        val newProforma = ProformaEntity(
                            Codigo = "PRO-${System.currentTimeMillis()}",
                            IdUsuario = idUsuario,
                            IdCliente = idCliente,
                            IdEmpresa = 1,
                            FechaEmision = fechaEmision,
                            Referencia = "Proforma desde app movil",
                            ValidezOferta = 10,
                            TiempoEntrega = "5 dias habiles",
                            LugarEntrega = "Lima",
                            Garantia = "12 meses",
                            FormaPago = "Contado",
                            PorcentajeIGV = 18.0,
                            SubTotal = subtotal,
                            TotalIGV = totalIgv,
                            Total = total,
                            Estado = "PENDIENTE"
                        )
                        val proformaId = intranetDao.insertProforma(newProforma)

                        intranetDao.insertDetalleProforma(
                            DetalleProformaEntity(
                                IdProforma = proformaId.toInt(),
                                IdProducto = idProducto,
                                Cantidad = cantidad.toDouble(),
                                PrecioUnitario = producto.PrecioUnitario,
                                Total = subtotal,
                                DescripcionAdicional = producto.Descripcion
                            )
                        )
                        
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
                            JornadaLaboral = "COMPLETA",
                            HoraEntrada = horaActual,
                            HoraSalida = null,
                            Estado = "PRESENTE",
                            TipoAsistencia = "REGULAR"
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

    fun clearSessionState() {
        _allowedMenus.value = emptyList()
        _proformaGenerada.value = false
        _lastProforma.value = null
        _asistenciaState.value = null
    }
}
