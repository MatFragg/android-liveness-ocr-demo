package com.matfragg.rekognition_demo

import android.app.Application
import android.util.Log
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class RekognitionApp : Application() {

    override fun onCreate() {
        super.onCreate()

        try {
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.configure(applicationContext)
            Log.i("Amplify", "Amplify inicializado correctamente")
        } catch (e: Exception) {
            Log.e("Amplify", "Error al inicializar Amplify", e)
        }
    }
}