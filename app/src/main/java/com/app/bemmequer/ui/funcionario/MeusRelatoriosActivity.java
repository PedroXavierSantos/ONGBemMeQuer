package com.app.bemmequer.ui.funcionario;

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
import com.app.bemmequer.ui.paciente.RelatorioAdapter;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//"Meus relatórios": lista dos relatórios diários que o funcionário logado já enviou (botão Relatórios do rodapé)
public class MeusRelatoriosActivity extends AppCompatActivity {
    //DECLARAÇÃO DAS VARIÁVEIS (COMPONENTES DA TELA)
    private RecyclerView listaRelatorios;
    private ProgressBar carregando;
    private TextView mensagem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_meus_relatorios);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.meus_relatorios), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        listaRelatorios = findViewById(R.id.lista_meus_relatorios);
        carregando = findViewById(R.id.carregando_meus_relatorios);
        mensagem = findViewById(R.id.mensagem_meus_relatorios);
        listaRelatorios.setLayoutManager(new LinearLayoutManager(this));
        findViewById(R.id.btn_voltar).setOnClickListener(v -> finish()); //Fecha esta tela e volta para a anterior

        carregarRelatorios();
    }

    //Método que busca no Firestore os relatórios em que este funcionário é o autor
    private void carregarRelatorios() {
        String uid = new AuthManager().getUid();
        if (uid == null) {
            finish();
            return;
        }
        new RelatorioRepository().listarRelatoriosDoFuncionario(uid, task -> {
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
                mostrarMensagem(R.string.semMeusRelatorios);
                return;
            }
            //Do mais recente para o mais antigo (relatório sem data vai para o fim)
            relatorios.sort((a, b) -> dataOuZero(b).compareTo(dataOuZero(a)));
            listaRelatorios.setAdapter(new RelatorioAdapter(relatorios, true));
            listaRelatorios.setVisibility(View.VISIBLE);
        });
    }

    private static Date dataOuZero(Relatorio relatorio) {
        return relatorio.getData() != null ? relatorio.getData() : new Date(0);
    }

    //Mostra uma mensagem no lugar da lista (lista vazia ou erro)
    private void mostrarMensagem(int texto) {
        mensagem.setText(texto);
        mensagem.setVisibility(View.VISIBLE);
    }
}
