package com.matfragg.rekognition_demo.di

import com.matfragg.rekognition_demo.data.reniec.mapper.ReniecMapper
import com.matfragg.rekognition_demo.data.reniec.remote.ReniecApi
import com.matfragg.rekognition_demo.data.reniec.repository.ReniecRepositoryImpl
import com.matfragg.rekognition_demo.domain.reniec.repository.ReniecRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ReniecModule {


    @Provides
    @Singleton
    fun provideReniecApi(
        @Named("spring_backend") retrofit: Retrofit // <--- ADD THIS ANNOTATION
    ): ReniecApi {
        return retrofit.create(ReniecApi::class.java)
    }

    @Provides
    @Singleton
    fun provideReniecMapper(): ReniecMapper = ReniecMapper()

    @Provides
    @Singleton
    fun provideReniecRepository(api: ReniecApi, mapper: ReniecMapper): ReniecRepository =
        ReniecRepositoryImpl(api, mapper)
}
