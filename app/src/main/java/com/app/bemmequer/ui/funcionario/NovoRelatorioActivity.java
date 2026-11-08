package com.app.bemmequer.ui.funcionario;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.app.bemmequer.R;
import com.app.bemmequer.auth.AuthManager;
import com.app.bemmequer.data.Relatorio;
import com.app.bemmequer.data.RelatorioRepository;
import com.app.bemmequer.data.UsuarioRepository;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

//Formulário em que o funcionário escreve o relatório diário de um paciente
public class NovoRelatorioActivity extends AppCompatActivity {
    //Formato da data mostrada no campo (ex.: 30/10/2026)
    private static final SimpleDateFormat FORMATO_DATA = new SimpleDateFormat("dd/MM/yyyy", new Locale("pt", "BR"));

    //DECLARAÇÃO DAS VARIÁVEIS (COMPONENTES DA TELA)
    private TextView nomeFuncionarioTela;
    private TextInputLayout layoutPaciente, layoutRelato, layoutLocal;
    private MaterialAutoCompleteTextView campoPaciente;
    private TextInputEditText campoRelato, campoData, campoLocal;
    private Button btnFinalizar;

    private AuthManager authManager;
    private UsuarioRepository usuarioRepository;
    private RelatorioRepository relatorioRepository;

    //Estado da tela
    private final List<DocumentSnapshot> pacientes = new ArrayList<>(); //mesma ordem do menu suspenso
    private DocumentSnapshot pacienteEscolhido;
    private String nomeFuncionario;
    private Date dataEscolhida;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_novo_relatorio);
        configurarInsets();

        inicializarComponentes();
        configurarListeners();
        definirData(hojeMeiaNoite());
        carregarFuncionario();
        carregarPacientes();
    }


    //MÉTODOS DE ORGANIZAÇÃO
    //Método que inicializa as variáveis, conectando com os IDs do layout XML
    private void inicializarComponentes() {
        nomeFuncionarioTela = findViewById(R.id.relatorio_nome_funcionario);
        layoutPaciente = findViewById(R.id.layout_paciente);
        layoutRelato = findViewById(R.id.layout_relato);
        layoutLocal = findViewById(R.id.layout_local);
        campoPaciente = findViewById(R.id.campo_paciente);
        campoRelato = findViewById(R.id.campo_relato);
        campoData = findViewById(R.id.campo_data);
        campoLocal = findViewById(R.id.campo_local);
        btnFinalizar = findViewById(R.id.btn_finalizar_relatorio);
        authManager = new AuthManager();
        usuarioRepository = new UsuarioRepository();
        relatorioRepository = new RelatorioRepository();
    }

    //Método que configura todas as ações de clique e interação do usuário
    private void configurarListeners() {
        //Guarda qual paciente foi escolhido no menu suspenso
        campoPaciente.setOnItemClickListener((parent, view, posicao, id) -> {
            pacienteEscolhido = pacientes.get(posicao);
            layoutPaciente.setError(null);
        });
        campoData.setOnClickListener(v -> abrirCalendario());
        btnFinalizar.setOnClickListener(v -> validarEEnviar());
    }


    //MÉTODOS DE AÇÃO
    //Método que busca o nome do funcionário logado (vai no cabeçalho e no relatório)
    private void carregarFuncionario() {
        usuarioRepository.buscarUsuario(authManager.getUid(), task -> {
            if (task.isSuccessful()) {
                nomeFuncionario = task.getResult().getString("nome");
            }
            nomeFuncionarioTela.setText(TextUtils.isEmpty(nomeFuncionario)
                    ? getString(R.string.olaSemNome) : nomeFuncionario);
        });
    }

    //Método que busca os pacientes cadastrados e monta o menu suspenso em ordem alfabética
    private void carregarPacientes() {
        usuarioRepository.listarPacientes(task -> {
            if (!task.isSuccessful()) {
                layoutPaciente.setError(getString(R.string.erroCarregarPacientes));
                return;
            }
            pacientes.clear();
            pacientes.addAll(task.getResult().getDocuments());
            Collections.sort(pacientes, (a, b) ->
                    textoOuVazio(a.getString("nomePaciente")).compareToIgnoreCase(textoOuVazio(b.getString("nomePaciente"))));

            List<String> nomes = new ArrayList<>();
            for (DocumentSnapshot paciente : pacientes) {
                nomes.add(getString(R.string.pacienteComResponsavel,
                        textoOuVazio(paciente.getString("nomePaciente")),
                        textoOuVazio(paciente.getString("nomeResponsavel"))));
            }
            campoPaciente.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, nomes));
            if (pacientes.isEmpty()) {
                layoutPaciente.setError(getString(R.string.semPacientes));
            }
        });
    }

    //Método que abre o calendário do Material Design para escolher a data do atendimento
    private void abrirCalendario() {
        MaterialDatePicker<Long> calendario = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.campoData)
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();
        calendario.addOnPositiveButtonClickListener(selecao -> definirData(converterDataDoCalendario(selecao)));
        calendario.show(getSupportFragmentManager(), "calendario");
    }

    //Método que valida os campos e envia o relatório
    private void validarEEnviar() {
        String relato = textoDo(campoRelato);
        String local = textoDo(campoLocal);
        boolean valido = true;

        if (pacienteEscolhido == null) {
            layoutPaciente.setError(getString(R.string.erroEscolherPaciente));
            valido = false;
        }
        if (relato.isEmpty()) {
            layoutRelato.setError(getString(R.string.erroCampoObrigatorio));
            valido = false;
        } else {
            layoutRelato.setError(null);
        }
        if (local.isEmpty()) {
            layoutLocal.setError(getString(R.string.erroCampoObrigatorio));
            valido = false;
        } else {
            layoutLocal.setError(null);
        }
        if (!valido) {
            return;
        }

        Relatorio relatorio = new Relatorio(
                pacienteEscolhido.getId(),
                textoOuVazio(pacienteEscolhido.getString("nomePaciente")),
                authManager.getUid(),
                TextUtils.isEmpty(nomeFuncionario) ? getString(R.string.olaSemNome) : nomeFuncionario,
                relato,
                dataEscolhida,
                local);

        btnFinalizar.setEnabled(false); //Evita enviar o mesmo relatório duas vezes
        relatorioRepository.salvarRelatorio(relatorio, task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, R.string.relatorioEnviado, Toast.LENGTH_SHORT).show();
                finish(); //Volta para a tela inicial do funcionário
            } else {
                btnFinalizar.setEnabled(true);
                Toast.makeText(this, R.string.erroEnviarRelatorio, Toast.LENGTH_LONG).show();
            }
        });
    }


    //MÉTODOS AUXILIARES
    //Guarda a data escolhida e mostra no campo
    private void definirData(Date data) {
        dataEscolhida = data;
        campoData.setText(FORMATO_DATA.format(data));
    }

    //O calendário devolve o dia em UTC; aqui ele vira o mesmo dia à meia-noite no horário do celular.
    //Sem isso, no Brasil (UTC-3) o dia escolhido apareceria como o dia anterior.
    private Date converterDataDoCalendario(long selecaoUtc) {
        Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utc.setTimeInMillis(selecaoUtc);
        Calendar local = Calendar.getInstance();
        local.clear();
        local.set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH));
        return local.getTime();
    }

    private Date hojeMeiaNoite() {
        Calendar hoje = Calendar.getInstance();
        hoje.set(Calendar.HOUR_OF_DAY, 0);
        hoje.set(Calendar.MINUTE, 0);
        hoje.set(Calendar.SECOND, 0);
        hoje.set(Calendar.MILLISECOND, 0);
        return hoje.getTime();
    }

    private String textoDo(TextInputEditText campo) {
        return campo.getText() == null ? "" : campo.getText().toString().trim();
    }

    private String textoOuVazio(String valor) {
        return valor == null ? "" : valor;
    }

    //Método auxiliar para configurar o espaçamento das barras do sistema
    private void configurarInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.novo_relatorio), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
