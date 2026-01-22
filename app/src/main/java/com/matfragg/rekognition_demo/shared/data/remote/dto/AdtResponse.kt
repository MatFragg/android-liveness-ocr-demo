package com.matfragg.rekognition_demo.shared.data.remote.dto

data class AdtResponse<T>(
    val result: AdtResult?,
    val data: T?
)

data class AdtResult(
    val code: String?,
    val info: String?
)