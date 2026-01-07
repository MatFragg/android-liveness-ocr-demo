package com.matfragg.rekognition_demo;

import android.app.Application;
import android.util.Log;
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin;
import com.amplifyframework.core.Amplify;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        try {
            // Inicializamos el plugin de autenticación
            Amplify.addPlugin(new AWSCognitoAuthPlugin());
            // Leemos la configuración del archivo JSON
            Amplify.configure(getApplicationContext());
            Log.i("Amplify", "Amplify inicializado correctamente");
        } catch (Exception e) {
            Log.e("Amplify", "Error fatal al inicializar Amplify", e);
        }
    }
}