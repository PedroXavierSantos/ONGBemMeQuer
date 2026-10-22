package com.app.bemmequer.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.app.bemmequer.R;
import com.app.bemmequer.auth.AuthManager;
import com.app.bemmequer.data.UsuarioRepository;




public class loginActivity extends AppCompatActivity {
    //DECLARAÇÃO DAS VARIÁVEIS (COMPONENTES DA TELA)
   private EditText editEmail;
   private EditText editTextSenha;
   private Button btn_login;
   private TextView textCadastre_se;
   private CheckBox checkbox;
   private AuthManager authManager;
   private UsuarioRepository usuarioRepository;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.tela_login);
        configurarInsets(); //Método para cuidas das barras do sistema

        inicializarComponentes();
        configurarListeners();
        }



    //Método que inicializa as variáveis, conectando com os IDs do layout XML
    private void inicializarComponentes() {
        editEmail = findViewById(R.id.editEmail);
        editTextSenha = findViewById(R.id.editTextSenha);
        textCadastre_se = findViewById(R.id.textCadastre_se);
        btn_login = findViewById(R.id.btn_login);
        checkbox = findViewById(R.id.checkBox);
        authManager = new AuthManager();
        usuarioRepository = new UsuarioRepository();
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
            Intent intent = new Intent(loginActivity.this, cadastroUmActivity.class);
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


    //Se o usuário já estiver logado, pula a tela de login e vai direto para a tela principal
    @Override
    protected void onStart() {
        super.onStart();
        String uid = authManager.getUid();
        if (uid != null) {
            direcionarUsuario(uid);
        }
    }


    //MÉTODOS DE AÇÃO
    //Método que valida os dados de login do usuário
    private void validarDadosLogin() {
        String email = editEmail.getText().toString().trim();
        String senha = editTextSenha.getText().toString(); //A senha não usa trim(), espaços fazem parte dela

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, R.string.erroEmailInvalido, Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(senha)){
            Toast.makeText(this, R.string.erroLoginSenha, Toast.LENGTH_SHORT).show();
            return;
        }
        fazerLoginFirebase(email, senha);
    }

    //Método se comunica com o Firebase para autenticar o usuário
    private void fazerLoginFirebase(String email, String senha) {
        btn_login.setEnabled(false); //Evita cliques repetidos enquanto o login é feito
        authManager.login(email, senha, task -> {
            if (task.isSuccessful()) {
                direcionarUsuario(authManager.getUid());
            } else {
                btn_login.setEnabled(true);
                Toast.makeText(loginActivity.this, AuthManager.mensagemErro(task.getException()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Método que consulta o tipo do usuário no Firestore e abre a tela de funcionário ou de paciente
    private void direcionarUsuario(String uid) {
        btn_login.setEnabled(false);
        usuarioRepository.buscarUsuario(uid, task -> {
            if (!task.isSuccessful()) {
                //Sem o perfil não sabemos para qual tela ir, então desloga para tentar de novo
                authManager.logout();
                btn_login.setEnabled(true);
                Toast.makeText(loginActivity.this, R.string.erroCarregarPerfil, Toast.LENGTH_SHORT).show();
                return;
            }
            String tipo = task.getResult().getString(UsuarioRepository.CAMPO_TIPO);
            if (UsuarioRepository.TIPO_FUNCIONARIO.equals(tipo)) {
                abrirTela(MainFuncionarioActivity.class);
            } else {
                abrirTela(mainPacienteActivity.class);
            }
        });
    }

    //Método que abre a tela principal e fecha a tela de login
    private void abrirTela(Class<?> tela) {
        Intent intent = new Intent(loginActivity.this, tela);
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
