package com.matfragg.rekognition_demo.data.reniec.mapper

import com.matfragg.rekognition_demo.data.reniec.remote.dto.ReniecResponseDto
import com.matfragg.rekognition_demo.domain.reniec.model.ReniecValidation

class ReniecMapper {
    fun toDomain(dto: ReniecResponseDto): ReniecValidation {
        return ReniecValidation(
            documentNumber = dto.documentNumber ?: "",
            names = dto.personName ?: "",
            lastNames = "${dto.personLastName ?: ""} ${dto.personMotherLastName ?: ""}".trim(),
            expirationDate = dto.expirationDate ?: "",
            nationality = "PERUANA",
            responseCode = if (dto.reniecErrorCode == 70006) "HIT" else "NO HIT",
            reniecCode = dto.reniecErrorCode ?: 0,
            trackingToken = dto.traking ?: "",
            isMatch = dto.reniecErrorCode == 70006
        )
    }
}