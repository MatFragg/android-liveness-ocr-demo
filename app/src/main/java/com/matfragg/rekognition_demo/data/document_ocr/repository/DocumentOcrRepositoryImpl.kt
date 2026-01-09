package com.matfragg.rekognition_demo.data.document_ocr.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.matfragg.rekognition_demo.data.document_ocr.parser.DniParserService
import com.matfragg.rekognition_demo.data.document_ocr.remote.DniApi
import com.matfragg.rekognition_demo.domain.document_ocr.model.DocumentType
import com.matfragg.rekognition_demo.domain.document_ocr.model.DniData
import com.matfragg.rekognition_demo.domain.document_ocr.model.ExtractedText
import com.matfragg.rekognition_demo.domain.document_ocr.repository.DocumentOcrRepository
import com.matfragg.rekognition_demo.shared.domain.Result
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class DocumentOcrRepositoryImpl @Inject constructor(
    private val api: DniApi
) : DocumentOcrRepository {

    override suspend fun processDni(frontImage: File, backImage: File): Result<DniData> {
        return try {
            // 1. OPTIMIZACIÓN: Redimensionar y comprimir antes de subir
            val optimizedFront = optimizeImage(frontImage)
            val optimizedBack = optimizeImage(backImage)

            // 2. PREPARAR MULTIPART
            val frontPart = MultipartBody.Part.createFormData(
                "frontImage",
                optimizedFront.name,
                optimizedFront.asRequestBody("image/jpeg".toMediaTypeOrNull())
            )
            val backPart = MultipartBody.Part.createFormData(
                "backImage",
                optimizedBack.name,
                optimizedBack.asRequestBody("image/jpeg".toMediaTypeOrNull())
            )

            // 3. LLAMADA AL BACKEND
            val response = api.processDni(frontPart, backPart)

            // 4. MAPEAR RESPUESTA AL DOMINIO (Usando tu Result personalizado)
            Result.Success(DniData(
                numeroDni = response.numeroDni,
                apellidos = response.apellidos,
                nombres = response.nombres,
                fechaNacimiento = response.fechaNacimiento,
                sexo = response.sexo,
                nacionalidad = response.nacionalidad,
                fechaEmision = response.fechaEmision ?: "", // Pasar valor o vacío
                fechaVencimiento = response.fechaVencimiento ?: "",
                fotoPersonaBase64 = response.fotoPersona, // Ahora coincide con el DTO
                frontImageBase64 = response.frontImageBase64,
                backImageBase64 = response.backImageBase64
            ))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    private fun optimizeImage(file: File): File {
        return try {
            val options = BitmapFactory.Options().apply { inMutable = true }
            val originalBitmap = BitmapFactory.decodeFile(file.absolutePath, options) ?: throw Exception("No se pudo decodificar la imagen")

            // 1. Corregir Rotación
            val exif = androidx.exifinterface.media.ExifInterface(file.absolutePath)
            val orientation = exif.getAttributeInt(androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION, 1)
            val matrix = android.graphics.Matrix()
            when (orientation) {
                6 -> matrix.postRotate(90f)
                3 -> matrix.postRotate(180f)
                8 -> matrix.postRotate(270f)
            }
            val rotatedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true)

            // 2. Lógica de Recorte Segura
            val imgW = rotatedBitmap.width
            val imgH = rotatedBitmap.height
            val cropW = (imgW * 0.85f).toInt()
            val cropH = (cropW / 1.586f).toInt()

            // Aseguramos que las coordenadas estén dentro del rango permitido
            val startX = ((imgW - cropW) / 2).coerceIn(0, imgW - 1)
            val startY = ((imgH - cropH) / 2).coerceIn(0, imgH - 1)
            val finalCropW = cropW.coerceAtMost(imgW - startX)
            val finalCropH = cropH.coerceAtMost(imgH - startY)

            val croppedBitmap = Bitmap.createBitmap(rotatedBitmap, startX, startY, finalCropW, finalCropH)

            // 3. Escalado y Compresión
            val finalScale = 1080f / croppedBitmap.width
            val scaledBitmap = Bitmap.createScaledBitmap(croppedBitmap, 1080, (croppedBitmap.height * finalScale).toInt(), true)

            val optimizedFile = File(file.parent, "upload_${file.name}")
            FileOutputStream(optimizedFile).use { out ->
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
            }

            optimizedFile
        } catch (e: Exception) {
            android.util.Log.e("OCR_REPO", "Error procesando imagen: ${e.message}")
            file // Si falla el recorte, devolvemos el original para no bloquear el botón
        }
    }
}