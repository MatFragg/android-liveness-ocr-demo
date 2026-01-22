package com.matfragg.rekognition_demo.domain.auth.repository

import com.matfragg.rekognition_demo.data.authentication.remote.dto.AuthResponse
import com.matfragg.rekognition_demo.shared.domain.Result

interface AuthRepository {
    suspend fun login(clientId: String, clientSecret: String): Result<AuthResponse>
}