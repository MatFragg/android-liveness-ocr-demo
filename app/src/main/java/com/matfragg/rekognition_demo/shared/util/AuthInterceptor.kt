package com.matfragg.rekognition_demo.shared.util

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val token = tokenManager.getToken()

        val builder = request.newBuilder()
            // Cambiar a "Channel" y "API" como el de tu compañero
            .addHeader("Channel", "API")

        // No agregar Bearer si es la ruta de login
        if (!request.url.encodedPath.contains("access/token") && !token.isNullOrBlank()) {
            builder.addHeader("Authorization", "$token")
        }

        return chain.proceed(builder.build())
    }
}