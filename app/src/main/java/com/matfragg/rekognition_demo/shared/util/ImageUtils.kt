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

    /**
     * Recorta la imagen basándose en las dimensiones reales de la UI
     * @param viewWidth Ancho del PreviewView en la pantalla
     * @param viewHeight Alto del PreviewView en la pantalla
     */
    fun cropImageToBoundingBox(
        photoFile: File,
        viewWidth: Int,
        viewHeight: Int,
        isLandscape: Boolean
    ) {
        val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath) ?: return

        // 1. Corregir rotación EXIF para trabajar con el bitmap "derecho"
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
        // Determinamos cómo se estiró la imagen del sensor para llenar la pantalla
        val scale = max(
            rotatedBitmap.width.toFloat() / viewWidth,
            rotatedBitmap.height.toFloat() / viewHeight
        )

        // 3. Replicar la lógica del DocumentGuideOverlay para obtener coordenadas UI
        val uiRectWidth = if (isLandscape) {
            viewHeight * 0.75f * DNI_ASPECT_RATIO
        } else {
            viewWidth * 0.85f
        }
        val uiRectHeight = uiRectWidth / DNI_ASPECT_RATIO

        val uiLeft = (viewWidth - uiRectWidth) / 2
        val uiTop = (viewHeight - uiRectHeight) / 2

        // 4. Mapear coordenadas de UI a Píxeles del Bitmap
        // Compensamos el desplazamiento si la cámara capturó más de lo que la pantalla muestra
        val bitmapVisibleWidth = viewWidth * scale
        val bitmapVisibleHeight = viewHeight * scale
        val offsetX = (rotatedBitmap.width - bitmapVisibleWidth) / 2
        val offsetY = (rotatedBitmap.height - bitmapVisibleHeight) / 2

        val finalLeft = (offsetX + (uiLeft * scale)).toInt().coerceAtLeast(0)
        val finalTop = (offsetY + (uiTop * scale)).toInt().coerceAtLeast(0)
        val finalWidth = (uiRectWidth * scale).toInt().coerceAtMost(rotatedBitmap.width - finalLeft)
        val finalHeight = (uiRectHeight * scale).toInt().coerceAtMost(rotatedBitmap.height - finalTop)

        Log.d("IMAGE_UTILS", "Crop: view[${viewWidth}x${viewHeight}] bitmap[${rotatedBitmap.width}x${rotatedBitmap.height}] -> final[${finalWidth}x${finalHeight}]")

        // 5. Ejecutar recorte
        val croppedBitmap = Bitmap.createBitmap(rotatedBitmap, finalLeft, finalTop, finalWidth, finalHeight)

        // 6. Guardar
        FileOutputStream(photoFile).use { out ->
            croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        }

        // Liberar memoria
        if (bitmap != rotatedBitmap) bitmap.recycle()
        rotatedBitmap.recycle()
        croppedBitmap.recycle()
    }
}