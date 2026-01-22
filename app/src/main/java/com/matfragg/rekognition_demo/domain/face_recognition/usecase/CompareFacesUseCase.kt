package com.matfragg.rekognition_demo.domain.face_recognition.usecase

import com.matfragg.rekognition_demo.domain.auth.repository.AuthRepository
import com.matfragg.rekognition_demo.domain.face_recognition.model.FaceComparison
import com.matfragg.rekognition_demo.domain.face_recognition.repository.FaceRecognitionRepository
import com.matfragg.rekognition_demo.shared.util.Constants
import com.matfragg.rekognition_demo.shared.util.TokenManager
import com.matfragg.rekognition_demo.shared.domain.Result

import java.io.File
import javax.inject.Inject

class CompareFacesUseCase @Inject constructor(
    private val repository: FaceRecognitionRepository,
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) {
    suspend operator fun invoke(source: File, target: File): Result<FaceComparison> {
        // 1. Validaciones básicas de negocio
        if (!source.exists() || !target.exists()) {
            return Result.Error(IllegalArgumentException("Una o ambas imágenes no existen"))
        }

        // 2. Verificación proactiva del token
        if (tokenManager.getToken() == null) {
            val authResult = authRepository.login(
                Constants.ACJ_API_CLIENT_ID,
                Constants.ACJ_API_CLIENT_SECRET
            )

            // Si el login falla, devolvemos el error inmediatamente
            if (authResult is Result.Error) {
                return Result.Error(Exception("No se pudo obtener el token de acceso"))
            }

            // Si tiene éxito, el TokenManager ya guardó el token mediante el repositorio
        }

        // 3. Ejecutar la comparación (con la seguridad de que el token existe)
        return repository.compareFaces(source, target)
    }
}