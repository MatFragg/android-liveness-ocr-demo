package com.matfragg.rekognition_demo.domain.document_ocr.model

data class DniData(
    val numeroDni: String,
    val apellidos: String,
    val nombres: String,
    val fechaNacimiento: String,
    val sexo: String,
    val nacionalidad: String,
    val fechaEmision: String,
    val fechaVencimiento: String,
    val fotoPersonaBase64: String? = null,
    val frontImageBase64: String? = null,
    val backImageBase64: String? = null,
    val confidence: Double = 0.0
) {
    fun isValid(): Boolean {
        return numeroDni.isNotBlank() &&
                apellidos.isNotBlank() &&
                nombres.isNotBlank() &&
                fechaNacimiento.isNotBlank()
    }

    fun getFullName(): String = "$nombres $apellidos"

    fun isExpired(): Boolean {
        // Implementar lógica de validación de fecha
        return false // Placeholder
    }
}