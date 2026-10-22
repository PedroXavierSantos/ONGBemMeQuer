package com.app.bemmequer.data;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class UsuarioRepository {
    //Nome da coleção e tipos de usuário salvos no Firestore
    public static final String COLECAO_USUARIOS = "usuarios";
    public static final String CAMPO_TIPO = "tipo";
    public static final String TIPO_PACIENTE = "paciente";
    public static final String TIPO_FUNCIONARIO = "funcionario";

    //Atributos da classe
    private final FirebaseFirestore db;

    // Inicializa o Firestore
    public UsuarioRepository() {
        db = FirebaseFirestore.getInstance();
    }



    //Método que salva os dados do usuário no documento usuarios/{uid}
    public void salvarUsuario(String uid, Map<String, Object> dados, OnCompleteListener<Void> listener) {
        db.collection(COLECAO_USUARIOS).document(uid)
                .set(dados)
                .addOnCompleteListener(listener);
    }

    //Método que busca os dados do usuário (usado para saber se é paciente ou funcionário)
    public void buscarUsuario(String uid, OnCompleteListener<DocumentSnapshot> listener) {
        db.collection(COLECAO_USUARIOS).document(uid)
                .get()
                .addOnCompleteListener(listener);
    }
}
