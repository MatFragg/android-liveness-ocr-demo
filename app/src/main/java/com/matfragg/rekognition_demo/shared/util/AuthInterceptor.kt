package com.matfragg.rekognition_demo.shared.util

import com.matfragg.rekognition_demo.domain.auth.repository.AuthRepository
import com.matfragg.rekognition_demo.shared.domain.Result
import dagger.Lazy
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import android.util.Log
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager,
    private val authRepository: Lazy<AuthRepository>
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val currentUrl = originalRequest.url.toString()

        // 1. Evitar bucles en llamadas de Auth
        if (currentUrl.contains("/token") || currentUrl.contains("/access")) {
            return chain.proceed(originalRequest)
        }

        val requestBuilder = originalRequest.newBuilder()
            .header("Channel", "API")

        var token = tokenManager.getToken()

        // 2. CAMBIO CLAVE: Chequeamos si es null O si está expirado
        if (token.isNullOrEmpty() || tokenManager.isTokenExpired(token)) {

            synchronized(this) {
                token = tokenManager.getToken()

                // Doble chequeo dentro del synchronized
                if (token.isNullOrEmpty() || tokenManager.isTokenExpired(token)) {
                    Log.w("INTERCEPTOR", "⚠️ Token inválido o expirado. Renovando ANTES de enviar...")

                    val loginResult = runBlocking {
                        authRepository.get().login(
                            Constants.ACJ_API_CLIENT_ID,
                            Constants.ACJ_API_CLIENT_SECRET
                        )
                    }

                    if (loginResult is Result.Success) {
                        val newToken = loginResult.data.accessToken
                        tokenManager.saveToken(newToken)
                        token = newToken
                        Log.i("INTERCEPTOR", "✅ Token renovado proactivamente.")
                    } else {
                        Log.e("INTERCEPTOR", "❌ Falló la renovación proactiva.")
                        // Podrías decidir limpiar el token aquí si falla
                        // tokenManager.clearToken()
                    }
                }
            }
        }

        if (!token.isNullOrEmpty()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        return chain.proceed(requestBuilder.build())
    }
}