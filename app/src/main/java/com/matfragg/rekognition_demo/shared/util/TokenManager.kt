// shared/util/TokenManager.kt
package com.matfragg.rekognition_demo.shared.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(private val prefs: SharedPreferences) {

    @Volatile
    private var memoryToken: String? = null

    // ELIMINADO: private val masterKey = ... (Lo movemos al Module)
    // ELIMINADO: private val prefs = EncryptedSharedPreferences.create... (Lo movemos al Module)

    @Synchronized
    fun saveToken(token: String) {
        memoryToken = token
        prefs.edit().putString("access_token", token).commit()
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

    fun saveExpiry(expiresInSeconds: Long) {
        val expiryTime = System.currentTimeMillis() + (expiresInSeconds * 1000)
        prefs.edit().putLong("expiry_time", expiryTime).commit()
    }

    fun isTokenExpired(token: String?): Boolean {
        if (token.isNullOrEmpty()) return true

        try {
            // Los JWT tienen 3 partes separadas por puntos. La segunda es el payload.
            val split = token.split(".")
            if (split.size < 2) return true

            // Decodificamos Base64 a String
            val payload = String(Base64.decode(split[1], Base64.URL_SAFE))

            // Convertimos a JSON para leer el campo "exp"
            val jsonObject = JSONObject(payload)
            val exp = jsonObject.optLong("exp")

            // Si no tiene expiración, asumimos que no expira (o que es inválido, mejor renovar)
            if (exp == 0L) return true

            // "exp" viene en segundos, System.currentTimeMillis en milisegundos.
            // Le restamos 10 segundos (buffer) para evitar problemas de latencia.
            val nowSeconds = System.currentTimeMillis() / 1000

            val isExpired = nowSeconds >= (exp - 10)

            if (isExpired) {
                Log.w("TOKEN_MANAGER", "⏰ El token ha expirado localmente.")
            }

            return isExpired

        } catch (e: Exception) {
            Log.e("TOKEN_MANAGER", "Error al parsear JWT: ${e.message}")
            return true // Si falla el parseo, mejor renovar por seguridad
        }
    }

    fun clearToken() {
        memoryToken = null
        prefs.edit().remove("access_token").commit()
        Log.d("TOKEN_MANAGER", "🗑️ Memoria de token limpiada")
    }
}