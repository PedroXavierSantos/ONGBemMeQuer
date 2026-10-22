package com.app.bemmequer.auth;


import androidx.annotation.StringRes;

import com.app.bemmequer.R;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

public class AuthManager {
    //Atributos da classe
    private FirebaseAuth mAuth;

    // Inicializa o Firebase Auth
    public AuthManager() {
    mAuth = FirebaseAuth.getInstance();
    }



    //Método que cria um usuário com email e senha
    public void createUser(String email, String password, OnCompleteListener<AuthResult> listener){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(listener);
    }

    //Método responsável por fazer login com email e senha
    public void login(String email, String password, OnCompleteListener<AuthResult> listener){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(listener);
    }

    //Método que desloga o usuário atual
    public void logout() {
        mAuth.signOut();
    }

    //Método que exclui o usuário atual (usado para desfazer um cadastro que não foi concluído)
    public void excluirUsuarioAtual() {
        FirebaseUser usuario = mAuth.getCurrentUser();
        if (usuario != null) {
            usuario.delete();
        }
    }

    // UID
    public String getUid() {
        if (mAuth.getCurrentUser() != null) {
            return mAuth.getCurrentUser().getUid();
        }
        return null;
    }

    //Método que traduz os erros do Firebase para mensagens em português (strings.xml)
    @StringRes
    public static int mensagemErro(Exception erro) {
        if (erro instanceof FirebaseAuthWeakPasswordException) {
            return R.string.erroSenhaFraca;
        }
        if (erro instanceof FirebaseAuthInvalidCredentialsException
                || erro instanceof FirebaseAuthInvalidUserException) {
            return R.string.erroCredenciais;
        }
        if (erro instanceof FirebaseAuthUserCollisionException) {
            return R.string.erroEmailEmUso;
        }
        if (erro instanceof FirebaseNetworkException) {
            return R.string.erroSemConexao;
        }
        if (erro instanceof FirebaseTooManyRequestsException) {
            return R.string.erroMuitasTentativas;
        }
        return R.string.erroGenerico;
    }
}
