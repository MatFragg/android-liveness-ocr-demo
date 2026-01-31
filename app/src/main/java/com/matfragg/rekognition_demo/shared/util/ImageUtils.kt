package com.matfragg.rekognition_demo.shared.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

object ImageUtils {

    private const val DNI_ASPECT_RATIO = 1.586f
    // Definimos un tamaño máximo para el lado más largo (p.ej. 800px es ideal para biometría)
    private const val MAX_HEIGHT = 800

    fun cropImageToBoundingBox(
        photoFile: File,
        viewWidth: Int,
        viewHeight: Int,
        isLandscape: Boolean
    ) {
        val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath) ?: return

        // 1. Corregir rotación EXIF
        val exif = ExifInterface(photoFile.absolutePath)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }

        val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

        // 2. Calcular Escala (CameraX FILL_CENTER)
        val scale = max(
            rotatedBitmap.width.toFloat() / viewWidth,
            rotatedBitmap.height.toFloat() / viewHeight
        )

        // 3. Coordenadas UI
        val uiRectWidth = if (isLandscape) viewHeight * 0.56f * DNI_ASPECT_RATIO else viewWidth * 0.59f
        val uiRectHeight = uiRectWidth / DNI_ASPECT_RATIO

        val uiLeft = (viewWidth - uiRectWidth) / 2
        val uiTop = (viewHeight - uiRectHeight) / 2

        // 4. Mapear coordenadas a Píxeles
        val bitmapVisibleWidth = viewWidth * scale
        val bitmapVisibleHeight = viewHeight * scale
        val offsetX = (rotatedBitmap.width - bitmapVisibleWidth) / 2
        val offsetY = (rotatedBitmap.height - bitmapVisibleHeight) / 2

        val finalLeft = (offsetX + (uiLeft * scale)).toInt().coerceAtLeast(0)
        val finalTop = (offsetY + (uiTop * scale)).toInt().coerceAtLeast(0)
        val finalWidth = (uiRectWidth * scale).toInt().coerceAtMost(rotatedBitmap.width - finalLeft)
        val finalHeight = (uiRectHeight * scale).toInt().coerceAtMost(rotatedBitmap.height - finalTop)

        // 5. Ejecutar recorte inicial
        val croppedBitmap = Bitmap.createBitmap(rotatedBitmap, finalLeft, finalTop, finalWidth, finalHeight)

        // --- NUEVO: REDIMENSIONAR PARA BAJAR PESO ---
        // Si el recorte es muy grande, lo bajamos a una escala manejable (MAX_HEIGHT)
        val finalResizedBitmap = if (croppedBitmap.height > MAX_HEIGHT) {
            val ratio = croppedBitmap.width.toFloat() / croppedBitmap.height.toFloat()
            val targetWidth = (MAX_HEIGHT * ratio).toInt()
            Bitmap.createScaledBitmap(croppedBitmap, targetWidth, MAX_HEIGHT, true)
        } else {
            croppedBitmap
        }

        // 6. Guardar con COMPRESIÓN al 80% (Baja mucho el peso sin pixelar la cara)
        FileOutputStream(photoFile).use { out ->
            finalResizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
        }

        Log.d("IMAGE_UTILS", "Peso reducido. Dimensiones finales: ${finalResizedBitmap.width}x${finalResizedBitmap.height}")

        // Liberar memoria
        if (bitmap != rotatedBitmap) bitmap.recycle()
        rotatedBitmap.recycle()
        if (croppedBitmap != finalResizedBitmap) croppedBitmap.recycle()
        finalResizedBitmap.recycle()
    }
}