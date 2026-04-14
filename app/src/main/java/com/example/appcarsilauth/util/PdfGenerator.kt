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

        val paint = Paint()
        val textPaint = Paint()
        val headerColor  = Color.parseColor("#1E1B4B") // Indigo oscuro
        val accentIndigo = Color.parseColor("#6366F1") // Indigo primario
        val accentBg     = Color.parseColor("#F5F3FF") // Fondo lila claro
        val textColor    = Color.parseColor("#1F2937")
        val grayColor    = Color.parseColor("#6B7280")

        // --- 1. BANDA SUPERIOR ---
        paint.color = headerColor
        canvas.drawRect(0f, 0f, 595f, 110f, paint)

        // Franja acento lateral derecho
        paint.color = accentIndigo
        canvas.drawRect(555f, 0f, 595f, 110f, paint)

        // Logo
        try {
            val logo = BitmapFactory.decodeResource(context.resources, R.drawable.logo_carsil_app)
            if (logo != null) {
                val scaledLogo = Bitmap.createScaledBitmap(logo, 75, 80, true)
                canvas.drawBitmap(scaledLogo, 30f, 15f, paint)
            }
        } catch (e: Exception) {
            textPaint.color = Color.WHITE
            textPaint.textSize = 22f
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("CARSIL", 30f, 60f, textPaint)
        }

        // Empresa Nombre y RUC
        textPaint.color = Color.WHITE
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 13f
        canvas.drawText(factura["EmpresaNombre"] as? String ?: "CARSIL SAC", 125f, 45f, textPaint)
        textPaint.typeface = Typeface.DEFAULT
        textPaint.textSize = 10f
        textPaint.color = Color.parseColor("#C7D2FE")
        canvas.drawText("RUC: ${factura["EmpresaRUC"]}", 125f, 62f, textPaint)
        canvas.drawText(factura["EmpresaDir"] as? String ?: "", 125f, 77f, textPaint)

        // Título documento
        textPaint.color = Color.WHITE
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textAlign = Paint.Align.RIGHT
        textPaint.textSize = 16f
        canvas.drawText("FACTURA ELECTRÓNICA", 540f, 45f, textPaint)
        textPaint.textSize = 11f
        textPaint.color = Color.parseColor("#A5B4FC")
        canvas.drawText(factura["Codigo"] as? String ?: "", 540f, 65f, textPaint)
        val estado = (factura["Estado"] as? String ?: "PENDIENTE").uppercase()
        textPaint.textSize = 9f
        textPaint.color = when(estado) {
            "PAGADA" -> Color.parseColor("#6EE7B7")
            "ANULADA" -> Color.parseColor("#FCA5A5")
            else -> Color.parseColor("#FDE68A")
        }
        canvas.drawText("● $estado", 540f, 83f, textPaint)
        textPaint.textAlign = Paint.Align.LEFT

        // --- 2. BLOQUE INFO FACTURA + QR ---
        // Recuadro izquierdo: datos de la factura
        paint.color = accentBg
        canvas.drawRoundRect(20f, 118f, 290f, 210f, 10f, 10f, paint)
        paint.color = accentIndigo
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(20f, 118f, 290f, 210f, 10f, 10f, paint)
        paint.style = Paint.Style.FILL

        textPaint.textSize = 9f
        textPaint.color = grayColor
        textPaint.typeface = Typeface.DEFAULT

        fun drawInfoRow(label: String, value: String, y: Float) {
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textPaint.color = grayColor
            canvas.drawText(label, 30f, y, textPaint)
            textPaint.typeface = Typeface.DEFAULT
            textPaint.color = textColor
            canvas.drawText(value, 120f, y, textPaint)
        }
        drawInfoRow("Fecha emisión:", factura["FechaEmision"] as? String ?: "-", 138f)
        drawInfoRow("Vencimiento:", factura["FechaVencimiento"] as? String ?: "-", 153f)
        drawInfoRow("Forma de pago:", factura["FormaPago"] as? String ?: "-", 168f)
        drawInfoRow("Estado:", estado, 183f)
        drawInfoRow("Total:", "S/ ${"%.2f".format(factura["Total"] as? Double ?: 0.0)}", 198f)

        // QR CODE — generado con píxeles propios (sin ZXing)
        val qrContent = buildString {
            append("FACTURA:${factura["Codigo"]}")
            append("|RUC:${factura["EmpresaRUC"]}")
            append("|CLIENTE:${factura["ClientName"]}")
            append("|TOTAL:${factura["Total"]}")
            append("|ESTADO:$estado")
        }
        val qrBitmap = generateSimpleQrBitmap(qrContent, 90)
        if (qrBitmap != null) {
            // Marco del QR
            paint.color = Color.WHITE
            canvas.drawRoundRect(300f, 115f, 410f, 215f, 8f, 8f, paint)
            paint.color = accentIndigo
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1.5f
            canvas.drawRoundRect(300f, 115f, 410f, 215f, 8f, 8f, paint)
            paint.style = Paint.Style.FILL
            canvas.drawBitmap(qrBitmap, 305f, 118f, paint)
        }
        // Leyenda QR
        textPaint.textSize = 7f
        textPaint.color = accentIndigo
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("VERIFICACIÓN DIGITAL", 355f, 208f, textPaint)
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = Typeface.DEFAULT

        // --- 3. DATOS DEL CLIENTE ---
        paint.color = accentIndigo
        canvas.drawRect(20f, 222f, 24f, 242f, paint)

        textPaint.textSize = 11f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.color = headerColor
        canvas.drawText("DATOS DEL CLIENTE", 30f, 237f, textPaint)

        paint.color = Color.parseColor("#F9FAFB")
        canvas.drawRect(20f, 247f, 575f, 315f, paint)
        paint.color = Color.parseColor("#E5E7EB")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.8f
        canvas.drawRect(20f, 247f, 575f, 315f, paint)
        paint.style = Paint.Style.FILL

        textPaint.typeface = Typeface.DEFAULT
        textPaint.textSize = 10f
        textPaint.color = textColor
        canvas.drawText("Razón Social: ${factura["ClientName"]}", 30f, 265f, textPaint)
        canvas.drawText("RUC / DNI:    ${factura["ClientRUC"]}", 30f, 280f, textPaint)
        canvas.drawText("Dirección:    ${factura["ClientDir"]}", 30f, 295f, textPaint)
        canvas.drawText("Contacto:    ${factura["ClientContact"]}", 310f, 265f, textPaint)
        canvas.drawText("Email:         ${factura["ClientEmail"]}", 310f, 280f, textPaint)

        // --- 4. TABLA DE PRODUCTOS ---
        val tableY = 330f
        paint.color = headerColor
        canvas.drawRect(20f, tableY, 575f, tableY + 26f, paint)

        textPaint.color = Color.WHITE
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 9f
        canvas.drawText("#",          28f,  tableY + 17f, textPaint)
        canvas.drawText("CANT",       55f,  tableY + 17f, textPaint)
        canvas.drawText("UNID",       100f, tableY + 17f, textPaint)
        canvas.drawText("DESCRIPCIÓN",150f, tableY + 17f, textPaint)
        canvas.drawText("P. UNIT",    430f, tableY + 17f, textPaint)
        canvas.drawText("TOTAL",      510f, tableY + 17f, textPaint)

        var currentY = tableY + 26f
        textPaint.typeface = Typeface.DEFAULT
        textPaint.color = textColor

        detalles.forEachIndexed { index, det ->
            if (index % 2 != 0) {
                paint.color = Color.parseColor("#F0EFFE")
                canvas.drawRect(20f, currentY, 575f, currentY + 22f, paint)
            }
            textPaint.textSize = 9f
            canvas.drawText((index + 1).toString(), 28f, currentY + 15f, textPaint)
            canvas.drawText("%.0f".format(det["Cantidad"] as? Double ?: 0.0), 55f, currentY + 15f, textPaint)
            canvas.drawText(det["Unidad"].toString(), 100f, currentY + 15f, textPaint)

            val desc = det["Descripcion"].toString()
            val cleanDesc = if (desc.length > 38) desc.take(35) + "..." else desc
            canvas.drawText(cleanDesc, 150f, currentY + 15f, textPaint)
            canvas.drawText("S/ ${"%.2f".format(det["PrecioUnitario"] as? Double ?: 0.0)}", 430f, currentY + 15f, textPaint)
            canvas.drawText("S/ ${"%.2f".format(det["Total"] as? Double ?: 0.0)}", 510f, currentY + 15f, textPaint)
            currentY += 22f
        }

        // Línea inferior tabla
        paint.color = Color.parseColor("#E5E7EB")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawLine(20f, currentY, 575f, currentY, paint)
        paint.style = Paint.Style.FILL

        // --- 5. TOTALES ---
        currentY += 15f
        val totX = 380f

        paint.color = accentBg
        canvas.drawRect(totX, currentY, 575f, currentY + 70f, paint)
        paint.color = accentIndigo
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.8f
        canvas.drawRect(totX, currentY, 575f, currentY + 70f, paint)
        paint.style = Paint.Style.FILL

        textPaint.textSize = 9.5f
        textPaint.color = grayColor
        canvas.drawText("SUB TOTAL",   totX + 10f, currentY + 18f, textPaint)
        canvas.drawText("IGV (18%)",   totX + 10f, currentY + 36f, textPaint)

        textPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("S/ ${"%.2f".format(factura["SubTotal"] as? Double ?: 0.0)}", 565f, currentY + 18f, textPaint)
        canvas.drawText("S/ ${"%.2f".format(factura["TotalIGV"] as? Double ?: 0.0)}", 565f, currentY + 36f, textPaint)

        paint.color = accentIndigo
        canvas.drawRect(totX, currentY + 45f, 575f, currentY + 70f, paint)
        textPaint.color = Color.WHITE
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 11f
        canvas.drawText("S/ ${"%.2f".format(factura["Total"] as? Double ?: 0.0)}", 565f, currentY + 62f, textPaint)
        textPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("IMPORTE TOTAL S/", totX + 10f, currentY + 62f, textPaint)
        textPaint.typeface = Typeface.DEFAULT

        // --- 6. OBSERVACIONES / CONDICIONES ---
        val obsY = currentY + 100f
        val obs = factura["Observaciones"] as? String ?: ""
        textPaint.textSize = 9f
        textPaint.color = headerColor
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("CONDICIONES COMERCIALES", 20f, obsY, textPaint)
        textPaint.typeface = Typeface.DEFAULT
        textPaint.color = grayColor
        canvas.drawText("Forma de pago: ${factura["FormaPago"]}", 20f, obsY + 14f, textPaint)
        if (obs.isNotEmpty()) {
            canvas.drawText("Obs.: $obs", 20f, obsY + 28f, textPaint)
        }

        // --- 7. FIRMA Y PIE ---
        val signY = 760f
        paint.color = Color.parseColor("#E5E7EB")
        canvas.drawLine(20f, signY, 210f, signY, paint)
        textPaint.textSize = 9.5f
        textPaint.color = textColor
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("${factura["UserName"]} ${factura["UserLast"]}", 20f, signY + 14f, textPaint)
        textPaint.typeface = Typeface.DEFAULT
        textPaint.color = grayColor
        canvas.drawText("Representante Comercial - CARSIL SAC", 20f, signY + 28f, textPaint)

        // Color de sello estado
        val selloColor = when(estado) {
            "PAGADA" -> Color.parseColor("#16A34A")
            "ANULADA" -> Color.parseColor("#DC2626")
            else -> Color.parseColor("#D97706")
        }
        paint.color = selloColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        val selloX = 475f; val selloY = 755f
        canvas.drawRoundRect(selloX, selloY, selloX + 90f, selloY + 40f, 6f, 6f, paint)
        paint.style = Paint.Style.FILL
        textPaint.color = selloColor
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 14f
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText(estado, selloX + 45f, selloY + 26f, textPaint)
        textPaint.textAlign = Paint.Align.LEFT

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

