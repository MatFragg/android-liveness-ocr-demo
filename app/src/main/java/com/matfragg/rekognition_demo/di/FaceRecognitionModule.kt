package com.matfragg.rekognition_demo.di

import com.matfragg.rekognition_demo.data.face_recognition.mapper.FaceRecognitionMapper
import com.matfragg.rekognition_demo.data.face_recognition.remote.RekognitionApi
import com.matfragg.rekognition_demo.data.face_recognition.repository.FaceRecognitionRepositoryImpl
import com.matfragg.rekognition_demo.domain.face_recognition.repository.FaceRecognitionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FaceRecognitionModule {

    @Provides
    @Singleton
    fun provideFaceRecognitionMapper(): FaceRecognitionMapper = FaceRecognitionMapper()

    @Provides
    @Singleton
    fun provideFaceRecognitionRepository(
        api: RekognitionApi,
        mapper: FaceRecognitionMapper
    ): FaceRecognitionRepository = FaceRecognitionRepositoryImpl(api, mapper)
}

