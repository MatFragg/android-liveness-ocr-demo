package com.matfragg.rekognition_demo.data.face_recognition.remote

import com.matfragg.rekognition_demo.data.authentication.remote.dto.AuthRequest
import com.matfragg.rekognition_demo.data.authentication.remote.dto.AuthResponse
import com.matfragg.rekognition_demo.data.face_recognition.remote.dto.FaceComparisonDto
import com.matfragg.rekognition_demo.data.face_recognition.remote.dto.FaceDetectionDto
import com.matfragg.rekognition_demo.data.face_recognition.remote.dto.FaceDetectionResponseDto
import com.matfragg.rekognition_demo.data.face_recognition.remote.dto.NewCompareRequest
import com.matfragg.rekognition_demo.data.face_recognition.remote.dto.NewCompareResponseDto
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface RekognitionApi {
    // BACKEND
    @Multipart
    @POST("detect-faces")
    suspend fun detectFace(@Part image: MultipartBody.Part): FaceDetectionResponseDto

    /*@Multipart
    @POST("compare-files")
    suspend fun compareFaces(@Part sourceImage: MultipartBody.Part,
                             @Part targetImage: MultipartBody.Part): FaceComparisonDto*/

    @POST("management/v1/facial-biometrics/compare")
    suspend fun compareFaces(@Body request: NewCompareRequest): NewCompareResponseDto
}