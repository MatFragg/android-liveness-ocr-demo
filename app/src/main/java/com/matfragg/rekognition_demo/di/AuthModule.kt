package com.matfragg.rekognition_demo.di

import android.content.Context
import com.matfragg.rekognition_demo.data.authentication.remote.AuthApi
import com.matfragg.rekognition_demo.data.authentication.repository.AuthRepositoryImpl
import com.matfragg.rekognition_demo.domain.auth.repository.AuthRepository
import com.matfragg.rekognition_demo.shared.util.TokenManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideAuthRepository(api: AuthApi): AuthRepository = AuthRepositoryImpl(api)

    @Provides
    @Singleton
    fun provideTokenManager(@ApplicationContext context: Context): TokenManager {
        return TokenManager(context)
    }
}

