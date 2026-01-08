package com.matfragg.rekognition_demo.domain.liveness.usecase

import com.matfragg.rekognition_demo.domain.liveness.model.LivenessResult
import com.matfragg.rekognition_demo.domain.liveness.repository.LivenessRepository
import com.matfragg.rekognition_demo.shared.domain.Result
import javax.inject.Inject

class GetLivenessResultUseCase @Inject constructor(
    private val repository: LivenessRepository
) {
    suspend operator fun invoke(sessionId: String): Result<LivenessResult> {
        if (sessionId.isBlank()) {
            return Result.Error(IllegalArgumentException("Session ID inválido"))
        }

        return repository.getResult(sessionId)
    }
}
