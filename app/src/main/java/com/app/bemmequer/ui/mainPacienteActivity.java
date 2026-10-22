package com.app.bemmequer.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.app.bemmequer.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class mainPacienteActivity extends AppCompatActivity {
//Firebase
private FirebaseAuth mAuth;
private Button btn_logout_telaprincipal;
private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_paciente);
        //Inicializa o Firebase Auth e as variáveis dos componentes
        mAuth = FirebaseAuth.getInstance();
        btn_logout_telaprincipal = findViewById(R.id.btn_logout_telaprincipal);
        bottomNavigation = findViewById(R.id.bottom_navigation);

        //Configuração do botão de Logout
        btn_logout_telaprincipal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut(); //Desloga o usuário do Firebase Auth
                abrirTelaLogin();
            }
        });

        //Configuração da barra de navegação inferior
        bottomNavigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                return true;
            }
            //As outras abas ainda serão criadas (Agenda, Acompanhamento, Perfil)
            Toast.makeText(this, R.string.emBreve, Toast.LENGTH_SHORT).show();
            return false;
        });

        //Ajuste de padding automático para barras do sistema
        //A parte de baixo fica por conta da BottomNavigationView, que já trata a barra de navegação do sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tela_principal), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });
    }


    //Código para identificar se o usuário tem conta no banco de dados, caso não a tela ficará na de Login
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            abrirTelaLogin();
        }}

    //Método que volta para a tela de Login limpando a pilha de telas
    private void abrirTelaLogin() {
        Intent intent = new Intent(mainPacienteActivity.this, loginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent); //Inicia a tela de Login
        finish(); //Finaliza a tela atual
    }

}
