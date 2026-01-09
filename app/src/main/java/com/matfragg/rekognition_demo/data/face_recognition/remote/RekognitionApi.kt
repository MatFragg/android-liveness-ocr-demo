package com.matfragg.rekognition_demo.data.face_recognition.remote

import com.matfragg.rekognition_demo.data.face_recognition.remote.dto.FaceComparisonDto
import com.matfragg.rekognition_demo.data.face_recognition.remote.dto.FaceDetectionDto
import com.matfragg.rekognition_demo.data.face_recognition.remote.dto.FaceDetectionResponseDto
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

data class DetectFaceRequest(
    val image: String
)

data class CompareFacesRequest(
    val sourceImage: String,
    val targetImage: String
)

interface RekognitionApi {
    @Multipart
    @POST("detect-faces")
    suspend fun detectFace(@Part image: MultipartBody.Part): FaceDetectionResponseDto

    @Multipart
    @POST("compare-files")
    suspend fun compareFaces(@Part sourceImage: MultipartBody.Part,
                             @Part targetImage: MultipartBody.Part): FaceComparisonDto
}