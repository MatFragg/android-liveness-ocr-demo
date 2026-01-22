// shared/util/TokenManager.kt
package com.matfragg.rekognition_demo.shared.util

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(@ApplicationContext context: Context) {

    @Volatile // ← Importante para thread-safety
    private var memoryToken: String? = null

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context, "auth_prefs", masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    @Synchronized // ← Prevenir acceso concurrente
    fun saveToken(token: String) {
        memoryToken = token
        prefs.edit().putString("access_token", token).apply()
        Log.d("TOKEN_MANAGER", "Token guardado: ${token.take(20)}...")
    }

    @Synchronized
    fun getToken(): String? {
        if (memoryToken != null) {
            Log.d("TOKEN_MANAGER", "Token desde RAM: ${memoryToken?.take(20)}...")
            return memoryToken
        }
        memoryToken = prefs.getString("access_token", null)
        Log.d("TOKEN_MANAGER", "Token desde disco: ${memoryToken?.take(20)}...")
        return memoryToken
    }

    fun saveExpiry(expiresInMs: Long) {
        val expiryTime = System.currentTimeMillis() + expiresInMs
        prefs.edit().putLong("expiry_time", expiryTime).apply()
    }

    fun isTokenExpired(): Boolean {
        val expiryTime = prefs.getLong("expiry_time", 0)
        return System.currentTimeMillis() >= (expiryTime - 30000)
    }

    fun clearToken() {
        memoryToken = null
        prefs.edit().remove("access_token").apply()
    }
}