package com.campusconnect.core.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

object PdfPageRenderer {
    suspend fun renderFirstPage(context: Context, pdfUrl: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val tempFile = File(context.cacheDir, "temp_pdf_preview.pdf")
            if (tempFile.exists()) tempFile.delete()

            // Download file from URL
            val url = URL(pdfUrl)
            url.openStream().use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }

            // Render page 0 using native PdfRenderer
            val fileDescriptor = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(fileDescriptor)
            if (renderer.pageCount > 0) {
                val page = renderer.openPage(0)
                // Proportional bitmap sizing for crop display
                val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()
                renderer.close()
                fileDescriptor.close()
                bitmap
            } else {
                renderer.close()
                fileDescriptor.close()
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
