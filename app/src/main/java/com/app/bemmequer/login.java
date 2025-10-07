package com.app.bemmequer;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class login extends AppCompatActivity {
    // 1. DECLARAÇÃO DAS VARIÁVEIS (COMPONENTES DA TELA)
   private EditText editEmail;
   private EditText editTextSenha;
   private Button btn_login;
   private TextView textCadastre_se;
   private CheckBox checkbox;
   private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.tela_login);
        configurarInsets(); //Método para cuidas das barras do sistema

        inicializarComponentes();
        configurarListeners();
        }


    //MÉTODOS DE ORGANIZAÇÃO
    //Método que inicializa as variáveis, conectando com os IDs do layout XML
    private void inicializarComponentes() {
        mAuth = FirebaseAuth.getInstance();
        editEmail = findViewById(R.id.editEmail);
        editTextSenha = findViewById(R.id.editTextSenha);
        textCadastre_se = findViewById(R.id.textCadastre_se);
        btn_login = findViewById(R.id.btn_login);
        checkbox = findViewById(R.id.checkBox);
    }


    //Método que configura todas as ações de clique e interação do usuário
    private void configurarListeners(){
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validarDadosLogin(); //chama o método de validação
            }
        });


    //Listener para o link "Cadastre-se"
    textCadastre_se.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(login.this, cadastroUm.class);
            startActivity(intent);
        }
    });


    //Método para o CheckBox (mostrar senha)
        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    editTextSenha.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    editTextSenha.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });
    }


    //MÉTODOS DE AÇÃO
    //Método que valida os dados de login do usuário
    private void validarDadosLogin() {
        String email = editEmail.getText().toString().trim();
        String senha = editTextSenha.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Por favor, insira um e-mail válido.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(senha)){
            Toast.makeText(this, "Por favor, insira uma senha válida.", Toast.LENGTH_SHORT).show();
            return;
        }
        fazerLoginFirebase(email, senha);
    }

    //Método se comunica com o Firebase para autenticar o usuário
    private void fazerLoginFirebase(String email, String senha) {
        mAuth.signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            abrirTelaPrincipal();
                        } else {
                            String error = task.getException().getMessage();
                            Toast.makeText(login.this, "Erro: " + error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    //Método que abre a tela principal
    private void abrirTelaPrincipal() {
        Intent intent = new Intent(login.this, telaPrincipal.class);
        startActivity(intent);
        finish();
    }


    //Método auxiliar para configurar o espaçamento das barras do sistema
    private void configurarInsets(){
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
