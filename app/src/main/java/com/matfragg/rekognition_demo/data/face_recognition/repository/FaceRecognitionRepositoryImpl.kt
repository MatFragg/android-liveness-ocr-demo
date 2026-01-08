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
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject

class FaceRecognitionRepositoryImpl @Inject constructor(
    private val api: RekognitionApi,
    private val mapper: FaceRecognitionMapper
) : FaceRecognitionRepository {

    override suspend fun detectFace(image: File): Result<FaceAnalysis> {
        return try {
            val base64 = imageToBase64(image)
            val dto = api.detectFace(DetectFaceRequest(base64))
            Result.Success(mapper.toDomain(dto))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun compareFaces(source: File, target: File): Result<FaceComparison> {
        return try {
            val sourceBase64 = imageToBase64(source)
            val targetBase64 = imageToBase64(target)
            val dto = api.compareFaces(CompareFacesRequest(sourceBase64, targetBase64))
            Result.Success(mapper.toDomain(dto))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    private fun imageToBase64(file: File): String {
        val bitmap = file.toOptimizedBitmap()
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 40, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }
}