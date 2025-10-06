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
   private EditText editEmail;
   private EditText editTextSenha;
   private Button btn_login;
   private TextView textCadastre_se;
   private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.tela_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Chamada das variáveis
        mAuth = FirebaseAuth.getInstance();
        editEmail = findViewById(R.id.editEmail);
        editTextSenha = findViewById(R.id.editTextSenha);
        textCadastre_se = findViewById(R.id.textCadastre_se);
        btn_login = findViewById(R.id.btn_login);

        //Função do Botão Login
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Capturo a entrada de texto do usuário
                String email = editEmail.getText().toString();
                String senha = editTextSenha.getText().toString();

                //Verificação se os campos estão vazios
                if (!TextUtils.isEmpty(email) || !TextUtils.isEmpty(senha)) {
                    mAuth.signInWithEmailAndPassword(email, senha) //Consulta dos dados que o usuário digitou no banco de dados
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) { //checa se está com dados de acesso certos e envia para a tela principal
                                    if (task.isSuccessful()) {
                                        abrirTelaPrincipal();
                                    } else {
                                        //caso esteja errado ele mostra a mensagem de erro
                                        String error = task.getException().getMessage();
                                        Toast.makeText(login.this, "" +error, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });



        //CheckBox(mostrar senha)
        CheckBox checkBox; // identificação da biblioteca e nome da variável
        EditText editTextSenha;
        checkBox = findViewById(R.id.checkBox);
        editTextSenha = findViewById(R.id.editTextSenha);

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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

    private void abrirTelaPrincipal() {
        Intent intent = new Intent(login.this, telaPrincipal.class);
        startActivity(intent);
        finish();
    }
}
