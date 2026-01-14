package com.matfragg.rekognition_demo.data.reniec.remote.dto

data class ReniecRequestDto(
    val documentNumber: String,
    val serialNumber: String,
    val type: String = "01", // DNI
    val quality: Int = 80,
    val template: String    // La foto en Base64
)

data class ReniecResponseDto(
    val documentNumber: String?,
    val personName: String?,
    val personLastName: String?,
    val personMotherLastName: String?,
    val expirationDate: String?,
    val reniecErrorCode: Int?,
    val reniecErrorDescription: String?,
    val traking: String?
)