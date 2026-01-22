package com.matfragg.rekognition_demo.data.reniec.remote.dto

data class ReniecRequestDto(
    val documentNumber: String,
    val serialNumber: String = "123456789", // Opcional: obtener del dispositivo
    val type: String = "R",                // Valor por defecto según PDF
    val quality: String = "7",             // Valor ejemplo según PDF
    val template: String                   // Foto en Base64
)

data class ReniecResponseDto(
    val reniecErrorCode: Int?,
    val reniecErrorDescription: String?,
    val documentNumber: String?,
    val personName: String?,
    val personLastName: String?,
    val personMotherLastName: String?,
    val expirationDate: String?,
    val validity: String?,
    val restriction: String?,
    val restrictionGroup: String?,
    val traking: String?
)