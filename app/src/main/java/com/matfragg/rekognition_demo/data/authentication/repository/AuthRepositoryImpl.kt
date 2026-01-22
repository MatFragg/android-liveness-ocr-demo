package com.matfragg.rekognition_demo.data.authentication.repository

import android.util.Log
import com.matfragg.rekognition_demo.data.authentication.remote.AuthApi
import com.matfragg.rekognition_demo.data.authentication.remote.dto.AuthRequest
import com.matfragg.rekognition_demo.shared.domain.Result
import com.matfragg.rekognition_demo.data.authentication.remote.dto.AuthResponse
import com.matfragg.rekognition_demo.domain.auth.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi
) : AuthRepository {
    override suspend fun login(clientId: String, clientSecret: String): Result<AuthResponse> {
        return try {
            val response = api.login(AuthRequest(clientId, clientSecret))
            Log.d("AUTH_REPO", "${response}")

            Log.d("AUTH_REPO", "Token recibido: ${response.accessToken}")
            Result.Success(response)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}