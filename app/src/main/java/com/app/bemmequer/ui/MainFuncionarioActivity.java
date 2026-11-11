package com.app.bemmequer.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.ColorRes;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.app.bemmequer.R;
import com.app.bemmequer.data.PontoDoDia;
import com.app.bemmequer.data.PontoRepository;
import com.app.bemmequer.data.TipoPonto;
import com.app.bemmequer.data.UsuarioRepository;
import com.app.bemmequer.ui.funcionario.MeusRelatoriosActivity;
import com.app.bemmequer.ui.funcionario.NovoRelatorioActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

//Tela inicial do funcionário (protótipo "Aba inicial" do colaborador):
//saudação, Status de Hoje, Últimos 15 dias, Controle de Tarefas (ponto), Relatório Diário, Relatórios e Sair
public class MainFuncionarioActivity extends AppCompatActivity {
    //Quantos dias anteriores aparecem na faixa "Últimos 15 dias"
    private static final int DIAS_ANTERIORES = 15;
    private static final Locale PT_BR = new Locale("pt", "BR");

    //Formatos de data e hora usados na tela
    private final SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm", PT_BR);
    private final SimpleDateFormat formatoDataHoje = new SimpleDateFormat("EEEE, d 'de' MMMM", PT_BR);
    private final SimpleDateFormat formatoDiaSemana = new SimpleDateFormat("EEE", PT_BR);
    private final SimpleDateFormat formatoDiaMes = new SimpleDateFormat("dd/MM", PT_BR);

    //Firebase
    private FirebaseAuth mAuth;
    private PontoRepository pontoRepository;

    //DECLARAÇÃO DAS VARIÁVEIS (COMPONENTES DA TELA)
    private TextView funcionarioOla;
    private TextView statusHojeData;
    private TextView statusHojeSituacao;
    private LinearProgressIndicator barraExpediente;
    private LinearLayout listaDias;
    private Tarefa[] tarefas;

    //Estado do ponto
    private PontoDoDia pontoDeHoje = new PontoDoDia();
    private String chaveDeHoje;
    private boolean registrando = false; //Evita dois toques seguidos gravarem duas vezes

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
        pontoRepository = new PontoRepository();
        funcionarioOla = findViewById(R.id.funcionario_ola);
        statusHojeData = findViewById(R.id.status_hoje_data);
        statusHojeSituacao = findViewById(R.id.status_hoje_situacao);
        barraExpediente = findViewById(R.id.barra_expediente);
        listaDias = findViewById(R.id.lista_dias);

        //As 4 linhas do Controle de Tarefas usam o mesmo layout (item_tarefa.xml) com textos diferentes
        tarefas = new Tarefa[]{
                new Tarefa(R.id.tarefa_entrada, R.string.tarefaInicioExpediente, R.string.horarioPrevistoEntrada,
                        TipoPonto.ENTRADA, null),
                new Tarefa(R.id.tarefa_almoco, R.string.tarefaAlmoco, R.string.horarioPrevistoAlmoco,
                        TipoPonto.ALMOCO_INICIO, TipoPonto.ALMOCO_FIM),
                new Tarefa(R.id.tarefa_intervalo, R.string.tarefaIntervalo, R.string.horarioPrevistoIntervalo,
                        TipoPonto.INTERVALO_INICIO, TipoPonto.INTERVALO_FIM),
                new Tarefa(R.id.tarefa_saida, R.string.tarefaFimExpediente, R.string.horarioPrevistoSaida,
                        TipoPonto.SAIDA, null),
        };

        Button btnLogout = findViewById(R.id.btn_logout_funcionario);
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut(); //Desloga o usuário do Firebase Auth
            abrirTelaLogin();
        });

        //Atalho para escrever um relatório diário
        findViewById(R.id.card_relatorio_diario).setOnClickListener(v ->
                startActivity(new Intent(this, NovoRelatorioActivity.class)));

        //Rodapé: lista dos relatórios que este funcionário já escreveu
        findViewById(R.id.btn_relatorios).setOnClickListener(v ->
                startActivity(new Intent(this, MeusRelatoriosActivity.class)));
    }

    //Se não houver usuário logado, volta para a tela de Login; se houver, carrega o nome e o ponto.
    //Fica no onStart (e não no onCreate) para atualizar a tela sempre que o funcionário voltar a ela.
    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() == null) {
            abrirTelaLogin();
            return;
        }
        String uid = mAuth.getCurrentUser().getUid();
        carregarNome(uid);
        carregarPonto(uid);
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

    //Método que busca no Firestore os registros de hoje e dos 15 dias anteriores (uma consulta só)
    private void carregarPonto(String uid) {
        Calendar agora = Calendar.getInstance();
        chaveDeHoje = PontoDoDia.chaveDoDia(agora.getTime());
        statusHojeData.setText(primeiraMaiuscula(formatoDataHoje.format(agora.getTime())));

        Calendar primeiroDia = (Calendar) agora.clone();
        primeiroDia.add(Calendar.DAY_OF_MONTH, -DIAS_ANTERIORES);

        pontoRepository.listarDesde(uid, PontoDoDia.chaveDoDia(primeiroDia.getTime()), task -> {
            if (isFinishing()) {
                return;
            }
            if (!task.isSuccessful()) {
                statusHojeSituacao.setText(R.string.erroCarregarPonto);
                return;
            }
            Map<String, PontoDoDia> dias = PontoRepository.agruparPorDia(task.getResult().getDocuments());
            PontoDoDia hoje = dias.get(chaveDeHoje);
            pontoDeHoje = hoje != null ? hoje : new PontoDoDia();
            atualizarStatusDeHoje();
            atualizarTarefas();
            montarUltimosDias(dias, agora);
        });
    }

    //STATUS DE HOJE: frase da situação e barra do expediente
    private void atualizarStatusDeHoje() {
        switch (pontoDeHoje.situacao()) {
            case TRABALHANDO:
                statusHojeSituacao.setText(getString(R.string.situacaoTrabalhando, hora(TipoPonto.ENTRADA)));
                break;
            case ALMOCO:
                statusHojeSituacao.setText(getString(R.string.situacaoAlmoco, hora(TipoPonto.ALMOCO_INICIO)));
                break;
            case INTERVALO:
                statusHojeSituacao.setText(getString(R.string.situacaoIntervalo, hora(TipoPonto.INTERVALO_INICIO)));
                break;
            case ENCERRADO:
                statusHojeSituacao.setText(getString(R.string.situacaoEncerrado, hora(TipoPonto.SAIDA)));
                break;
            default:
                statusHojeSituacao.setText(R.string.situacaoNaoIniciado);
        }
        int minutosAgora = PontoDoDia.minutosDoDia(new Date());
        barraExpediente.setProgressCompat(pontoDeHoje.progressoExpediente(minutosAgora), true);
    }

    //ÚLTIMOS 15 DIAS: cria um cartão (item_dia.xml) para cada dia, do mais recente para o mais antigo
    private void montarUltimosDias(Map<String, PontoDoDia> dias, Calendar agora) {
        listaDias.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        Calendar dia = (Calendar) agora.clone();
        for (int i = 0; i < DIAS_ANTERIORES; i++) {
            dia.add(Calendar.DAY_OF_MONTH, -1);
            Date data = dia.getTime();
            PontoDoDia ponto = dias.get(PontoDoDia.chaveDoDia(data));
            PontoDoDia.Resumo resumo = ponto == null ? PontoDoDia.Resumo.SEM_REGISTRO : ponto.resumo();

            View cartao = inflater.inflate(R.layout.item_dia, listaDias, false);
            //"seg." -> "SEG"
            String semana = formatoDiaSemana.format(data).replace(".", "").toUpperCase(PT_BR);
            ((TextView) cartao.findViewById(R.id.dia_semana)).setText(semana);
            ((TextView) cartao.findViewById(R.id.dia_data)).setText(formatoDiaMes.format(data));
            TextView status = cartao.findViewById(R.id.dia_status);
            switch (resumo) {
                case COMPLETO:
                    pintarStatus(status, R.string.diaCompleto, R.color.verde_finalizar);
                    break;
                case INCOMPLETO:
                    pintarStatus(status, R.string.diaIncompleto, R.color.vermelho_sair);
                    break;
                default:
                    pintarStatus(status, R.string.diaSemRegistro, R.color.cinza_texto);
            }
            listaDias.addView(cartao);
        }
    }

    private void pintarStatus(TextView status, @StringRes int texto, @ColorRes int cor) {
        status.setText(texto);
        status.setTextColor(ContextCompat.getColor(this, cor));
    }

    //CONTROLE DE TAREFAS: atualiza o texto, a cor e se o botão pode ser tocado em cada linha
    private void atualizarTarefas() {
        for (Tarefa tarefa : tarefas) {
            tarefa.atualizar();
        }
    }

    //Pede confirmação antes de gravar, porque o ponto batido não pode ser corrigido pelo app
    private void confirmarRegistro(TipoPonto tipo) {
        String mensagem = getString(R.string.confirmarPontoMensagem,
                getString(nomeDoRegistro(tipo)), formatoHora.format(new Date()));
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.confirmarPontoTitulo)
                .setMessage(mensagem)
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.registrar, (dialogo, botao) -> registrar(tipo))
                .show();
    }

    //Método que grava o ponto e, se der certo, recarrega a tela para mostrar o horário do servidor
    private void registrar(TipoPonto tipo) {
        if (registrando || mAuth.getCurrentUser() == null) {
            return;
        }
        String uid = mAuth.getCurrentUser().getUid();
        registrando = true;
        atualizarTarefas(); //Desliga os botões enquanto grava
        pontoRepository.registrar(uid, chaveDeHoje, tipo, task -> {
            registrando = false;
            if (isFinishing()) {
                return;
            }
            if (task.isSuccessful()) {
                Toast.makeText(this, R.string.pontoRegistrado, Toast.LENGTH_SHORT).show();
                carregarPonto(uid);
            } else {
                Toast.makeText(this, R.string.erroRegistrarPonto, Toast.LENGTH_LONG).show();
                atualizarTarefas();
            }
        });
    }

    //Nome de cada registro, usado na mensagem de confirmação
    @StringRes
    private static int nomeDoRegistro(TipoPonto tipo) {
        switch (tipo) {
            case ALMOCO_INICIO:
                return R.string.pontoAlmocoInicio;
            case ALMOCO_FIM:
                return R.string.pontoAlmocoFim;
            case INTERVALO_INICIO:
                return R.string.pontoIntervaloInicio;
            case INTERVALO_FIM:
                return R.string.pontoIntervaloFim;
            case SAIDA:
                return R.string.pontoSaida;
            default:
                return R.string.pontoEntrada;
        }
    }

    //Horário (HH:mm) em que um registro de hoje foi feito
    private String hora(TipoPonto tipo) {
        Date horario = pontoDeHoje.horario(tipo);
        return horario == null ? "" : formatoHora.format(horario);
    }

    //O Java escreve "terça-feira"; na tela fica "Terça-feira"
    private static String primeiraMaiuscula(String texto) {
        return texto.isEmpty() ? texto : Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }

    //Método que volta para a tela de Login limpando a pilha de telas
    private void abrirTelaLogin() {
        Intent intent = new Intent(this, loginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    //Uma linha do Controle de Tarefas. Pode ter um registro só (Início/Fim de Expediente)
    //ou dois (Almoço e Intervalo: primeiro "Iniciar", depois "Finalizar").
    private class Tarefa {
        private final TipoPonto primeiro;
        private final TipoPonto segundo; //null quando a tarefa tem um registro só
        private final TextView registrado;
        private final MaterialButton botao;

        Tarefa(int idLinha, @StringRes int titulo, @StringRes int horarioPrevisto,
               TipoPonto primeiro, TipoPonto segundo) {
            this.primeiro = primeiro;
            this.segundo = segundo;
            View linha = findViewById(idLinha);
            ((TextView) linha.findViewById(R.id.tarefa_titulo)).setText(titulo);
            ((TextView) linha.findViewById(R.id.tarefa_horario)).setText(horarioPrevisto);
            registrado = linha.findViewById(R.id.tarefa_registrado);
            botao = linha.findViewById(R.id.tarefa_botao);

            //Localização e selfie aparecem como no protótipo, mas ainda não funcionam
            View.OnClickListener emBreve = v -> Toast.makeText(MainFuncionarioActivity.this,
                    R.string.localizacaoSelfieEmBreve, Toast.LENGTH_SHORT).show();
            linha.findViewById(R.id.tarefa_localizacao).setOnClickListener(emBreve);
            linha.findViewById(R.id.tarefa_camera).setOnClickListener(emBreve);

            botao.setOnClickListener(v -> {
                TipoPonto proximo = proximoRegistro();
                if (proximo != null) {
                    confirmarRegistro(proximo);
                }
            });
        }

        //O registro que o botão vai gravar agora, ou null se a tarefa já foi concluída
        private TipoPonto proximoRegistro() {
            if (!pontoDeHoje.foiRegistrado(primeiro)) {
                return primeiro;
            }
            if (segundo != null && !pontoDeHoje.foiRegistrado(segundo)) {
                return segundo;
            }
            return null;
        }

        void atualizar() {
            TipoPonto proximo = proximoRegistro();

            //Texto com o que já foi registrado hoje
            if (!pontoDeHoje.foiRegistrado(primeiro)) {
                registrado.setVisibility(View.GONE);
            } else {
                registrado.setVisibility(View.VISIBLE);
                if (segundo == null) {
                    registrado.setText(getString(R.string.registradoAs, hora(primeiro)));
                } else if (proximo == segundo) {
                    registrado.setText(getString(R.string.iniciadoAs, hora(primeiro)));
                } else {
                    registrado.setText(getString(R.string.iniciadoEFinalizado, hora(primeiro), hora(segundo)));
                }
            }

            //Botão: Iniciar (roxo), Finalizar (verde) ou Feito (cinza, desligado).
            //Como no protótipo, o Fim de Expediente já começa como "Finalizar".
            boolean podeTocar;
            if (proximo == null) {
                botao.setText(R.string.concluido);
                pintarBotao(R.color.gray);
                podeTocar = false;
            } else {
                boolean ehFinalizar = proximo == segundo || proximo == TipoPonto.SAIDA;
                botao.setText(ehFinalizar ? R.string.finalizar : R.string.iniciar);
                pintarBotao(ehFinalizar ? R.color.verde_finalizar : R.color.roxo_bem_me_quer);
                podeTocar = pontoDeHoje.podeRegistrar(proximo) && !registrando;
            }
            botao.setEnabled(podeTocar);
            botao.setAlpha(podeTocar || proximo == null ? 1f : 0.4f); //Botão "apagado" quando ainda não pode
        }

        private void pintarBotao(@ColorRes int cor) {
            botao.setBackgroundTintList(ContextCompat.getColorStateList(MainFuncionarioActivity.this, cor));
        }
    }
}
