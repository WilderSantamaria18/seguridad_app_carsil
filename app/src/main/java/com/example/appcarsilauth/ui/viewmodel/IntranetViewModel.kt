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
import android.content.Intent
import android.net.Uri
import com.example.appcarsilauth.data.remote.RailwayDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.mindrot.jbcrypt.BCrypt

class IntranetViewModel(private val intranetDao: IntranetDao) : ViewModel() {

    private val _clientes = MutableStateFlow<List<ClienteEntity>>(emptyList())
    val clientes: StateFlow<List<ClienteEntity>> = _clientes.asStateFlow()

    private val _productos = MutableStateFlow<List<ProductoEntity>>(emptyList())
    val productos: StateFlow<List<ProductoEntity>> = _productos.asStateFlow()

    private val _lastProforma = MutableStateFlow<ProformaEntity?>(null)
    val lastProforma: StateFlow<ProformaEntity?> = _lastProforma.asStateFlow()

    private val _proformas = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val proformas: StateFlow<List<Map<String, Any>>> = _proformas.asStateFlow()

    private val _facturas = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val facturas: StateFlow<List<Map<String, Any>>> = _facturas.asStateFlow()

    private val _usuarios = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val usuarios: StateFlow<List<Map<String, Any>>> = _usuarios.asStateFlow()

    private val _roles = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val roles: StateFlow<List<Map<String, Any>>> = _roles.asStateFlow()

    private val _proformaGenerada = MutableStateFlow<Boolean>(false)
    val proformaGenerada: StateFlow<Boolean> = _proformaGenerada.asStateFlow()

    private val _allowedMenus = MutableStateFlow<List<String>>(emptyList())
    val allowedMenus: StateFlow<List<String>> = _allowedMenus.asStateFlow()

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    private val _dashboardStats = MutableStateFlow<Map<String, Int>>(emptyMap())
    val dashboardStats: StateFlow<Map<String, Int>> = _dashboardStats.asStateFlow()

    private val _proformaActivity = MutableStateFlow<List<Int>>(emptyList())
    val proformaActivity: StateFlow<List<Int>> = _proformaActivity.asStateFlow()

    private val _activityLabels = MutableStateFlow<List<String>>(listOf("L", "M", "M", "J", "V", "S", "D"))
    val activityLabels: StateFlow<List<String>> = _activityLabels.asStateFlow()

    private val _activityFilter = MutableStateFlow("WEEK")
    val activityFilter: StateFlow<String> = _activityFilter.asStateFlow()

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // --- REPORTES ANALÍTICOS ---
    private val _reportKPIs = MutableStateFlow<Map<String, Any>>(emptyMap())
    val reportKPIs: StateFlow<Map<String, Any>> = _reportKPIs.asStateFlow()

    private val _proformasByState = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val proformasByState: StateFlow<List<Map<String, Any>>> = _proformasByState.asStateFlow()

    private val _topClients = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val topClients: StateFlow<List<Map<String, Any>>> = _topClients.asStateFlow()

    private val _salesByMonth = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val salesByMonth: StateFlow<List<Map<String, Any>>> = _salesByMonth.asStateFlow()

    private var usersSearchCache: String = ""
    private var usersStatusFilterCache: String = "TODOS"
    private var weeklyActivityCache: List<Int> = List(7) { 0 }
    private var weeklyLabelsCache: List<String> = listOf("Lun", "Mar", "Mie", "Jue", "Vie", "Sab", "Dom")
    private var monthlyActivityCache: List<Int> = List(6) { 0 }
    private var monthlyLabelsCache: List<String> = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun")
    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")

    fun loadAllowedMenus(roleId: Int) {
        viewModelScope.launch {
            _allowedMenus.value = intranetDao.getMenusByRole(roleId)
        }
    }

    fun setActivityFilter(filter: String) {
        val normalized = filter.uppercase()
        if (normalized != "WEEK" && normalized != "MONTH") return
        if (_activityFilter.value == normalized) return

        _activityFilter.value = normalized
        if (normalized == "MONTH") {
            _activityLabels.value = monthlyLabelsCache
            _proformaActivity.value = monthlyActivityCache
        } else {
            _activityLabels.value = weeklyLabelsCache
            _proformaActivity.value = weeklyActivityCache
        }
    }

    fun loadIntranetData(search: String = "") {
        viewModelScope.launch {
            _isLoading.value = true
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
            _isLoading.value = false
        }
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _isLoading.value = true
            val currentFilter = _activityFilter.value
            
            _dashboardStats.value = RailwayDatabase.getDashboardStats()

            val weeklySeries = RailwayDatabase.getProformaActivityWeekly(7)
            weeklyLabelsCache = weeklySeries.labels
            weeklyActivityCache = weeklySeries.values

            val monthlySeries = RailwayDatabase.getProformaActivityMonthly(6)
            monthlyLabelsCache = monthlySeries.labels
            monthlyActivityCache = monthlySeries.values

            _activityFilter.value = if (currentFilter == "MONTH") "MONTH" else "WEEK"
            if (_activityFilter.value == "MONTH") {
                _activityLabels.value = monthlyLabelsCache
                _proformaActivity.value = monthlyActivityCache
            } else {
                _activityLabels.value = weeklyLabelsCache
                _proformaActivity.value = weeklyActivityCache
            }

            loadAllProformas()
            _isLoading.value = false
        }
    }

    fun refreshDashboardRealtime() {
        viewModelScope.launch {
            runCatching {
                _dashboardStats.value = RailwayDatabase.getDashboardStats()
                _proformas.value = RailwayDatabase.getProformas()
            }
        }
    }

    fun loadAllProformas(search: String = "") {
        viewModelScope.launch {
            _proformas.value = RailwayDatabase.getProformas(search)
        }
    }

    fun loadAllFacturas(search: String = "", estadoFiltro: String = "TODOS") {
        viewModelScope.launch {
            _isLoading.value = true
            _facturas.value = RailwayDatabase.getFacturas(search, estadoFiltro)
            _isLoading.value = false
        }
    }

    fun loadRolesUsuarios() {
        viewModelScope.launch {
            _roles.value = RailwayDatabase.getRoles()
        }
    }

    fun loadAllUsuarios(search: String = "", estadoFiltro: String = "TODOS") {
        viewModelScope.launch {
            _isLoading.value = true
            usersSearchCache = search
            usersStatusFilterCache = estadoFiltro
            _usuarios.value = RailwayDatabase.getUsers(search, estadoFiltro)
            _isLoading.value = false
        }
    }

    fun registrarUsuario(
        nombres: String,
        apellidos: String,
        tipoDocumento: String,
        numeroDocumento: String,
        correo: String,
        telefono: String,
        direccion: String,
        idRol: Int,
        estado: Int,
        clave: String,
        confirmarClave: String
    ) {
        viewModelScope.launch {
            val nom = nombres.trim()
            val ape = apellidos.trim()
            val tipoDoc = tipoDocumento.trim().ifEmpty { "DNI" }
            val numeroDoc = numeroDocumento.trim()
            val email = correo.trim().lowercase()
            val phone = telefono.trim()
            val addr = direccion.trim()
            val pass = clave.trim()
            val passConfirm = confirmarClave.trim()

            if (nom.isEmpty() || ape.isEmpty() || numeroDoc.isEmpty() || email.isEmpty()) {
                _uiMessage.value = "Nombres, apellidos, documento y correo son obligatorios."
                return@launch
            }
            if (!emailRegex.matches(email)) {
                _uiMessage.value = "El correo electrónico no tiene un formato válido."
                return@launch
            }
            if (pass.length < 6) {
                _uiMessage.value = "La contraseña debe tener al menos 6 caracteres."
                return@launch
            }
            if (pass != passConfirm) {
                _uiMessage.value = "La confirmación de contraseña no coincide."
                return@launch
            }

            _isLoading.value = true
            try {
                if (RailwayDatabase.isUserDocumentInUse(numeroDoc)) {
                    _uiMessage.value = "El número de documento ya está registrado."
                    return@launch
                }
                if (RailwayDatabase.isUserEmailInUse(email)) {
                    _uiMessage.value = "El correo electrónico ya está registrado."
                    return@launch
                }

                val userMap = mapOf(
                    "Nombres" to nom,
                    "Apellidos" to ape,
                    "TipoDocumento" to tipoDoc,
                    "NumeroDocumento" to numeroDoc,
                    "Correo" to email,
                    "Clave" to BCrypt.hashpw(pass, BCrypt.gensalt()),
                    "IdRol" to idRol,
                    "Estado" to estado,
                    "Telefono" to phone,
                    "Direccion" to addr
                )

                val remoteId = RailwayDatabase.insertUser(userMap)
                _uiMessage.value = if (remoteId > 0) {
                    loadAllUsuarios(usersSearchCache, usersStatusFilterCache)
                    "Usuario registrado correctamente."
                } else {
                    "No se pudo registrar el usuario en la nube."
                }
            } catch (e: Exception) {
                _uiMessage.value = "Error al registrar usuario: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun actualizarUsuario(
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
        nuevaClave: String,
        confirmarClave: String
    ) {
        viewModelScope.launch {
            val nom = nombres.trim()
            val ape = apellidos.trim()
            val tipoDoc = tipoDocumento.trim().ifEmpty { "DNI" }
            val numeroDoc = numeroDocumento.trim()
            val email = correo.trim().lowercase()
            val phone = telefono.trim()
            val addr = direccion.trim()
            val pass = nuevaClave.trim()
            val passConfirm = confirmarClave.trim()

            if (nom.isEmpty() || ape.isEmpty() || numeroDoc.isEmpty() || email.isEmpty()) {
                _uiMessage.value = "Nombres, apellidos, documento y correo son obligatorios."
                return@launch
            }
            if (!emailRegex.matches(email)) {
                _uiMessage.value = "El correo electrónico no tiene un formato válido."
                return@launch
            }

            val wantsPasswordUpdate = pass.isNotEmpty() || passConfirm.isNotEmpty()
            if (wantsPasswordUpdate && pass.length < 6) {
                _uiMessage.value = "La nueva contraseña debe tener al menos 6 caracteres."
                return@launch
            }
            if (wantsPasswordUpdate && pass != passConfirm) {
                _uiMessage.value = "La confirmación de contraseña no coincide."
                return@launch
            }

            _isLoading.value = true
            try {
                if (RailwayDatabase.isUserDocumentInUse(numeroDoc, idUsuario)) {
                    _uiMessage.value = "El número de documento ya pertenece a otro usuario."
                    return@launch
                }
                if (RailwayDatabase.isUserEmailInUse(email, idUsuario)) {
                    _uiMessage.value = "El correo electrónico ya pertenece a otro usuario."
                    return@launch
                }

                val hash = if (wantsPasswordUpdate) BCrypt.hashpw(pass, BCrypt.gensalt()) else null
                val updated = RailwayDatabase.updateUser(
                    idUsuario = idUsuario,
                    nombres = nom,
                    apellidos = ape,
                    tipoDocumento = tipoDoc,
                    numeroDocumento = numeroDoc,
                    correo = email,
                    telefono = phone,
                    direccion = addr,
                    idRol = idRol,
                    estado = estado,
                    newPasswordHash = hash
                )

                _uiMessage.value = if (updated) {
                    loadAllUsuarios(usersSearchCache, usersStatusFilterCache)
                    "Usuario actualizado correctamente."
                } else {
                    "No se pudo actualizar el usuario."
                }
            } catch (e: Exception) {
                _uiMessage.value = "Error al actualizar usuario: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun actualizarEstadoUsuario(idUsuario: Int, nuevoEstado: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val updated = RailwayDatabase.updateUserStatus(idUsuario, nuevoEstado)
                _uiMessage.value = if (updated) {
                    loadAllUsuarios(usersSearchCache, usersStatusFilterCache)
                    if (nuevoEstado == 1) "Usuario activado correctamente." else "Usuario inactivado correctamente."
                } else {
                    "No se pudo actualizar el estado del usuario."
                }
            } catch (e: Exception) {
                _uiMessage.value = "Error al cambiar estado: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun descargarPdfFactura(context: android.content.Context, idFactura: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val fullFact = RailwayDatabase.getFacturaById(idFactura)
                val details  = RailwayDatabase.getFacturaDetails(idFactura)

                if (fullFact != null) {
                    val uri = com.example.appcarsilauth.util.PdfGenerator.generatePremiumFacturaPdf(
                        context = context,
                        factura = fullFact,
                        detalles = details
                    )
                    if (uri != null) {
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, "application/pdf")
                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(android.content.Intent.createChooser(intent, "Abrir Factura CARSIL"))
                    }
                } else {
                    _uiMessage.value = "No se pudo recuperar la factura de la nube."
                }
            } catch (e: Exception) {
                _uiMessage.value = "Error al descargar PDF: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // --- CARGAR REPORTES ---
    fun loadReportData() {
        viewModelScope.launch {
            _isLoading.value = true
            _reportKPIs.value = RailwayDatabase.getReportKPIs()
            _proformasByState.value = RailwayDatabase.getProformasByState()
            _topClients.value = RailwayDatabase.getTopClients(7)
            _salesByMonth.value = RailwayDatabase.getSalesByMonth()
            _isLoading.value = false
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
            refreshDashboardRealtime()
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
            refreshDashboardRealtime()
            _isLoading.value = false
        }
    }

    // CARSIL-POL-INT: La proforma no mueve inventario; el stock se descuenta al facturar.
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
                }

                // 3. RESPALDO LOCAL (OPCIONAL/HISTORICO)
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
                _uiMessage.value = if (remoteId > 0) {
                    "Proforma generada y sincronizada."
                } else {
                    "Proforma generada en modo local."
                }
                loadIntranetData() 
                refreshDashboardRealtime()
            } else {
                _uiMessage.value = "Stock insuficiente disponible."
            }
        }
    }

    // CARSIL-POL-ASIS: Control de asistencia nativo sincronizado
    private val _asistenciaState = MutableStateFlow<Map<String, Any?>?>(null)
    val asistenciaState: StateFlow<Map<String, Any?>?> = _asistenciaState.asStateFlow()

    private val _allAttendances = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val allAttendances: StateFlow<List<Map<String, Any>>> = _allAttendances.asStateFlow()

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

    fun loadAllAttendances(
        fecha: String? = null,
        search: String = "",
        estadoFiltro: String = "TODOS"
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val dateToFetch = fecha ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            _allAttendances.value = RailwayDatabase.getAttendances(
                fecha = dateToFetch,
                search = search,
                estadoFiltro = estadoFiltro
            )
            _isLoading.value = false
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

    fun descargarPdfProforma(context: android.content.Context, idProforma: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val fullProf = RailwayDatabase.getProformaById(idProforma)
                val details = RailwayDatabase.getProformaDetails(idProforma)
                
                if (fullProf != null) {
                    val uri = com.example.appcarsilauth.util.PdfGenerator.generatePremiumProformaPdf(
                        context = context,
                        proforma = fullProf,
                        detalles = details
                    )
                    
                    if (uri != null) {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, "application/pdf")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(Intent.createChooser(intent, "Abrir Proforma CARSIL"))
                    }
                } else {
                    _uiMessage.value = "No se pudo recuperar la proforma de la nube."
                }
            } catch (e: Exception) {
                _uiMessage.value = "Error al descargar PDF: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearSessionState() {
        _allowedMenus.value = emptyList()
        _proformaGenerada.value = false
        _lastProforma.value = null
        _dashboardStats.value = emptyMap()
        _proformaActivity.value = emptyList()
        _activityLabels.value = listOf("L", "M", "M", "J", "V", "S", "D")
        _activityFilter.value = "WEEK"
        weeklyActivityCache = List(7) { 0 }
        weeklyLabelsCache = listOf("Lun", "Mar", "Mie", "Jue", "Vie", "Sab", "Dom")
        monthlyActivityCache = List(6) { 0 }
        monthlyLabelsCache = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun")
        _asistenciaState.value = null
        _allAttendances.value = emptyList()
        _usuarios.value = emptyList()
        _roles.value = emptyList()
        usersSearchCache = ""
        usersStatusFilterCache = "TODOS"
    }
}
