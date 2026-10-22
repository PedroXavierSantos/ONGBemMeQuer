package com.app.bemmequer.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.app.bemmequer.R;
import com.app.bemmequer.auth.AuthManager;
import com.app.bemmequer.data.UsuarioRepository;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Map;

public class cadastroDoisActivity extends AppCompatActivity {
    //DECLARAÇÃO DAS VARIÁVEIS (COMPONENTES DA TELA)
    private Button btn_finalizar;
    private EditText editNamePacient, editDescriptionPacient, editIntentions;
    private AuthManager authManager;
    private UsuarioRepository usuarioRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.tela_cadastro_dois);
        configurarInsets(); //Método para cuidar das barras do sistema

        inicializarComponentes();
        configurarListeners();
    }


    //MÉTODOS DE ORGANIZAÇÃO
    //Método que inicializa as variáveis, conectando com os IDs do layout XML
    private void inicializarComponentes() {
        btn_finalizar = findViewById(R.id.btn_finalizar);
        editNamePacient = findViewById(R.id.editNamePacient);
        editDescriptionPacient = findViewById(R.id.editDescriptionPacient);
        editIntentions = findViewById(R.id.editIntentions);
        authManager = new AuthManager();
        usuarioRepository = new UsuarioRepository();
    }

    //Método que configura todas as ações de clique e interação do usuário
    private void configurarListeners() {
        btn_finalizar.setOnClickListener(view -> validarDados());
    }


    //MÉTODOS DE AÇÃO
    //Método que valida os campos desta tela antes de criar a conta
    private void validarDados() {
        String nomePaciente = editNamePacient.getText().toString().trim();
        if (TextUtils.isEmpty(nomePaciente)) {
            editNamePacient.setError(getString(R.string.erroCampoObrigatorio));
            editNamePacient.requestFocus();
            return;
        }
        criarConta(nomePaciente);
    }

    //Método que cria a conta no Firebase Auth e depois salva os dados do cadastro no Firestore
    private void criarConta(String nomePaciente) {
        Intent dadosTelaUm = getIntent();
        String email = dadosTelaUm.getStringExtra(cadastroUmActivity.EXTRA_EMAIL);
        String senha = dadosTelaUm.getStringExtra(cadastroUmActivity.EXTRA_SENHA);

        btn_finalizar.setEnabled(false); //Evita criar a conta duas vezes
        authManager.createUser(email, senha, task -> {
            if (!task.isSuccessful()) {
                btn_finalizar.setEnabled(true);
                Toast.makeText(this, AuthManager.mensagemErro(task.getException()), Toast.LENGTH_LONG).show();
                return;
            }

            Map<String, Object> dados = new HashMap<>();
            dados.put(UsuarioRepository.CAMPO_TIPO, UsuarioRepository.TIPO_PACIENTE);
            dados.put("nomeResponsavel", dadosTelaUm.getStringExtra(cadastroUmActivity.EXTRA_NOME_RESPONSAVEL));
            dados.put("telefone", dadosTelaUm.getStringExtra(cadastroUmActivity.EXTRA_TELEFONE));
            dados.put("email", email);
            dados.put("nomePaciente", nomePaciente);
            dados.put("descricaoPaciente", editDescriptionPacient.getText().toString().trim());
            dados.put("intencoes", editIntentions.getText().toString().trim());
            dados.put("criadoEm", FieldValue.serverTimestamp());

            usuarioRepository.salvarUsuario(authManager.getUid(), dados, salvar -> {
                if (salvar.isSuccessful()) {
                    Toast.makeText(this, R.string.cadastroConcluido, Toast.LENGTH_SHORT).show();
                    abrirTelaPrincipal();
                } else {
                    //Desfaz a conta criada para que o usuário possa tentar de novo com o mesmo e-mail
                    authManager.excluirUsuarioAtual();
                    btn_finalizar.setEnabled(true);
                    Toast.makeText(this, R.string.erroSalvarCadastro, Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    //Método que abre a tela principal e limpa as telas de login/cadastro da pilha
    private void abrirTelaPrincipal() {
        Intent intent = new Intent(this, mainPacienteActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    //Método auxiliar para configurar o espaçamento das barras do sistema
    private void configurarInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.cadastroDois), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
