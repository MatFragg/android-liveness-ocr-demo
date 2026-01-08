package com.matfragg.rekognition_demo.data.liveness.remote

import com.matfragg.rekognition_demo.data.liveness.remote.dto.LivenessResultDto
import com.matfragg.rekognition_demo.data.liveness.remote.dto.LivenessSessionDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

data class CreateSessionRequest(
    val mode: String = "create_liveness_session"
)

data class GetResultRequest(
    val mode: String = "get_liveness_results",
    val sessionId: String
)

interface LivenessApi {
    /*@POST("/")
    suspend fun createSession(@Body request: CreateSessionRequest): LivenessSessionDto*/

    @POST("create-session")  // Ruta diferente para distinguir
    suspend fun createSession(): LivenessSessionDto

    /*@POST("/")
    suspend fun getResult(@Body request: GetResultRequest): LivenessResultDto*/

    @GET("results/{sessionId}")  // Sobrecarga con ruta y parámetro de ruta
    suspend fun getResult(@Path("sessionId") sessionId: String): LivenessResultDto
}