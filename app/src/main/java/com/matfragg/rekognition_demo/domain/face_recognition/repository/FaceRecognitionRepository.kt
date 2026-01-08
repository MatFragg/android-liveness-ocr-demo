package com.matfragg.rekognition_demo.domain.face_recognition.repository

import com.matfragg.rekognition_demo.domain.face_recognition.model.FaceAnalysis
import com.matfragg.rekognition_demo.domain.face_recognition.model.FaceComparison
import com.matfragg.rekognition_demo.shared.domain.Result
import java.io.File

interface FaceRecognitionRepository {
    suspend fun detectFace(image: File): Result<FaceAnalysis>
    suspend fun compareFaces(source: File, target: File): Result<FaceComparison>
}