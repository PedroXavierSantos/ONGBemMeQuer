package com.app.bemmequer.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.app.bemmequer.R;
import com.app.bemmequer.ui.paciente.EmBreveFragment;
import com.app.bemmequer.ui.paciente.InicioFragment;
import com.app.bemmequer.ui.paciente.PerfilFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

//Moldura do lado do paciente: a barra inferior troca a aba (Fragment) exibida no meio da tela
public class mainPacienteActivity extends AppCompatActivity {
//Firebase
private FirebaseAuth mAuth;
private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_paciente);
        //Inicializa o Firebase Auth e as variáveis dos componentes
        mAuth = FirebaseAuth.getInstance();
        bottomNavigation = findViewById(R.id.bottom_navigation);

        //Configuração da barra de navegação inferior: cada toque troca a aba do meio
        bottomNavigation.setOnItemSelectedListener(item -> {
            mostrarAba(criarAba(item.getItemId()));
            return true;
        });

        //Só cria a aba Início na primeira vez. Quando o Android recria a tela
        //(ex.: girar o celular), ele mesmo restaura a aba que estava aberta.
        if (savedInstanceState == null) {
            mostrarAba(new InicioFragment());
        }

        configurarBotaoVoltar();

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

    //Método usado pelas abas para pedir a troca de aba (ex.: botão "Faça uma Doação" do Início)
    public void abrirAba(int idAba) {
        bottomNavigation.setSelectedItemId(idAba);
    }

    //Método que cria a aba correspondente ao item tocado na barra inferior
    private Fragment criarAba(int idAba) {
        if (idAba == R.id.nav_perfil) {
            return new PerfilFragment();
        }
        if (idAba == R.id.nav_agenda) {
            return EmBreveFragment.novaInstancia(R.string.abaAgenda, R.drawable.ic_calendar);
        }
        if (idAba == R.id.nav_noticias) {
            return EmBreveFragment.novaInstancia(R.string.abaNoticias, R.drawable.ic_noticias);
        }
        if (idAba == R.id.nav_doacoes) {
            return EmBreveFragment.novaInstancia(R.string.abaDoacoes, R.drawable.ic_doacao);
        }
        return new InicioFragment();
    }

    //Método que coloca a aba no espaço do meio da tela (frame_container)
    private void mostrarAba(Fragment aba) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_container, aba)
                .commit();
    }

    //Botão "voltar" do celular: em outra aba, volta para o Início; no Início, fecha o app
    private void configurarBotaoVoltar() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (bottomNavigation.getSelectedItemId() != R.id.nav_inicio) {
                    abrirAba(R.id.nav_inicio);
                } else {
                    finish();
                }
            }
        });
    }

    //Método que volta para a tela de Login limpando a pilha de telas
    private void abrirTelaLogin() {
        Intent intent = new Intent(mainPacienteActivity.this, loginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent); //Inicia a tela de Login
        finish(); //Finaliza a tela atual
    }

}
