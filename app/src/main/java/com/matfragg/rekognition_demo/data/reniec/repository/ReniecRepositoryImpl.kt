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
            // 1. Preparamos el DTO para el backend
            val request = ReniecRequestDto(
                documentNumber = dni,
                serialNumber = serial,
                template = photoBase64
            )

            // 2. Llamada a tu API de Spring Boot
            val response = api.validateFacial(request)

            // 3. Mapeo a modelo de Dominio
            // Nota: Si el backend devuelve 200, asumimos que es un éxito de red.
            // La lógica de HIT/NO HIT se evalúa en el mapper.
            val domainModel = mapper.toDomain(response)

            Result.Success(domainModel)

        } catch (e: Exception) {
            // Manejo de errores de red o errores 400/500 del backend
            Result.Error(e)
        }
    }
}