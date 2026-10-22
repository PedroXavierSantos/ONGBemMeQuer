package com.app.bemmequer.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.app.bemmequer.R;
import com.google.firebase.auth.FirebaseAuth;

public class MainFuncionarioActivity extends AppCompatActivity {
    //Firebase
    private FirebaseAuth mAuth;
    private Button btn_logout_funcionario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_funcionario);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        btn_logout_funcionario = findViewById(R.id.btn_logout_funcionario);
        btn_logout_funcionario.setOnClickListener(v -> {
            mAuth.signOut(); //Desloga o usuário do Firebase Auth
            abrirTelaLogin();
        });
    }

    //Se não houver usuário logado, volta para a tela de Login
    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() == null) {
            abrirTelaLogin();
        }
    }

    //Método que volta para a tela de Login limpando a pilha de telas
    private void abrirTelaLogin() {
        Intent intent = new Intent(this, loginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
