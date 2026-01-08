package com.matfragg.rekognition_demo.domain.face_recognition.usecase

import com.matfragg.rekognition_demo.domain.face_recognition.model.FaceComparison
import com.matfragg.rekognition_demo.domain.face_recognition.repository.FaceRecognitionRepository
import com.matfragg.rekognition_demo.shared.domain.Result
import java.io.File
import javax.inject.Inject

class CompareFacesUseCase @Inject constructor(
    private val repository: FaceRecognitionRepository
) {
    suspend operator fun invoke(source: File, target: File): Result<FaceComparison> {
        if (!source.exists() || !target.exists()) {
            return Result.Error(IllegalArgumentException("Una o ambas imágenes no existen"))
        }

        return repository.compareFaces(source, target)
    }
}
