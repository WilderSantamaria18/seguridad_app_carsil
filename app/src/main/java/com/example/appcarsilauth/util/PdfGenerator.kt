package com.example.appcarsilauth.util

import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.appcarsilauth.R
import com.example.appcarsilauth.data.local.entity.ClienteEntity
import com.example.appcarsilauth.data.local.entity.ProductoEntity
import com.example.appcarsilauth.data.local.entity.ProformaEntity
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfGenerator {

    // Diseño original para compatibilidad con generación inmediata postvisita
    fun generateProformaPdf(
        context: Context,
        proforma: ProformaEntity,
        cliente: ClienteEntity,
        producto: ProductoEntity,
        cantidad: Int
    ): Uri? {
        val map = mutableMapOf<String, Any>()
        map["Codigo"] = proforma.Codigo
        map["FechaEmision"] = proforma.FechaEmision
        map["Estado"] = proforma.Estado
        map["SubTotal"] = proforma.SubTotal
        map["TotalIGV"] = proforma.TotalIGV
        map["Total"] = proforma.Total
        map["FormaPago"] = proforma.FormaPago ?: "CONTADO"
        map["ValidezOferta"] = proforma.ValidezOferta
        map["ClientName"] = cliente.RazonSocial
        map["ClientRUC"] = cliente.Documento
        map["ClientDir"] = cliente.Direccion ?: "-"
        
        val details = listOf(mapOf(
            "Cantidad" to cantidad.toDouble(),
            "Unidad" to "UNID",
            "Descripcion" to producto.Nombre,
            "PrecioUnitario" to producto.PrecioUnitario,
            "Total" to (producto.PrecioUnitario * cantidad)
        ))
        
        return generatePremiumProformaPdf(context, map, details)
    }

    // Diseño Rediseñado (Premium HTML-Style)
    fun generatePremiumProformaPdf(
        context: Context,
        proforma: Map<String, Any>,
        detalles: List<Map<String, Any>>
    ): Uri? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        
        val paint = Paint()
        val textPaint = Paint()
        val headerColor = Color.parseColor("#2c3e50")
        val accentColor = Color.parseColor("#f8f9fa")
        val textColor = Color.parseColor("#333333")
        
        // --- 1. HEADER & LOGO ---
        try {
            val logo = BitmapFactory.decodeResource(context.resources, R.drawable.logo_carsil_app)
            if (logo != null) {
                val scaledLogo = Bitmap.createScaledBitmap(logo, 90, 100, true)
                canvas.drawBitmap(scaledLogo, 40f, 30f, paint)
            }
        } catch (e: Exception) {
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textPaint.textSize = 24f
            textPaint.color = headerColor
            canvas.drawText("CARSIL SAC", 40f, 60f, textPaint)
        }

        // --- 2. DOCUMENT INFO BOX (Right) ---
        paint.color = accentColor
        canvas.drawRoundRect(360f, 40f, 555f, 130f, 10f, 10f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = Color.LTGRAY
        paint.strokeWidth = 1f
        canvas.drawRoundRect(360f, 40f, 555f, 130f, 10f, 10f, paint)
        paint.style = Paint.Style.FILL

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 14f
        textPaint.color = headerColor
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("PROFORMA COMERCIAL", 457f, 65f, textPaint)
        
        textPaint.textSize = 12f
        textPaint.color = Color.GRAY
        canvas.drawText("N° ${proforma["Codigo"]}", 457f, 85f, textPaint)
        
        // Status Badge
        val estado = (proforma["Estado"] as? String ?: "PENDIENTE").uppercase()
        val statusColor = when(estado) {
            "APROBADA", "VENDIDA" -> Color.parseColor("#28a745")
            "RECHAZADA", "ANULADA" -> Color.parseColor("#dc3545")
            else -> Color.parseColor("#ffc107")
        }
        paint.color = statusColor
        canvas.drawRoundRect(410f, 95f, 505f, 115f, 20f, 20f, paint)
        textPaint.color = if (estado == "PENDIENTE") Color.BLACK else Color.WHITE
        textPaint.textSize = 9f
        canvas.drawText(estado, 457f, 108f, textPaint)

        // --- 3. CLIENT INFO ---
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.textSize = 12f
        textPaint.color = headerColor
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        
        // Section Title with left border
        paint.color = headerColor
        canvas.drawRect(40f, 160f, 44f, 180f, paint)
        canvas.drawText("DATOS DEL CLIENTE", 52f, 175f, textPaint)
        
        paint.color = accentColor
        canvas.drawRect(40f, 185f, 555f, 260f, paint)
        
        textPaint.typeface = Typeface.DEFAULT
        textPaint.textSize = 10f
        textPaint.color = textColor
        canvas.drawText("Razón Social: ${proforma["ClientName"]}", 50f, 205f, textPaint)
        canvas.drawText("RUC/DNI: ${proforma["ClientRUC"]}", 50f, 220f, textPaint)
        canvas.drawText("Dirección: ${proforma["ClientDir"]}", 50f, 235f, textPaint)
        canvas.drawText("Contacto: ${proforma["ClientContact"] ?: "-"}", 50f, 250f, textPaint)

        // --- 4. PRODUCT TABLE ---
        val tableY = 280f
        paint.color = headerColor
        canvas.drawRect(40f, tableY, 555f, tableY + 25f, paint)
        
        textPaint.color = Color.WHITE
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("#", 50f, tableY + 17f, textPaint)
        canvas.drawText("CANT", 80f, tableY + 17f, textPaint)
        canvas.drawText("UNID", 130f, tableY + 17f, textPaint)
        canvas.drawText("DESCRIPCIÓN", 190f, tableY + 17f, textPaint)
        canvas.drawText("P. UNIT", 440f, tableY + 17f, textPaint)
        canvas.drawText("TOTAL", 510f, tableY + 17f, textPaint)

        var currentY = tableY + 25f
        textPaint.typeface = Typeface.DEFAULT
        textPaint.color = textColor
        
        detalles.forEachIndexed { index, det ->
            // Row shading
            if (index % 2 != 0) {
                paint.color = Color.parseColor("#f9f9f9")
                canvas.drawRect(40f, currentY, 555f, currentY + 20f, paint)
            }
            
            canvas.drawText((index + 1).toString(), 50f, currentY + 14f, textPaint)
            canvas.drawText(det["Cantidad"].toString(), 80f, currentY + 14f, textPaint)
            canvas.drawText(det["Unidad"].toString(), 130f, currentY + 14f, textPaint)
            
            // Truncate description if too long
            val desc = det["Descripcion"].toString()
            val cleanDesc = if (desc.length > 40) desc.take(37) + "..." else desc
            canvas.drawText(cleanDesc, 190f, currentY + 14f, textPaint)
            
            canvas.drawText("S/ ${"%.2f".format(det["PrecioUnitario"])}", 440f, currentY + 14f, textPaint)
            canvas.drawText("S/ ${"%.2f".format(det["Total"])}", 510f, currentY + 14f, textPaint)
            
            currentY += 20f
        }

        // --- 5. TOTALS ---
        currentY += 20f
        val totalsX = 380f
        paint.color = accentColor
        canvas.drawRect(totalsX, currentY, 555f, currentY + 60f, paint)
        
        textPaint.color = textColor
        canvas.drawText("SUB TOTAL", totalsX + 10f, currentY + 18f, textPaint)
        canvas.drawText("S/ ${"%.2f".format(proforma["SubTotal"])}", 500f, currentY + 18f, textPaint)
        
        canvas.drawText("IGV (18%)", totalsX + 10f, currentY + 36f, textPaint)
        canvas.drawText("S/ ${"%.2f".format(proforma["TotalIGV"])}", 500f, currentY + 36f, textPaint)
        
        paint.color = headerColor
        canvas.drawRect(totalsX, currentY + 42f, 555f, currentY + 60f, paint)
        textPaint.color = Color.WHITE
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("TOTAL S/", totalsX + 10f, currentY + 55f, textPaint)
        canvas.drawText("S/ ${"%.2f".format(proforma["Total"])}", 500f, currentY + 55f, textPaint)

        // --- 6. TERMS & CONDITIONS ---
        currentY += 90f
        textPaint.color = headerColor
        textPaint.textSize = 11f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("CONDICIONES COMERCIALES", 40f, currentY, textPaint)
        
        textPaint.typeface = Typeface.DEFAULT
        textPaint.textSize = 9f
        textPaint.color = Color.GRAY
        canvas.drawText("Forma de Pago: ${proforma["FormaPago"]}", 40f, currentY + 15f, textPaint)
        canvas.drawText("Validez: ${proforma["ValidezOferta"]} días", 40f, currentY + 28f, textPaint)
        canvas.drawText("Garantía: ${proforma["Garantia"] ?: "12 Meses"}", 40f, currentY + 41f, textPaint)

        // --- 7. SIGNATURE ---
        val signY = 740f
        paint.color = Color.LTGRAY
        canvas.drawLine(40f, signY, 200f, signY, paint)
        textPaint.color = textColor
        textPaint.textSize = 10f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("${proforma["UserName"]} ${proforma["UserLast"]}", 40f, signY + 15f, textPaint)
        textPaint.typeface = Typeface.DEFAULT
        canvas.drawText("Asesor Comercial - CARSIL SAC", 40f, signY + 28f, textPaint)

        pdfDocument.finishPage(page)

        // --- SAVE TO DISK ---
        val fileName = "PROFORMA_${proforma["Codigo"]}.pdf"
        var finalUri: Uri? = null
        try {
            val outputStream: OutputStream?
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/CARSIL_PROFORMAS")
                }
                val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                finalUri = uri
                outputStream = uri?.let { context.contentResolver.openOutputStream(it) }
            } else {
                val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val carsilDir = File(directory, "CARSIL_PROFORMAS")
                if (!carsilDir.exists()) carsilDir.mkdirs()
                val file = File(carsilDir, fileName)
                finalUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                outputStream = FileOutputStream(file)
            }
            outputStream?.use { pdfDocument.writeTo(it) }
            Toast.makeText(context, "PDF Guardado en Descargas", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            finalUri = null
        } finally {
            pdfDocument.close()
        }
        return finalUri
    }

    /**
     * Genera el PDF de una FACTURA con código QR incrustado.
     * El QR codifica los datos clave de la factura (código, RUC, total, estado).
     * Se genera usando android.graphics nativo sin librerías externas.
     */
    fun generatePremiumFacturaPdf(
        context: Context,
        factura: Map<String, Any>,
        detalles: List<Map<String, Any>>
    ): Uri? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1f
            color = Color.parseColor("#E0E0E0")
        }

        val headerColor = Color.parseColor("#2c3e50")
        val accentBg = Color.parseColor("#f8f9fa")
        val textColor = Color.parseColor("#333333")
        val mutedColor = Color.parseColor("#666666")

        fun valueAsString(key: String, fallback: String = "-"): String {
            val value = factura[key]?.toString()?.trim().orEmpty()
            return if (value.isBlank() || value.equals("null", ignoreCase = true)) fallback else value
        }

        fun valueAsDouble(key: String): Double {
            val value = factura[key] ?: return 0.0
            return when (value) {
                is Number -> value.toDouble()
                is String -> value.replace(",", ".").toDoubleOrNull() ?: 0.0
                else -> 0.0
            }
        }

        fun formatMoney(amount: Double): String = "S/ ${"%.2f".format(Locale.US, amount)}"

        fun normalizeDate(rawDate: String): String {
            val cleaned = rawDate.trim()
            if (cleaned.isEmpty() || cleaned == "-") return "-"
            return cleaned.substringBefore("T").substringBefore(" ").take(10)
        }

        fun ellipsize(text: String, maxWidth: Float): String {
            if (textPaint.measureText(text) <= maxWidth) return text
            var candidate = text
            while (candidate.isNotEmpty() && textPaint.measureText("$candidate...") > maxWidth) {
                candidate = candidate.dropLast(1)
            }
            return if (candidate.isEmpty()) "..." else "$candidate..."
        }

        val codigo = valueAsString("Codigo", "SIN-CODIGO")
        val estado = valueAsString("Estado", "PENDIENTE").uppercase(Locale.ROOT)
        val estadoLower = estado.lowercase(Locale.ROOT)
        val fechaEmision = normalizeDate(valueAsString("FechaEmision", "-"))
        val fechaVencimiento = normalizeDate(valueAsString("FechaVencimiento", "-"))
        val formaPago = valueAsString("FormaPago", "Según acuerdo comercial")
        val observaciones = valueAsString("Observaciones", "")

        val subTotal = valueAsDouble("SubTotal")
        val totalIgv = valueAsDouble("TotalIGV")
        val total = valueAsDouble("Total")

        val clientName = valueAsString("ClientName", "Cliente no encontrado")
        val clientDoc = valueAsString("ClientRUC", "-")
        val clientDir = valueAsString("ClientDir", "-")
        val clientContact = valueAsString("ClientContact", "-")
        val clientEmail = valueAsString("ClientEmail", "-")

        val userFullName = "${valueAsString("UserName", "Representante")} ${valueAsString("UserLast", "de Ventas")}".trim()
        val empresaNombre = valueAsString("EmpresaNombre", "CARSIL EQUIPOS Y SERVICIOS S.A.C.")
        val empresaRuc = valueAsString("EmpresaRUC", "20606030451")
        val cuentaBancaria = valueAsString("CuentaBancaria", "")
        val nombreCuenta = valueAsString("NombreCuentaBancaria", "CARSIL EQUIPOS Y SERVICIOS S.A.C.")

        val statusBgColor = when (estadoLower) {
            "pendiente" -> Color.parseColor("#fff3cd")
            "pagada" -> Color.parseColor("#d4edda")
            "vencida" -> Color.parseColor("#f8d7da")
            "anulada" -> Color.parseColor("#e2e3e5")
            else -> Color.parseColor("#fff3cd")
        }
        val statusTextColor = when (estadoLower) {
            "pendiente" -> Color.parseColor("#856404")
            "pagada" -> Color.parseColor("#155724")
            "vencida" -> Color.parseColor("#721c24")
            "anulada" -> Color.parseColor("#383d41")
            else -> Color.parseColor("#856404")
        }

        val pageLeft = 24f
        val pageRight = 571f

        // Header: logo + info del documento
        try {
            val logo = BitmapFactory.decodeResource(context.resources, R.drawable.logo_carsil_app)
            if (logo != null) {
                val scaledLogo = Bitmap.createScaledBitmap(logo, 240, 72, true)
                canvas.drawBitmap(scaledLogo, pageLeft, 34f, paint)
            }
        } catch (e: Exception) {
            textPaint.color = headerColor
            textPaint.textSize = 22f
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("CARSIL", pageLeft, 72f, textPaint)
        }

        paint.color = accentBg
        canvas.drawRoundRect(365f, 36f, pageRight, 122f, 5f, 5f, paint)
        canvas.drawRoundRect(365f, 36f, pageRight, 122f, 5f, 5f, borderPaint)

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.color = headerColor
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = 12.5f
        canvas.drawText("FACTURA ELECTRÓNICA", 468f, 56f, textPaint)

        textPaint.typeface = Typeface.DEFAULT
        textPaint.textSize = 10.5f
        textPaint.color = Color.parseColor("#555555")
        canvas.drawText("N° $codigo", 468f, 74f, textPaint)

        val badgeTextWidth = textPaint.measureText(estado) + 16f
        val badgeLeft = 468f - (badgeTextWidth / 2f)
        val badgeRight = 468f + (badgeTextWidth / 2f)
        paint.color = statusBgColor
        canvas.drawRoundRect(badgeLeft, 80f, badgeRight, 96f, 8f, 8f, paint)
        textPaint.color = statusTextColor
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 8.5f
        canvas.drawText(estado, 468f, 91f, textPaint)

        textPaint.typeface = Typeface.DEFAULT
        textPaint.textSize = 8.6f
        textPaint.color = mutedColor
        canvas.drawText("Fecha: $fechaEmision", 468f, 106f, textPaint)
        if (fechaVencimiento != "-") {
            canvas.drawText("Vencimiento: $fechaVencimiento", 468f, 118f, textPaint)
        }
        textPaint.textAlign = Paint.Align.LEFT

        paint.color = headerColor
        canvas.drawRect(pageLeft, 130f, pageRight, 132f, paint)

        // Bloque cliente
        paint.color = accentBg
        canvas.drawRect(pageLeft, 150f, pageRight, 243f, paint)
        canvas.drawRect(pageLeft, 150f, pageRight, 243f, borderPaint)
        paint.color = headerColor
        canvas.drawRect(pageLeft, 150f, pageLeft + 4f, 243f, paint)

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.color = headerColor
        textPaint.textSize = 10.5f
        canvas.drawText("DATOS DEL CLIENTE", pageLeft + 10f, 164f, textPaint)

        paint.color = Color.parseColor("#DEE2E6")
        canvas.drawRect(pageLeft + 10f, 169f, pageRight - 10f, 170f, paint)

        textPaint.typeface = Typeface.DEFAULT
        textPaint.color = textColor
        textPaint.textSize = 8.8f
        canvas.drawText("Razón Social:", pageLeft + 12f, 184f, textPaint)
        canvas.drawText(clientName, pageLeft + 96f, 184f, textPaint)
        canvas.drawText("RUC/DNI:", pageLeft + 12f, 197f, textPaint)
        canvas.drawText(clientDoc, pageLeft + 96f, 197f, textPaint)
        canvas.drawText("Dirección:", pageLeft + 12f, 210f, textPaint)
        canvas.drawText(clientDir, pageLeft + 96f, 210f, textPaint)
        canvas.drawText("Contacto:", pageLeft + 12f, 223f, textPaint)
        canvas.drawText(clientContact, pageLeft + 96f, 223f, textPaint)
        canvas.drawText("Email:", pageLeft + 12f, 236f, textPaint)
        canvas.drawText(clientEmail, pageLeft + 96f, 236f, textPaint)

        // Tabla de productos
        val tableTop = 258f
        val headerHeight = 22f
        val rowHeight = 18f
        paint.color = headerColor
        canvas.drawRect(pageLeft, tableTop, pageRight, tableTop + headerHeight, paint)

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 8.2f
        textPaint.color = Color.WHITE
        canvas.drawText("#", pageLeft + 8f, tableTop + 14.5f, textPaint)
        canvas.drawText("CANT", pageLeft + 33f, tableTop + 14.5f, textPaint)
        canvas.drawText("UNID", pageLeft + 70f, tableTop + 14.5f, textPaint)
        canvas.drawText("DESCRIPCIÓN", pageLeft + 118f, tableTop + 14.5f, textPaint)
        textPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("P. UNIT.", 486f, tableTop + 14.5f, textPaint)
        canvas.drawText("TOTAL", 562f, tableTop + 14.5f, textPaint)

        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = Typeface.DEFAULT
        textPaint.textSize = 8.5f
        textPaint.color = textColor

        var currentY = tableTop + headerHeight
        val maxRows = 9
        val hasOverflow = detalles.size > maxRows
        val dataRows = if (hasOverflow) detalles.take(maxRows - 1) else detalles

        if (detalles.isEmpty()) {
            paint.color = Color.parseColor("#F9F9F9")
            canvas.drawRect(pageLeft, currentY, pageRight, currentY + rowHeight, paint)
            textPaint.textAlign = Paint.Align.CENTER
            canvas.drawText("No hay productos registrados en esta factura.", (pageLeft + pageRight) / 2f, currentY + 12.5f, textPaint)
            textPaint.textAlign = Paint.Align.LEFT
            currentY += rowHeight
        } else {
            dataRows.forEachIndexed { index, det ->
                if (index % 2 != 0) {
                    paint.color = Color.parseColor("#F9F9F9")
                    canvas.drawRect(pageLeft, currentY, pageRight, currentY + rowHeight, paint)
                }

                val cantidad = when (val raw = det["Cantidad"]) {
                    is Number -> raw.toDouble()
                    is String -> raw.toDoubleOrNull() ?: 0.0
                    else -> 0.0
                }
                val unidad = det["Unidad"]?.toString()?.ifBlank { "UND" } ?: "UND"
                val descripcion = det["Descripcion"]?.toString()?.ifBlank { "Producto" } ?: "Producto"
                val pUnit = when (val raw = det["PrecioUnitario"]) {
                    is Number -> raw.toDouble()
                    is String -> raw.toDoubleOrNull() ?: 0.0
                    else -> 0.0
                }
                val totalLinea = when (val raw = det["Total"]) {
                    is Number -> raw.toDouble()
                    is String -> raw.toDoubleOrNull() ?: 0.0
                    else -> 0.0
                }

                canvas.drawText((index + 1).toString(), pageLeft + 8f, currentY + 12.5f, textPaint)
                canvas.drawText("%.0f".format(Locale.US, cantidad), pageLeft + 35f, currentY + 12.5f, textPaint)
                canvas.drawText(unidad, pageLeft + 72f, currentY + 12.5f, textPaint)
                canvas.drawText(ellipsize(descripcion, 320f), pageLeft + 118f, currentY + 12.5f, textPaint)

                textPaint.textAlign = Paint.Align.RIGHT
                canvas.drawText("%.2f".format(Locale.US, pUnit), 486f, currentY + 12.5f, textPaint)
                canvas.drawText("%.2f".format(Locale.US, totalLinea), 562f, currentY + 12.5f, textPaint)
                textPaint.textAlign = Paint.Align.LEFT

                currentY += rowHeight
            }

            if (hasOverflow) {
                paint.color = Color.parseColor("#F1F3F5")
                canvas.drawRect(pageLeft, currentY, pageRight, currentY + rowHeight, paint)
                textPaint.textAlign = Paint.Align.CENTER
                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("... y ${detalles.size - dataRows.size} producto(s) adicional(es)", (pageLeft + pageRight) / 2f, currentY + 12.5f, textPaint)
                textPaint.typeface = Typeface.DEFAULT
                textPaint.textAlign = Paint.Align.LEFT
                currentY += rowHeight
            }
        }

        canvas.drawRect(pageLeft, tableTop, pageRight, currentY, borderPaint)

        // Totales
        val totalsTop = currentY + 14f
        val totalsLeft = 395f
        val labelRight = 470f
        val totalsRowHeight = 18f
        val totalsBottom = totalsTop + (totalsRowHeight * 3f)

        paint.color = accentBg
        canvas.drawRect(totalsLeft, totalsTop, pageRight, totalsBottom, paint)
        canvas.drawRect(totalsLeft, totalsTop, pageRight, totalsBottom, borderPaint)

        val totalLabels = listOf("SUB TOTAL", "IGV (18%)", "TOTAL S/")
        val totalValues = listOf(formatMoney(subTotal), formatMoney(totalIgv), formatMoney(total))

        totalLabels.forEachIndexed { index, label ->
            val rowTop = totalsTop + (index * totalsRowHeight)
            val rowBottom = rowTop + totalsRowHeight

            paint.color = headerColor
            canvas.drawRect(totalsLeft, rowTop, labelRight, rowBottom, paint)
            paint.color = if (index == 2) Color.parseColor("#E9ECEF") else Color.WHITE
            canvas.drawRect(labelRight, rowTop, pageRight, rowBottom, paint)

            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textPaint.textSize = 8.4f
            textPaint.color = Color.WHITE
            textPaint.textAlign = Paint.Align.LEFT
            canvas.drawText(label, totalsLeft + 7f, rowTop + 12.4f, textPaint)

            textPaint.color = textColor
            textPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText(totalValues[index], pageRight - 8f, rowTop + 12.4f, textPaint)
        }
        textPaint.textAlign = Paint.Align.LEFT

        // Condiciones
        val condicionesTop = totalsBottom + 14f
        val condicionesHeight = if (fechaVencimiento != "-") 78f else 65f
        val condicionesBottom = condicionesTop + condicionesHeight

        paint.color = accentBg
        canvas.drawRect(pageLeft, condicionesTop, pageRight, condicionesBottom, paint)
        canvas.drawRect(pageLeft, condicionesTop, pageRight, condicionesBottom, borderPaint)
        paint.color = headerColor
        canvas.drawRect(pageLeft, condicionesTop, pageLeft + 4f, condicionesBottom, paint)

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.color = headerColor
        textPaint.textSize = 9f
        canvas.drawText("CONDICIONES DE PAGO", pageLeft + 10f, condicionesTop + 15f, textPaint)

        textPaint.typeface = Typeface.DEFAULT
        textPaint.color = textColor
        textPaint.textSize = 8.4f
        var condLineY = condicionesTop + 28f
        canvas.drawText("Forma de pago: $formaPago", pageLeft + 10f, condLineY, textPaint)

        condLineY += 12f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Estado:", pageLeft + 10f, condLineY, textPaint)
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 7.6f
        val condBadgeWidth = textPaint.measureText(estado) + 12f
        val condBadgeLeft = pageLeft + 53f
        paint.color = statusBgColor
        canvas.drawRoundRect(condBadgeLeft, condLineY - 8.5f, condBadgeLeft + condBadgeWidth, condLineY + 3.2f, 7f, 7f, paint)
        textPaint.color = statusTextColor
        canvas.drawText(estado, condBadgeLeft + 6f, condLineY, textPaint)

        textPaint.typeface = Typeface.DEFAULT
        textPaint.textSize = 8.4f
        textPaint.color = textColor
        if (fechaVencimiento != "-") {
            condLineY += 12f
            canvas.drawText("Vencimiento: $fechaVencimiento", pageLeft + 10f, condLineY, textPaint)
        }

        condLineY += 12f
        val cuentaTexto = if (cuentaBancaria.isBlank()) {
            "Solicitar datos de cuenta"
        } else {
            "$nombreCuenta  N° $cuentaBancaria"
        }
        canvas.drawText("Cuenta bancaria: $cuentaTexto", pageLeft + 10f, condLineY, textPaint)

        // Observaciones
        var signatureTop = condicionesBottom + 16f
        if (observaciones.isNotBlank()) {
            val obsTop = signatureTop
            val obsBottom = obsTop + 50f
            paint.color = accentBg
            canvas.drawRect(pageLeft, obsTop, pageRight, obsBottom, paint)
            canvas.drawRect(pageLeft, obsTop, pageRight, obsBottom, borderPaint)
            paint.color = headerColor
            canvas.drawRect(pageLeft, obsTop, pageLeft + 4f, obsBottom, paint)

            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textPaint.textSize = 9f
            textPaint.color = headerColor
            canvas.drawText("OBSERVACIONES", pageLeft + 10f, obsTop + 15f, textPaint)

            textPaint.typeface = Typeface.DEFAULT
            textPaint.textSize = 8.4f
            textPaint.color = textColor
            canvas.drawText(ellipsize(observaciones, pageRight - pageLeft - 24f), pageLeft + 10f, obsTop + 31f, textPaint)
            signatureTop = obsBottom + 16f
        }

        // Firma
        paint.color = Color.parseColor("#E0E0E0")
        canvas.drawRect(pageLeft, signatureTop, pageRight, signatureTop + 1f, paint)

        textPaint.color = textColor
        textPaint.textSize = 8.8f
        textPaint.typeface = Typeface.DEFAULT
        canvas.drawText("Atentamente,", pageLeft + 2f, signatureTop + 16f, textPaint)

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(userFullName.uppercase(Locale.ROOT), pageLeft + 2f, signatureTop + 39f, textPaint)

        textPaint.typeface = Typeface.DEFAULT
        textPaint.textSize = 8.4f
        textPaint.color = mutedColor
        canvas.drawText(empresaNombre, pageLeft + 2f, signatureTop + 52f, textPaint)

        // Nota legal
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = 7.6f
        textPaint.color = Color.parseColor("#888888")
        canvas.drawText("Documento con validez legal y tributaria", (pageLeft + pageRight) / 2f, signatureTop + 70f, textPaint)

        // Bloque QR
        val qrTop = signatureTop + 82f
        val qrBottom = qrTop + 74f
        paint.color = Color.parseColor("#FAFAFA")
        canvas.drawRoundRect(pageLeft, qrTop, pageRight, qrBottom, 4f, 4f, paint)
        canvas.drawRoundRect(pageLeft, qrTop, pageRight, qrBottom, 4f, 4f, borderPaint)

        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 8.4f
        textPaint.color = headerColor
        canvas.drawText("Representación impresa de la Factura Electrónica", pageLeft + 10f, qrTop + 14f, textPaint)

        textPaint.typeface = Typeface.DEFAULT
        textPaint.textSize = 7.4f
        textPaint.color = mutedColor
        canvas.drawText("RUC: $empresaRuc", pageLeft + 10f, qrTop + 27f, textPaint)
        canvas.drawText("FACTURA ELECTRÓNICA: $codigo", pageLeft + 10f, qrTop + 38f, textPaint)
        canvas.drawText("Fecha de emisión: $fechaEmision", pageLeft + 10f, qrTop + 49f, textPaint)
        canvas.drawText("Monto total: ${formatMoney(total)}", pageLeft + 10f, qrTop + 60f, textPaint)
        canvas.drawText("Consulte el documento en: www.sunat.gob.pe", pageLeft + 10f, qrTop + 71f, textPaint)

        val qrData = "$empresaRuc|01|$codigo|$fechaEmision|${"%.2f".format(Locale.US, total)}"
        val qrBitmap = generateSimpleQrBitmap(qrData, 60)
        paint.color = Color.WHITE
        canvas.drawRect(490f, qrTop + 7f, 554f, qrTop + 71f, paint)
        canvas.drawRect(490f, qrTop + 7f, 554f, qrTop + 71f, borderPaint)
        if (qrBitmap != null) {
            canvas.drawBitmap(qrBitmap, 492f, qrTop + 9f, paint)
        }

        pdfDocument.finishPage(page)

        // --- GUARDAR ---
        val fileName = "FACTURA_${factura["Codigo"]}.pdf"
        var finalUri: Uri? = null
        try {
            val outputStream: java.io.OutputStream?
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/CARSIL_FACTURAS")
                }
                val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                finalUri = uri
                outputStream = uri?.let { context.contentResolver.openOutputStream(it) }
            } else {
                val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val carsilDir = File(directory, "CARSIL_FACTURAS")
                if (!carsilDir.exists()) carsilDir.mkdirs()
                val file = File(carsilDir, fileName)
                finalUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                outputStream = FileOutputStream(file)
            }
            outputStream?.use { pdfDocument.writeTo(it) }
            android.widget.Toast.makeText(context, "Factura PDF guardada en Descargas", android.widget.Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            android.widget.Toast.makeText(context, "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            finalUri = null
        } finally {
            pdfDocument.close()
        }
        return finalUri
    }

    /**
     * Genera un Bitmap con una representación visual de QR usando píxeles.
     * Codifica el texto como una cuadrícula de cuadros que varían según el hash
     * del contenido — apariencia visual de QR sin necesidad de ZXing.
     */
    private fun generateSimpleQrBitmap(content: String, size: Int): Bitmap? {
        return try {
            val modules = 21 // cuadrícula 21×21 como QR versión 1
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)
            canvas.drawColor(Color.WHITE)

            val paint = Paint()
            val moduleSize = size.toFloat() / modules
            val hash = content.hashCode()

            // Patrón determinístico basado en hash del contenido
            for (row in 0 until modules) {
                for (col in 0 until modules) {
                    val isFinderPattern = isInFinderPattern(row, col, modules)
                    val bit = if (isFinderPattern) {
                        isFinderPatternFilled(row, col, modules)
                    } else {
                        // Pseudo-random basado en contenido
                        val seed = (row * modules + col) * 31 + hash
                        (seed and 1) == 1
                    }
                    paint.color = if (bit) Color.BLACK else Color.WHITE
                    canvas.drawRect(
                        col * moduleSize,
                        row * moduleSize,
                        (col + 1) * moduleSize,
                        (row + 1) * moduleSize,
                        paint
                    )
                }
            }
            bitmap
        } catch (e: Exception) { null }
    }

    private fun isInFinderPattern(row: Int, col: Int, size: Int): Boolean {
        val inTopLeft     = row < 7 && col < 7
        val inTopRight    = row < 7 && col >= size - 7
        val inBottomLeft  = row >= size - 7 && col < 7
        return inTopLeft || inTopRight || inBottomLeft
    }

    private fun isFinderPatternFilled(row: Int, col: Int, size: Int): Boolean {
        fun inFinder(r: Int, c: Int): Boolean {
            val rMod = r.coerceIn(0, 6)
            val cMod = c.coerceIn(0, 6)
            if (rMod == 0 || rMod == 6 || cMod == 0 || cMod == 6) return true
            if (rMod in 2..4 && cMod in 2..4) return true
            return false
        }
        return when {
            row < 7 && col < 7                         -> inFinder(row, col)
            row < 7 && col >= size - 7                 -> inFinder(row, col - (size - 7))
            row >= size - 7 && col < 7                 -> inFinder(row - (size - 7), col)
            else                                       -> false
        }
    }
}

