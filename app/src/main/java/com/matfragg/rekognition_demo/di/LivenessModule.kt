package com.matfragg.rekognition_demo.di

import com.matfragg.rekognition_demo.data.liveness.mapper.LivenessMapper
import com.matfragg.rekognition_demo.data.liveness.remote.LivenessApi
import com.matfragg.rekognition_demo.data.liveness.repository.LivenessRepositoryImpl
import com.matfragg.rekognition_demo.domain.liveness.repository.LivenessRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LivenessModule {

    @Provides
    @Singleton
    fun provideLivenessMapper(): LivenessMapper = LivenessMapper()

    @Provides
    @Singleton
    fun provideLivenessRepository(
        api: LivenessApi,
        mapper: LivenessMapper
    ): LivenessRepository = LivenessRepositoryImpl(api, mapper)
}
