package com.app.bemmequer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class telaPrincipal extends AppCompatActivity {
//Firebase
private FirebaseAuth mAuth;
private Button btn_logout_telaprincipal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.tela_principal);
        //Inicializa o Firebase Auth e a variável do botão de Logout
        mAuth = FirebaseAuth.getInstance();
        btn_logout_telaprincipal = findViewById(R.id.btn_logout_telaprincipal);

        //Configuração do botão de Logout
        btn_logout_telaprincipal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut(); //Desloga o usuário do Firebase Auth
                Intent intent = new Intent(telaPrincipal.this, login.class); //Retorna para a tela de Login
                startActivity(intent); //Inicia a tela de Login
                finish(); //Finaliza a tela atual
            }
        });

        //Ajuste de padding automático para barras do sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tela_principal), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }


    //Código para identificar se o usuário tem conta no banco de dados, caso não a tela ficará na de Login
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(telaPrincipal.this, login.class);
            startActivity(intent);
            finish();
        }}

}
