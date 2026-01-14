package com.matfragg.rekognition_demo.data.liveness.mapper

import com.matfragg.rekognition_demo.data.liveness.remote.dto.LivenessResultDto
import com.matfragg.rekognition_demo.data.liveness.remote.dto.LivenessSessionDto
import com.matfragg.rekognition_demo.domain.liveness.model.LivenessResult
import com.matfragg.rekognition_demo.domain.liveness.model.LivenessSession
import com.matfragg.rekognition_demo.domain.liveness.model.LivenessStatus

class LivenessMapper {

    fun toDomain(dto: LivenessSessionDto): LivenessSession {
        return LivenessSession(sessionId = dto.sessionId)
    }

    fun toDomain(dto: LivenessResultDto): LivenessResult {
        return LivenessResult(
            isLive = dto.isLive,
            confidence = dto.confidence,
            status = when (dto.status.lowercase()) {
                "succeeded" -> LivenessStatus.SUCCEEDED
                "failed" -> LivenessStatus.FAILED
                else -> LivenessStatus.ERROR
            },
            fotoBase64 = dto.referenceImage?.bytes
        )
    }
}