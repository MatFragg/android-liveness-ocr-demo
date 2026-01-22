package com.matfragg.rekognition_demo.data.authentication.remote.dto

import com.google.gson.annotations.SerializedName

data class AuthRequest(val clientId: String, val clientSecret: String)
data class AuthResponse(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("tokenType") val tokenType: String,
    @SerializedName("expireIn") val expireIn: Long // ← Verifica que sea en MILISEGUNDOS
)

