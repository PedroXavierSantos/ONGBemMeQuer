package com.app.bemmequer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class cadastroUmActivity extends AppCompatActivity {
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
        btn_proximo.setOnClickListener(view -> startActivity(new Intent(this, cadastroDoisActivity.class)));

    }


    //MÉTODOS DE AÇÃO
    //Método auxiliar para configurar o espaçamento das barras do sistema
    private void configurarInsets(){
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.cadastroUm), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

}