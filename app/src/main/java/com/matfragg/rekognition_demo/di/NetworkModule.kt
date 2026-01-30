package com.matfragg.rekognition_demo.di

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.matfragg.rekognition_demo.data.authentication.remote.AuthApi
import com.matfragg.rekognition_demo.data.authentication.remote.TokenAuthenticator
import com.matfragg.rekognition_demo.data.document_ocr.remote.DniApi
import com.matfragg.rekognition_demo.data.face_recognition.remote.RekognitionApi
import com.matfragg.rekognition_demo.data.liveness.remote.LivenessApi
import com.matfragg.rekognition_demo.domain.auth.repository.AuthRepository
import com.matfragg.rekognition_demo.shared.util.AuthInterceptor
import com.matfragg.rekognition_demo.shared.util.Constants
import com.matfragg.rekognition_demo.shared.util.TokenManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import dagger.Lazy

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder()
        .setLenient()
        .create()

    fun provideAuthInterceptor(
        tokenManager: TokenManager,
        authRepository: Lazy<AuthRepository> // Dagger inyectará esto automáticamente
    ): AuthInterceptor {
        return AuthInterceptor(tokenManager, authRepository)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        tokenAuthenticator: TokenAuthenticator,
        authInterceptor: AuthInterceptor // ✅ CORRECCIÓN 2: Inyectamos el interceptor ya creado arriba
    ): OkHttpClient {

        return try {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })

            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())

            return OkHttpClient.Builder()
                .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                .hostnameVerifier { _, _ -> true }
                .addInterceptor(authInterceptor) // ✅ Usamos la instancia inyectada, no creamos una nueva
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
                .authenticator(tokenAuthenticator)
                .connectTimeout(Constants.CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(Constants.READ_TIMEOUT, TimeUnit.MILLISECONDS)
                .build()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
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
        .baseUrl(Constants.ACJ_API)
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

    @Provides
    @Singleton
    fun provideAuthApi(@Named("compare") retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)
}