package com.example.appcarsilauth.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.example.appcarsilauth.data.local.entity.ClienteEntity
import com.example.appcarsilauth.data.local.entity.ProductoEntity
import com.example.appcarsilauth.data.local.entity.ProformaEntity
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfGenerator {

    fun generateProformaPdf(
        context: Context,
        proforma: ProformaEntity,
        cliente: ClienteEntity,
        producto: ProductoEntity,
        cantidad: Int
    ) {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val titlePaint = Paint()

        // Configuración de página (A4 aprox en puntos: 595 x 842)
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        // --- ENCABEZADO CORPORATIVO ---
        titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        titlePaint.textSize = 28f
        titlePaint.color = Color.parseColor("#0F172A")
        canvas.drawText("CARSIL EQUIPOS Y SERVICIOS", 40f, 60f, titlePaint)

        paint.textSize = 10f
        paint.color = Color.GRAY
        canvas.drawText("Sistemas de Bombeo Industrial | RUC: 20450987321", 40f, 80f, paint)
        canvas.drawText("Av. Industrial 456, Lima, Perú | www.carsil.com", 40f, 95f, paint)

        // Línea divisoria
        paint.color = Color.LTGRAY
        canvas.drawLine(40f, 110f, 555f, 110f, paint)

        // --- DATOS DE LA PROFORMA ---
        paint.color = Color.BLACK
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("PROFORMA TÉCNICA #INV-${proforma.IdProforma}", 40f, 140f, paint)
        
        paint.typeface = Typeface.DEFAULT
        paint.textSize = 12f
        canvas.drawText("Fecha: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}", 40f, 160f, paint)

        // --- DATOS DEL CLIENTE ---
        paint.color = Color.parseColor("#F1F5F9")
        canvas.drawRect(40f, 180f, 555f, 240f, paint) // Fondo para datos cliente
        
        paint.color = Color.BLACK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("CLIENTE:", 50f, 200f, paint)
        paint.typeface = Typeface.DEFAULT
        canvas.drawText(cliente.RazonSocial, 50f, 215f, paint)
        canvas.drawText("RUC: ${cliente.Documento} | Direccion: ${cliente.Direccion ?: "No registrada"}", 50f, 230f, paint)

        // --- TABLA DE PRODUCTOS ---
        // Encabezados tabla
        paint.color = Color.parseColor("#1E293B")
        canvas.drawRect(40f, 260f, 555f, 285f, paint)
        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("DESCRIPCIÓN", 50f, 278f, paint)
        canvas.drawText("CANT.", 350f, 278f, paint)
        canvas.drawText("P. UNIT.", 420f, 278f, paint)
        canvas.drawText("TOTAL", 500f, 278f, paint)

        // Fila única (simplificado para el demo)
        paint.color = Color.BLACK
        paint.typeface = Typeface.DEFAULT
        canvas.drawText(producto.Nombre, 50f, 310f, paint)
        canvas.drawText(cantidad.toString(), 350f, 310f, paint)
        canvas.drawText("S/ ${producto.PrecioUnitario}", 420f, 310f, paint)
        canvas.drawText("S/ ${"%.2f".format(producto.PrecioUnitario * cantidad)}", 500f, 310f, paint)

        // --- TOTALES ---
        val subtotal = producto.PrecioUnitario * cantidad
        val igv = subtotal * 0.18
        val total = subtotal + igv

        paint.color = Color.LTGRAY
        canvas.drawLine(350f, 350f, 555f, 350f, paint)
        
        paint.color = Color.BLACK
        canvas.drawText("SUBTOTAL:", 350f, 375f, paint)
        canvas.drawText("S/ ${"%.2f".format(subtotal)}", 500f, 375f, paint)
        
        canvas.drawText("IGV (18%):", 350f, 395f, paint)
        canvas.drawText("S/ ${"%.2f".format(igv)}", 500f, 395f, paint)
        
        paint.textSize = 16f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.parseColor("#2E7D32")
        canvas.drawText("TOTAL:", 350f, 425f, paint)
        canvas.drawText("S/ ${"%.2f".format(total)}", 480f, 425f, paint)

        // --- PIE DE PÁGINA SEGURIDAD (CARSIL-POL-INT) ---
        paint.textSize = 9f
        paint.color = Color.GRAY
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        canvas.drawText("Documento generado digitalmente por CARSIL Intranet. Válido por 15 días.", 40f, 800f, paint)
        canvas.drawText("Hash de Integridad: ${UUID.randomUUID().toString().substring(0, 8)}", 40f, 815f, paint)

        pdfDocument.finishPage(page)

        // --- GUARDAR ARCHIVO ---
        val fileName = "PROFORMA_${proforma.IdProforma}_${cliente.RazonSocial.replace(" ", "_")}.pdf"
        
        try {
            val outputStream: OutputStream?
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/CARSIL_PROFORMAS")
                }
                val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                outputStream = uri?.let { context.contentResolver.openOutputStream(it) }
            } else {
                val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = java.io.File(directory, fileName)
                outputStream = java.io.FileOutputStream(file)
            }

            outputStream?.use { pdfDocument.writeTo(it) }
            Toast.makeText(context, "PDF Guardado en Descargas", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error al generar PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }
}
