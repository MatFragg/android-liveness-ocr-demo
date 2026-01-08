package com.matfragg.rekognition_demo.domain.liveness.usecase

import com.matfragg.rekognition_demo.domain.liveness.model.LivenessSession
import com.matfragg.rekognition_demo.domain.liveness.repository.LivenessRepository
import com.matfragg.rekognition_demo.shared.domain.Result
import javax.inject.Inject

class CreateLivenessSessionUseCase @Inject constructor(
    private val repository: LivenessRepository
) {
    suspend operator fun invoke(): Result<LivenessSession> {
        return repository.createSession()
    }
}