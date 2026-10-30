package com.app.bemmequer;

import android.app.Application;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class BemMeQuerApp extends Application {
    //Endereço do computador visto de dentro do emulador do Android
    private static final String HOST_EMULADOR = "10.0.2.2";
    private static final int PORTA_AUTH = 9099;
    private static final int PORTA_FIRESTORE = 8080;

    @Override
    public void onCreate() {
        super.onCreate();
        //Em builds de teste, usa os emuladores locais do Firebase em vez do projeto real.
        //Precisa rodar antes de qualquer uso do Firebase, por isso fica na classe Application.
        if (BuildConfig.USAR_EMULADOR_FIREBASE) {
            FirebaseAuth.getInstance().useEmulator(HOST_EMULADOR, PORTA_AUTH);
            FirebaseFirestore.getInstance().useEmulator(HOST_EMULADOR, PORTA_FIRESTORE);
        }
    }
}
