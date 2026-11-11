package com.app.bemmequer.ui.paciente;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.bemmequer.R;
import com.app.bemmequer.auth.AuthManager;
import com.app.bemmequer.data.Relatorio;
import com.app.bemmequer.data.RelatorioRepository;
import com.app.bemmequer.ui.CompartilhadorDeRelatorio;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

//Lista dos relatórios diários do paciente, vista pela família ("Acompanhe a evolução")
public class RelatoriosActivity extends AppCompatActivity {
    //DECLARAÇÃO DAS VARIÁVEIS (COMPONENTES DA TELA)
    private RecyclerView listaRelatorios;
    private ProgressBar carregando;
    private TextView mensagem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_relatorios);
        configurarInsets();
        findViewById(R.id.btn_voltar).setOnClickListener(v -> finish()); //Fecha esta tela e volta para a anterior

        listaRelatorios = findViewById(R.id.lista_relatorios);
        carregando = findViewById(R.id.carregando_relatorios);
        mensagem = findViewById(R.id.mensagem_relatorios);
        listaRelatorios.setLayoutManager(new LinearLayoutManager(this));

        carregarRelatorios();
    }

    //Método que busca no Firestore os relatórios em que esta família é o pacienteUid
    private void carregarRelatorios() {
        String uid = new AuthManager().getUid();
        if (uid == null) {
            finish();
            return;
        }
        new RelatorioRepository().listarRelatoriosDoPaciente(uid, task -> {
            if (isFinishing()) {
                return;
            }
            carregando.setVisibility(View.GONE);
            if (!task.isSuccessful()) {
                mostrarMensagem(R.string.erroCarregarRelatorios);
                return;
            }
            List<Relatorio> relatorios = new ArrayList<>();
            for (DocumentSnapshot documento : task.getResult().getDocuments()) {
                relatorios.add(Relatorio.deDocumento(documento));
            }
            if (relatorios.isEmpty()) {
                mostrarMensagem(R.string.semRelatorios);
                return;
            }
            //O terceiro parâmetro é o que acontece ao tocar em "Compartilhar PDF" num cartão
            listaRelatorios.setAdapter(new RelatorioAdapter(relatorios, false,
                    relatorio -> CompartilhadorDeRelatorio.compartilhar(this, relatorio)));
            listaRelatorios.setVisibility(View.VISIBLE);
        });
    }

    //Mostra uma mensagem no lugar da lista (lista vazia ou erro)
    private void mostrarMensagem(int texto) {
        mensagem.setText(texto);
        mensagem.setVisibility(View.VISIBLE);
    }

    //Método auxiliar para configurar o espaçamento das barras do sistema
    private void configurarInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.relatorios), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
