package com.matfragg.rekognition_demo.di

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.matfragg.rekognition_demo.data.authentication.remote.AuthApi
import com.matfragg.rekognition_demo.data.authentication.repository.AuthRepositoryImpl
import com.matfragg.rekognition_demo.domain.auth.repository.AuthRepository
import com.matfragg.rekognition_demo.shared.util.TokenManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.IOException
import java.security.GeneralSecurityException
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    private const val PREFS_FILE_NAME = "auth_prefs" // Usamos el nombre que tenías originalmente

    @Provides
    @Singleton
    fun provideAuthRepository(api: AuthApi): AuthRepository = AuthRepositoryImpl(api)

    @Provides
    @Singleton
    fun provideTokenManager(@ApplicationContext context: Context): TokenManager {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val sharedPrefs = try {
            createEncryptedPrefs(context, masterKey)
        } catch (e: Exception) {
            // Manejo de errores de desencriptación (reinstalación o corrupción)
            if (e is GeneralSecurityException || e is IOException) {
                // Borrar el archivo corrupto
                context.deleteSharedPreferences(PREFS_FILE_NAME)

                // Reintentar creación desde cero
                createEncryptedPrefs(context, masterKey)
            } else {
                throw e
            }
        }

        // AHORA SÍ FUNCIONA: TokenManager espera SharedPreferences
        return TokenManager(sharedPrefs)
    }

    private fun createEncryptedPrefs(context: Context, masterKey: MasterKey): SharedPreferences {
        return EncryptedSharedPreferences.create(
            context,
            PREFS_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
}
