package com.matfragg.rekognition_demo.domain.document_ocr.usecase

import com.matfragg.rekognition_demo.domain.document_ocr.model.DniData
import com.matfragg.rekognition_demo.shared.domain.Result
import javax.inject.Inject

class ValidateDniDataUseCase @Inject constructor() {

    operator fun invoke(dniData: DniData): Result<Boolean> {
        val errors = mutableListOf<String>()

        // Validar número de DNI (8 dígitos en Perú)
        if (!isValidDniNumber(dniData.numeroDni)) {
            errors.add("Número de DNI inválido")
        }

        // Validar que los campos obligatorios no estén vacíos
        if (dniData.apellidos.isBlank()) {
            errors.add("Apellidos son requeridos")
        }

        if (dniData.nombres.isBlank()) {
            errors.add("Nombres son requeridos")
        }

        // Validar formato de fecha de nacimiento
        if (!isValidDate(dniData.fechaNacimiento)) {
            errors.add("Fecha de nacimiento inválida")
        }

        // Validar sexo
        if (!isValidSex(dniData.sexo)) {
            errors.add("Sexo inválido")
        }

        return if (errors.isEmpty()) {
            Result.Success(true)
        } else {
            Result.Error(IllegalArgumentException(errors.joinToString("; ")))
        }
    }

    private fun isValidDniNumber(dni: String): Boolean {
        return dni.matches(Regex("^\\d{8}$"))
    }

    private fun isValidDate(date: String): Boolean {
        // Formato: DD/MM/YYYY o DD-MM-YYYY
        return date.matches(Regex("^\\d{2}[/-]\\d{2}[/-]\\d{4}$"))
    }

    private fun isValidSex(sex: String): Boolean {
        return sex.uppercase() in listOf("M", "F", "MASCULINO", "FEMENINO")
    }
}