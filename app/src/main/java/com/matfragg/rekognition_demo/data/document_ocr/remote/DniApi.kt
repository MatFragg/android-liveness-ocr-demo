package com.matfragg.rekognition_demo.data.document_ocr.remote

import com.matfragg.rekognition_demo.data.document_ocr.remote.dto.DniDataResponse
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface DniApi {
    @Multipart
    @POST("api/dni/process")
    suspend fun processDni(
        @Part frontImage: MultipartBody.Part,
        @Part backImage: MultipartBody.Part
    ): DniDataResponse
}