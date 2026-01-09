package com.matfragg.rekognition_demo.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.matfragg.rekognition_demo.data.document_ocr.remote.DniApi
import com.matfragg.rekognition_demo.data.face_recognition.remote.RekognitionApi
import com.matfragg.rekognition_demo.data.liveness.remote.LivenessApi
import com.matfragg.rekognition_demo.shared.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder()
        .setLenient()
        .create()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = okhttp3.logging.HttpLoggingInterceptor().apply {
            level = okhttp3.logging.HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(Constants.CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
            .readTimeout(Constants.READ_TIMEOUT, TimeUnit.MILLISECONDS)
            .build()
    }

    @Provides
    @Singleton
    @Named("detect")
    fun provideDetectRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit = Retrofit.Builder()
        .baseUrl(Constants.LAMBDA_DETECT_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    @Provides
    @Singleton
    @Named("compare")
    fun provideCompareRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit = Retrofit.Builder()
        .baseUrl(Constants.LAMBDA_COMPARE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    @Provides
    @Singleton
    @Named("spring_backend")
    fun provideSpringRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit = Retrofit.Builder()
        .baseUrl(Constants.DNI_OCR_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    @Provides
    @Singleton
    fun provideRekognitionApi(
        @Named("compare") retrofit: Retrofit
    ): RekognitionApi = retrofit.create(RekognitionApi::class.java)

    @Provides
    @Singleton
    fun provideLivenessApi(
        @Named("detect") retrofit: Retrofit
    ): LivenessApi = retrofit.create(LivenessApi::class.java)

    @Provides
    @Singleton
    fun provideDniApi(
        @Named("spring_backend") retrofit: Retrofit
    ): DniApi = retrofit.create(DniApi::class.java)
}