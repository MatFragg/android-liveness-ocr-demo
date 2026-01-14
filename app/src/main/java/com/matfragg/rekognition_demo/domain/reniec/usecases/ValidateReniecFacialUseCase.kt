package com.matfragg.rekognition_demo.domain.reniec.usecases

import com.matfragg.rekognition_demo.shared.domain.Result
import com.matfragg.rekognition_demo.domain.reniec.model.ReniecValidation
import com.matfragg.rekognition_demo.domain.reniec.repository.ReniecRepository
import javax.inject.Inject

class ValidateReniecFacialUseCase @Inject constructor(
    private val repository: ReniecRepository
) {
    suspend operator fun invoke(
        dni: String,
        serial: String,
        photoBase64: String
    ): Result<ReniecValidation> {
        // Aquí podrías agregar lógica de negocio adicional si fuera necesario
        // (ej. validar que el DNI tenga 8 dígitos antes de llamar al repo)
        return repository.validateFacial(dni, serial, photoBase64)
    }
}