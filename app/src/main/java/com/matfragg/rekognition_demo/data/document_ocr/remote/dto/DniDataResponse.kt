package com.matfragg.rekognition_demo.data.document_ocr.remote.dto

data class DniDataResponse(
    val numeroDni: String,
    val apellidos: String,
    val nombres: String,
    val fechaNacimiento: String,
    val sexo: String,
    val nacionalidad: String,
    val fechaEmision: String?,    // Cambiado a opcional
    val fechaVencimiento: String?, // Cambiado a opcional
    val fotoPersona: String?,      // IMPORTANTE: Debe llamarse igual que en Spring
    val frontImageBase64: String?,
    val backImageBase64: String?
)