package com.matfragg.rekognition_demo.shared.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min
import kotlin.math.roundToInt

object ImageUtils {

    private const val DNI_ASPECT_RATIO = 1.586f // Ancho / Alto estándar
    // Factor para "apretar" el recorte. 0.95 significa que usaremos el 95% del área calculada,
    // eliminando un 5% de margen para asegurar que no salga el fondo.
    // Si aún sale fondo, redúcelo a 0.92f o 0.90f.
    private const val TIGHT_CROP_FACTOR = 0.7f


    /**
     * @param viewWidth Ancho del PreviewView en la pantalla
     * @param viewHeight Alto del PreviewView en la pantalla
     */
    fun cropImageToBoundingBox(photoFile: File) {
        val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)

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
        val bmWidth = rotatedBitmap.width.toFloat()
        val bmHeight = rotatedBitmap.height.toFloat()
        val bmRatio = bmWidth / bmHeight

        // 2. Calcular dimensiones del recorte objetivo basado en el DNI
        var cropWidth: Float
        var cropHeight: Float

        // Determinamos qué lado limita el tamaño del DNI dentro de la foto
        if (bmRatio > DNI_ASPECT_RATIO) {
            // La foto es más "ancha" que un DNI. La altura es el límite.
            cropHeight = bmHeight * TIGHT_CROP_FACTOR
            cropWidth = cropHeight * DNI_ASPECT_RATIO
        } else {
            // La foto es más "alta" o cuadrada que un DNI. El ancho es el límite.
            cropWidth = bmWidth * TIGHT_CROP_FACTOR
            cropHeight = cropWidth / DNI_ASPECT_RATIO
        }

        // Asegurarnos que no nos pasamos de las dimensiones de la imagen por errores de redondeo
        cropWidth = min(cropWidth, bmWidth)
        cropHeight = min(cropHeight, bmHeight)

        // 3. Calcular coordenadas para centrar el recorte
        val left = ((bmWidth - cropWidth) / 2).roundToInt().coerceAtLeast(0)
        val top = ((bmHeight - cropHeight) / 2).roundToInt().coerceAtLeast(0)
        val finalWidthInt = cropWidth.roundToInt().coerceAtMost(rotatedBitmap.width - left)
        val finalHeightInt = cropHeight.roundToInt().coerceAtMost(rotatedBitmap.height - top)

        Log.d("IMAGE_UTILS", "Recortando: Origen[${rotatedBitmap.width}x${rotatedBitmap.height}] -> Crop[${finalWidthInt}x${finalHeightInt}] en ($left, $top)")

        // 4. Ejecutar recorte
        val croppedBitmap = Bitmap.createBitmap(
            rotatedBitmap,
            left,
            top,
            finalWidthInt,
            finalHeightInt
        )

        // 5. Guardar sobrescribiendo el archivo original con alta calidad
        FileOutputStream(photoFile).use { out ->
            // Usamos PNG para evitar artefactos de compresión en el texto del DNI,
            // o JPEG con calidad 100 si el tamaño no es problema.
            croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        }

        // Liberar memoria
        if (bitmap != rotatedBitmap) bitmap.recycle()
        rotatedBitmap.recycle()
        croppedBitmap.recycle()
    }
}