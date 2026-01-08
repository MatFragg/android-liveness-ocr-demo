package com.matfragg.rekognition_demo.data.face_recognition.remote

import com.matfragg.rekognition_demo.data.face_recognition.remote.dto.FaceComparisonDto
import com.matfragg.rekognition_demo.data.face_recognition.remote.dto.FaceDetectionDto
import retrofit2.http.Body
import retrofit2.http.POST

data class DetectFaceRequest(
    val imageBase64: String
)

data class CompareFacesRequest(
    val sourceImage: String,
    val targetImage: String
)

interface RekognitionApi {
    @POST("/")
    suspend fun detectFace(@Body request: DetectFaceRequest): FaceDetectionDto

    @POST("/")
    suspend fun compareFaces(@Body request: CompareFacesRequest): FaceComparisonDto
}