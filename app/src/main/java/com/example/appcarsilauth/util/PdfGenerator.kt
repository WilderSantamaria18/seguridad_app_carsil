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
}
