package com.matfragg.rekognition_demo.domain.document_ocr.repository

import com.matfragg.rekognition_demo.domain.document_ocr.model.DniData
import com.matfragg.rekognition_demo.shared.domain.Result
import java.io.File

interface DocumentOcrRepository {
    suspend fun processDni(frontImage: File, backImage: File): Result<DniData>
}
