package com.matfragg.rekognition_demo.domain.reniec.repository

import com.matfragg.rekognition_demo.domain.reniec.model.ReniecValidation
import com.matfragg.rekognition_demo.shared.domain.Result

interface ReniecRepository {
    suspend fun validateFacial(
        dni: String,
        serial: String,
        photoBase64: String
    ): Result<ReniecValidation>
}