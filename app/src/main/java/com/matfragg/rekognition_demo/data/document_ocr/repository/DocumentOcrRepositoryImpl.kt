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
            val originalBitmap = BitmapFactory.decodeFile(file.absolutePath, options) ?: return file

            // 1. Corregir Rotación (Mantener esto)
            val exif = androidx.exifinterface.media.ExifInterface(file.absolutePath)
            val orientation = exif.getAttributeInt(androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION, 1)
            val matrix = android.graphics.Matrix()
            when (orientation) {
                6 -> matrix.postRotate(90f)
                3 -> matrix.postRotate(180f)
                8 -> matrix.postRotate(270f)
            }
            val rotatedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true)

            // ❌ ELIMINA TODO EL BLOQUE DE "Lógica de Recorte Segura" (val cropW, val cropH, etc.)
            // No debemos recortar aquí porque ImageUtils ya recortó el área del DNI.

            // 2. Solo redimensionar para asegurar que el backend reciba un tamaño estándar y comprimir
            val targetWidth = 1080
            val scale = targetWidth.toFloat() / rotatedBitmap.width
            val finalBitmap = Bitmap.createScaledBitmap(rotatedBitmap, targetWidth, (rotatedBitmap.height * scale).toInt(), true)

            val optimizedFile = File(file.parent, "upload_${file.name}")
            FileOutputStream(optimizedFile).use { out ->
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out) // 85% de calidad es ideal
            }
            optimizedFile
        } catch (e: Exception) {
            file
        }
    }
}