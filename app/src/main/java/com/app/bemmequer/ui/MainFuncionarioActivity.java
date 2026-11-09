package com.app.bemmequer.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.app.bemmequer.R;
import com.app.bemmequer.data.UsuarioRepository;
import com.app.bemmequer.ui.funcionario.NovoRelatorioActivity;
import com.google.firebase.auth.FirebaseAuth;

//Tela inicial do funcionário: saudação, atalho para o relatório diário e Sair
public class MainFuncionarioActivity extends AppCompatActivity {
    //Firebase
    private FirebaseAuth mAuth;
    private Button btn_logout_funcionario;
    private TextView funcionarioOla;

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
        funcionarioOla = findViewById(R.id.funcionario_ola);
        btn_logout_funcionario = findViewById(R.id.btn_logout_funcionario);
        btn_logout_funcionario.setOnClickListener(v -> {
            mAuth.signOut(); //Desloga o usuário do Firebase Auth
            abrirTelaLogin();
        });

        //Atalho para escrever um relatório diário
        findViewById(R.id.card_relatorio_diario).setOnClickListener(v ->
                startActivity(new Intent(this, NovoRelatorioActivity.class)));
    }

    //Se não houver usuário logado, volta para a tela de Login; se houver, mostra o nome na saudação
    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() == null) {
            abrirTelaLogin();
            return;
        }
        carregarNome(mAuth.getCurrentUser().getUid());
    }

    //Método que busca o nome do funcionário (campo "nome" em usuarios/{uid}) para a saudação
    private void carregarNome(String uid) {
        new UsuarioRepository().buscarUsuario(uid, task -> {
            if (!task.isSuccessful() || isFinishing()) {
                return; //Sem o nome, a tela continua mostrando só "Olá!"
            }
            String nome = task.getResult().getString("nome");
            if (nome != null && !nome.isEmpty()) {
                funcionarioOla.setText(getString(R.string.olaNome, nome));
            }
        });
    }

    //Método que volta para a tela de Login limpando a pilha de telas
    private void abrirTelaLogin() {
        Intent intent = new Intent(this, loginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
