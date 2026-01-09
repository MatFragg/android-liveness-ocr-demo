package com.matfragg.rekognition_demo.di

import com.matfragg.rekognition_demo.data.document_ocr.parser.DniFieldExtractor
import com.matfragg.rekognition_demo.data.document_ocr.parser.DniParserService
import com.matfragg.rekognition_demo.data.document_ocr.remote.DniApi
import com.matfragg.rekognition_demo.data.document_ocr.repository.DocumentOcrRepositoryImpl
import com.matfragg.rekognition_demo.domain.document_ocr.repository.DocumentOcrRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DocumentOcrModule {

    @Provides
    @Singleton
    fun provideDniFieldExtractor(): DniFieldExtractor {
        return DniFieldExtractor()
    }

    @Provides
    @Singleton
    fun provideDniParserService(
        fieldExtractor: DniFieldExtractor
    ): DniParserService {
        return DniParserService(fieldExtractor)
    }

    @Provides
    @Singleton
    fun provideDocumentOcrRepository(
        api: DniApi
    ): DocumentOcrRepository {
        // DocumentOcrRepositoryImpl actualmente espera solo un DniApi
        return DocumentOcrRepositoryImpl(api)
    }
}
