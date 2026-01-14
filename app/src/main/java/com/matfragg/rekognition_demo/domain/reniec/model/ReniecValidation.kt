package com.matfragg.rekognition_demo.domain.reniec.model

data class ReniecValidation(
    val documentNumber: String,
    val names: String,
    val lastNames: String,
    val expirationDate: String,
    val nationality: String,
    val responseCode: String, // HIT o NO HIT
    val reniecCode: Int,      // 70006, 70007, etc.
    val trackingToken: String,
    val isMatch: Boolean
)