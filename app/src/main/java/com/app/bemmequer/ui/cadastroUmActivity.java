package com.app.bemmequer.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.app.bemmequer.R;

public class cadastroUmActivity extends AppCompatActivity {
    //Chaves usadas para enviar os dados desta tela para a segunda tela do cadastro
    public static final String EXTRA_NOME_RESPONSAVEL = "nomeResponsavel";
    public static final String EXTRA_TELEFONE = "telefone";
    public static final String EXTRA_EMAIL = "email";
    public static final String EXTRA_SENHA = "senha";

    //Tamanho mínimo de senha aceito pelo Firebase Auth
    private static final int TAMANHO_MINIMO_SENHA = 6;
    //Telefone com DDD: 10 dígitos (fixo) ou 11 (celular)
    private static final int DIGITOS_MINIMOS_TELEFONE = 10;

    //DECLARAÇÃO DAS VARIÁVEIS (COMPONENTES DA TELA)
    private Button btn_proximo;
    private EditText editNomePessoa, editNumeroTelefone, editEmail, editSenha, confirmarSenha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.tela_cadastro_um);
        configurarInsets(); //Método para cuidar das barras do sistema

        inicializarComponentes();
        configurarListeners();
    }


    //MÉTODOS DE ORGANIZAÇÃO
    //Método que inicializa as variáveis, conectando com os IDs do layout XML
    private void inicializarComponentes() {
        btn_proximo = findViewById(R.id.btn_proximo);
        editNomePessoa = findViewById(R.id.editNomePessoa);
        editNumeroTelefone = findViewById(R.id.editNumeroTelefone);
        editEmail = findViewById(R.id.editEmail);
        editSenha = findViewById(R.id.editSenha);
        confirmarSenha = findViewById(R.id.confirmarSenha);
    }

    //Método que configura todas as ações de clique e interação do usuário
    private void configurarListeners(){
        btn_proximo.setOnClickListener(view -> validarDados());

    }


    //MÉTODOS DE AÇÃO
    //Método que valida os campos e, se estiver tudo certo, envia os dados para a segunda tela
    private void validarDados() {
        String nome = editNomePessoa.getText().toString().trim();
        String telefone = editNumeroTelefone.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String senha = editSenha.getText().toString(); //A senha não usa trim(), espaços fazem parte dela
        String senhaConfirmada = confirmarSenha.getText().toString();

        if (TextUtils.isEmpty(nome)) {
            editNomePessoa.setError(getString(R.string.erroCampoObrigatorio));
            editNomePessoa.requestFocus();
            return;
        }
        if (telefone.replaceAll("\\D", "").length() < DIGITOS_MINIMOS_TELEFONE) {
            editNumeroTelefone.setError(getString(R.string.erroTelefoneInvalido));
            editNumeroTelefone.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.setError(getString(R.string.erroEmailInvalido));
            editEmail.requestFocus();
            return;
        }
        if (senha.length() < TAMANHO_MINIMO_SENHA) {
            editSenha.setError(getString(R.string.erroSenhaCurta));
            editSenha.requestFocus();
            return;
        }
        if (!senha.equals(senhaConfirmada)) {
            confirmarSenha.setError(getString(R.string.erroSenhasDiferentes));
            confirmarSenha.requestFocus();
            return;
        }

        Intent intent = new Intent(this, cadastroDoisActivity.class);
        intent.putExtra(EXTRA_NOME_RESPONSAVEL, nome);
        intent.putExtra(EXTRA_TELEFONE, telefone);
        intent.putExtra(EXTRA_EMAIL, email);
        intent.putExtra(EXTRA_SENHA, senha);
        startActivity(intent);
    }

    //Método auxiliar para configurar o espaçamento das barras do sistema
    private void configurarInsets(){
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.cadastroUm), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

}
