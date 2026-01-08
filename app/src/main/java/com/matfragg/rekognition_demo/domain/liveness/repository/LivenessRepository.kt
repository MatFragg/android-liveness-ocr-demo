package com.matfragg.rekognition_demo.domain.liveness.repository

import com.matfragg.rekognition_demo.domain.liveness.model.LivenessResult
import com.matfragg.rekognition_demo.domain.liveness.model.LivenessSession
import com.matfragg.rekognition_demo.shared.domain.Result

interface LivenessRepository {
    suspend fun createSession(): Result<LivenessSession>
    suspend fun getResult(sessionId: String): Result<LivenessResult>
}