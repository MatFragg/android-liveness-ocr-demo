package com.matfragg.rekognition_demo.domain.face_recognition.usecase

import com.matfragg.rekognition_demo.domain.face_recognition.model.FaceAnalysis
import com.matfragg.rekognition_demo.domain.face_recognition.repository.FaceRecognitionRepository
import com.matfragg.rekognition_demo.shared.domain.Result
import java.io.File
import javax.inject.Inject

class DetectFaceUseCase @Inject constructor(
    private val repository: FaceRecognitionRepository
) {
    suspend operator fun invoke(image: File): Result<FaceAnalysis> {
        if (!image.exists()) {
            return Result.Error(IllegalArgumentException("La imagen no existe"))
        }

        return repository.detectFace(image)
    }
}