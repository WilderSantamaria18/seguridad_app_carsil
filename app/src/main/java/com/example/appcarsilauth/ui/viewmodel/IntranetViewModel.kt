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
import com.example.appcarsilauth.data.remote.RailwayDatabase
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

    private val _proformas = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val proformas: StateFlow<List<Map<String, Any>>> = _proformas.asStateFlow()

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

    fun loadIntranetData(search: String = "") {
        viewModelScope.launch {
            // TRAER CLIENTES DE RAILWAY
            val remoteClients = RailwayDatabase.getClients(search)
            _clientes.value = remoteClients.map { map ->
                ClienteEntity(
                    IdCliente = map["IdCliente"] as Int,
                    Documento = map["Documento"] as String,
                    RazonSocial = map["RazonSocial"] as String,
                    Direccion = map["Direccion"] as String,
                    Telefono = map["Telefono"] as String,
                    Celular = map["Celular"] as String,
                    Email = map["Email"] as String,
                    Contacto = map["NombreContacto"] as String,
                    Estado = 1
                )
            }
            
            // TRAER PRODUCTOS DE RAILWAY
            val remoteProducts = RailwayDatabase.getProducts(search)
            _productos.value = remoteProducts.map { map ->
                ProductoEntity(
                    IdProducto = map["IdProducto"] as Int,
                    Codigo = map["Codigo"] as String,
                    Nombre = map["Nombre"] as String,
                    Marca = map["Marca"] as String,
                    PrecioUnitario = map["PrecioUnitario"] as Double,
                    Stock = map["Stock"] as Int,
                    Estado = 1
                )
            }
        }
    }

    fun loadAllProformas(search: String = "") {
        viewModelScope.launch {
            _proformas.value = RailwayDatabase.getProformas(search)
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

            // REGISTRAR EN NUBE (RAILWAY)
            val clientMap = mapOf(
                "Documento" to doc,
                "RazonSocial" to razon,
                "Direccion" to direccion.trim(),
                "Telefono" to telefono.trim(),
                "Celular" to celular.trim(),
                "Email" to email.trim(),
                "Contacto" to contacto.trim()
            )
            val remoteId = RailwayDatabase.insertClient(clientMap)

            // RESPALDO EN LOCAL
            val nextId = if (remoteId > 0) remoteId else (intranetDao.getMaxClienteId() + 1)
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

            _uiMessage.value = if (remoteId > 0) "Cliente registrado en la nube correctamente." else "Cliente registrado localmente (Falla en nube)."
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

            // REGISTRAR EN NUBE (RAILWAY)
            val prodMap = mapOf(
                "Codigo" to cod,
                "Nombre" to nom,
                "Descripcion" to descripcion.trim(),
                "Marca" to marca.trim(),
                "Modelo" to modelo.trim(),
                "Tipo" to tipo.trim(),
                "PrecioUnitario" to precio,
                "Stock" to stockActual,
                "StockMinimo" to stockMin
            )
            val remoteId = RailwayDatabase.insertProduct(prodMap)

            // RESPALDO EN LOCAL
            val nextId = if (remoteId > 0) remoteId else (intranetDao.getMaxProductoId() + 1)
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

            _uiMessage.value = if (remoteId > 0) "Producto sincronizado en Railway." else "Producto guardado local (Offline)."
            loadIntranetData()
        }
    }

    // CARSIL-POL-INT: Precisión Financiera y Control de Inventario Sincronizado
    fun generarProforma(idUsuario: Int, idCliente: Int, idProducto: Int, cantidad: Int) {
        viewModelScope.launch {
            val producto = _productos.value.find { it.IdProducto == idProducto } ?: return@launch
            
            if (producto.Stock >= cantidad) {
                val subtotal = producto.PrecioUnitario * cantidad
                val totalIgv = subtotal * 0.18
                val total = subtotal + totalIgv
                val fechaEmision = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val codigoProf = "PRO-${System.currentTimeMillis()}"

                // 1. REGISTRAR PROFORMA EN RAILWAY
                val profMap = mapOf(
                    "Codigo" to codigoProf,
                    "IdUsuario" to idUsuario,
                    "IdCliente" to idCliente,
                    "FechaEmision" to fechaEmision,
                    "Referencia" to "App Movil Railway",
                    "SubTotal" to subtotal,
                    "TotalIGV" to totalIgv,
                    "Total" to total
                )
                val remoteId = RailwayDatabase.insertProforma(profMap)

                if (remoteId > 0) {
                    // 2. REGISTRAR DETALLE EN RAILWAY
                    val detMap = mapOf(
                        "IdProforma" to remoteId,
                        "IdProducto" to idProducto,
                        "Cantidad" to cantidad.toDouble(),
                        "PrecioUnitario" to producto.PrecioUnitario,
                        "Total" to subtotal,
                        "Descripcion" to producto.Nombre
                    )
                    RailwayDatabase.insertProformaDetail(detMap)
                    
                    // 3. ACTUALIZAR STOCK EN RAILWAY
                    RailwayDatabase.updateProductStock(idProducto, cantidad)
                }

                // 4. RESPALDO LOCAL (OPCIONAL/HISTORICO)
                val resultLocal = intranetDao.updateStockReduction(idProducto, cantidad)
                if (resultLocal > 0) {
                    val newProforma = ProformaEntity(
                        Codigo = codigoProf,
                        IdUsuario = idUsuario,
                        IdCliente = idCliente,
                        IdEmpresa = 1,
                        FechaEmision = fechaEmision,
                        Referencia = "Copia local sincronizada",
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
                            DescripcionAdicional = producto.Nombre
                        )
                    )
                    
                    _lastProforma.value = newProforma.copy(IdProforma = if (remoteId > 0) remoteId else proformaId.toInt())
                    _proformaGenerada.value = true
                    _uiMessage.value = "Proforma generada y sincronizada."
                }
                loadIntranetData() 
            } else {
                _uiMessage.value = "Stock insuficiente disponible."
            }
        }
    }

    // CARSIL-POL-ASIS: Control de asistencia nativo sincronizado
    private val _asistenciaState = MutableStateFlow<Map<String, Any?>?>(null)
    val asistenciaState: StateFlow<Map<String, Any?>?> = _asistenciaState.asStateFlow()

    fun cargarAsistenciaHoy(idUsuario: Int) {
        viewModelScope.launch {
            val idEmpleado = RailwayDatabase.getEmpleadoByUserId(idUsuario)
            if (idEmpleado > 0) {
                val fechaDeHoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                _asistenciaState.value = RailwayDatabase.getAsistenciaHoy(idEmpleado, fechaDeHoy)
            } else {
                // Fallback local
                val empleadoLocal = intranetDao.getEmpleadoByUsuario(idUsuario)
                if (empleadoLocal != null) {
                    val fechaDeHoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    val localAsis = intranetDao.getAsistenciaHoy(empleadoLocal.IdEmpleado, fechaDeHoy)
                    _asistenciaState.value = if (localAsis != null) mapOf(
                        "HoraEntrada" to localAsis.HoraEntrada,
                        "HoraSalida" to localAsis.HoraSalida
                    ) else null
                }
            }
        }
    }

    fun registrarAsistencia(idUsuario: Int) {
        viewModelScope.launch {
            val idEmpleado = RailwayDatabase.getEmpleadoByUserId(idUsuario)
            val fechaDeHoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val horaActual = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            
            if (idEmpleado > 0) {
                val asistenciaActual = RailwayDatabase.getAsistenciaHoy(idEmpleado, fechaDeHoy)
                
                if (asistenciaActual == null) {
                    RailwayDatabase.insertAsistenciaEntry(idEmpleado, fechaDeHoy, horaActual)
                } else if (asistenciaActual["HoraSalida"] == null) {
                    RailwayDatabase.updateAsistenciaExit(idEmpleado, fechaDeHoy, horaActual)
                }
                // Recargar de nube
                _asistenciaState.value = RailwayDatabase.getAsistenciaHoy(idEmpleado, fechaDeHoy)
            }
            
            // SIEMPRE RESPALDO LOCAL
            val empleadoLocal = intranetDao.getEmpleadoByUsuario(idUsuario)
            if (empleadoLocal != null) {
                val localAsis = intranetDao.getAsistenciaHoy(empleadoLocal.IdEmpleado, fechaDeHoy)
                if (localAsis == null) {
                    intranetDao.insertAsistencia(
                        AsistenciaEntity(
                            IdEmpleado = empleadoLocal.IdEmpleado,
                            Fecha = fechaDeHoy,
                            JornadaLaboral = "COMPLETA",
                            HoraEntrada = horaActual,
                            HoraSalida = null,
                            Estado = "PRESENTE",
                            TipoAsistencia = "REGULAR"
                        )
                    )
                } else if (localAsis.HoraSalida == null) {
                    intranetDao.updateHoraSalida(empleadoLocal.IdEmpleado, fechaDeHoy, horaActual)
                }
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
