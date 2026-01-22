package com.matfragg.rekognition_demo.data.authentication.remote

import com.matfragg.rekognition_demo.data.authentication.remote.dto.AuthRequest
import com.matfragg.rekognition_demo.data.authentication.remote.dto.AuthResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {
    @POST("management/v1/access/token")
    suspend fun login(@Body request: AuthRequest): AuthResponse
}