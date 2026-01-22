package com.matfragg.rekognition_demo.data.reniec.repository

import com.matfragg.rekognition_demo.data.reniec.mapper.ReniecMapper
import com.matfragg.rekognition_demo.data.reniec.remote.ReniecApi
import com.matfragg.rekognition_demo.data.reniec.remote.dto.ReniecRequestDto
import com.matfragg.rekognition_demo.domain.reniec.model.ReniecValidation
import com.matfragg.rekognition_demo.domain.reniec.repository.ReniecRepository
import com.matfragg.rekognition_demo.shared.domain.Result
import javax.inject.Inject

class ReniecRepositoryImpl @Inject constructor(
    private val api: ReniecApi,
    private val mapper: ReniecMapper
) : ReniecRepository {

    override suspend fun validateFacial(
        dni: String,
        serial: String,
        photoBase64: String
    ): Result<ReniecValidation> {
        return try {
            val request = ReniecRequestDto(
                documentNumber = dni,
                serialNumber = serial,
                template = photoBase64
            )

            val responseWrapper = api.validateFacial(request)

            val code = responseWrapper.result?.code ?: ""
            // Validamos el éxito de la transacción según el código de ACJ
            if ((code == "000" || code == "0000") && responseWrapper.data != null) {
                Result.Success(mapper.toDomain(responseWrapper.data))
            } else {
                val errorMsg = responseWrapper.result?.info ?: "Error desconocido en RENIEC"
                Result.Error(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}