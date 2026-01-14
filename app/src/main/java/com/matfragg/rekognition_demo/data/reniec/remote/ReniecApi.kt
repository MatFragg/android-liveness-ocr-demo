package com.matfragg.rekognition_demo.data.reniec.remote

import com.matfragg.rekognition_demo.data.reniec.remote.dto.ReniecRequestDto
import com.matfragg.rekognition_demo.data.reniec.remote.dto.ReniecResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface ReniecApi {
    @POST("api/consultas/validacion-facial")
    suspend fun validateFacial(@Body request: ReniecRequestDto): ReniecResponseDto
}