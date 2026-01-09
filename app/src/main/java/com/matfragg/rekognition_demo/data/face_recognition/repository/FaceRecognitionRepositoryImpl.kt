package com.matfragg.rekognition_demo.data.face_recognition.repository

import android.util.Base64
import com.matfragg.rekognition_demo.data.face_recognition.mapper.FaceRecognitionMapper
import com.matfragg.rekognition_demo.data.face_recognition.remote.CompareFacesRequest
import com.matfragg.rekognition_demo.data.face_recognition.remote.DetectFaceRequest
import com.matfragg.rekognition_demo.data.face_recognition.remote.RekognitionApi
import com.matfragg.rekognition_demo.domain.face_recognition.model.FaceAnalysis
import com.matfragg.rekognition_demo.domain.face_recognition.model.FaceComparison
import com.matfragg.rekognition_demo.domain.face_recognition.repository.FaceRecognitionRepository
import com.matfragg.rekognition_demo.shared.domain.Result
import com.matfragg.rekognition_demo.shared.util.toOptimizedBitmap
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

class FaceRecognitionRepositoryImpl @Inject constructor(
    private val api: RekognitionApi,
    private val mapper: FaceRecognitionMapper
) : FaceRecognitionRepository {

    override suspend fun detectFace(image: File): Result<FaceAnalysis> {
        return try {
            val part = fileToMultipart(image, "image")

            val dto = api.detectFace(part)
            Result.Success(mapper.toDomain(dto))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun compareFaces(source: File, target: File): Result<FaceComparison> {
        return try {
            val sourcePart = fileToMultipart(source, "sourceImage")
            val targetPart = fileToMultipart(target, "targetImage")

            val dto = api.compareFaces(sourcePart, targetPart)
            Result.Success(mapper.toDomain(dto))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    private fun fileToMultipart(file: File, partName: String): MultipartBody.Part {
        val bitmap = file.toOptimizedBitmap()

        val optimizedFile = File(file.parent, "temp_upload_${file.name}")
        java.io.FileOutputStream(optimizedFile).use { out ->
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, out)
        }

        val requestFile = optimizedFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName, optimizedFile.name, requestFile)
    }
}