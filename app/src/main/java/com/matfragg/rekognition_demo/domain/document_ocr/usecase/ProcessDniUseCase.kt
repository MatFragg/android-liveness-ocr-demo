package com.matfragg.rekognition_demo.domain.document_ocr.usecase

import com.matfragg.rekognition_demo.domain.document_ocr.model.DniData
import com.matfragg.rekognition_demo.domain.document_ocr.repository.DocumentOcrRepository
import com.matfragg.rekognition_demo.shared.domain.Result
import java.io.File
import javax.inject.Inject

class ProcessDniUseCase @Inject constructor(
    private val repository: DocumentOcrRepository
) {
    suspend operator fun invoke(frontImage: File, backImage: File): Result<DniData> {
        // Validaciones
        if (!frontImage.exists() || !backImage.exists()) {
            return Result.Error(IllegalArgumentException("Las imágenes del DNI son requeridas"))
        }

        if (frontImage.length() > 10 * 1024 * 1024 || backImage.length() > 10 * 1024 * 1024) {
            return Result.Error(IllegalArgumentException("Las imágenes son demasiado grandes (máx 10MB cada una)"))
        }

        return repository.processDni(frontImage, backImage)
    }
}