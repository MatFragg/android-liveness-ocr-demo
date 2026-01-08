package com.matfragg.rekognition_demo.data.liveness.repository

import com.matfragg.rekognition_demo.data.liveness.mapper.LivenessMapper
import com.matfragg.rekognition_demo.data.liveness.remote.CreateSessionRequest
import com.matfragg.rekognition_demo.data.liveness.remote.GetResultRequest
import com.matfragg.rekognition_demo.data.liveness.remote.LivenessApi
import com.matfragg.rekognition_demo.domain.liveness.model.LivenessResult
import com.matfragg.rekognition_demo.domain.liveness.model.LivenessSession
import com.matfragg.rekognition_demo.domain.liveness.repository.LivenessRepository
import com.matfragg.rekognition_demo.shared.domain.Result
import com.matfragg.rekognition_demo.shared.domain.runCatching
import javax.inject.Inject

class LivenessRepositoryImpl @Inject constructor(
    private val api: LivenessApi,
    private val mapper: LivenessMapper
) : LivenessRepository {

    override suspend fun createSession(): Result<LivenessSession> {
        return try {
            //val dto = api.createSession(CreateSessionRequest())
            val dto = api.createSession()
            Result.Success(mapper.toDomain(dto))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getResult(sessionId: String): Result<LivenessResult> {
        return try {
            //val dto = api.getResult(GetResultRequest(sessionId = sessionId))
            val dto = api.getResult(sessionId)
            Result.Success(mapper.toDomain(dto))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}