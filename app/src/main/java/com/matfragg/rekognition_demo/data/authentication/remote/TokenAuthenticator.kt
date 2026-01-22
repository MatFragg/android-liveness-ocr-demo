package com.matfragg.rekognition_demo.data.authentication.remote

import com.matfragg.rekognition_demo.domain.auth.repository.AuthRepository
import com.matfragg.rekognition_demo.shared.domain.Result
import com.matfragg.rekognition_demo.shared.util.Constants
import com.matfragg.rekognition_demo.shared.util.TokenManager
import dagger.Lazy
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

class TokenAuthenticator @Inject constructor(
    private val authRepositoryProvider: Lazy<AuthRepository>,
    private val tokenManager: TokenManager
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.priorResponse != null) return null

        val authRepository = authRepositoryProvider.get()

        val loginResult = runBlocking {
            authRepository.login(
                Constants.ACJ_API_CLIENT_ID,
                Constants.ACJ_API_CLIENT_SECRET
            )
        }

        return when (loginResult) {
            is Result.Success -> {
                val newToken = loginResult.data.accessToken
                tokenManager.saveToken(newToken)
                response.request.newBuilder()
                    .header("Authorization", "Bearer $newToken")
                    .build()
            }
            is Result.Error -> null
            else -> null
        }
    }
}