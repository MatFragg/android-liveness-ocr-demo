package com.matfragg.rekognition_demo.domain.auth.usecase

import com.matfragg.rekognition_demo.domain.auth.repository.AuthRepository
import javax.inject.Inject
import com.matfragg.rekognition_demo.shared.domain.Result

class LoginUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(clientId: String, clientSecret: String) =
        repository.login(clientId, clientSecret)
}