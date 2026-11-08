package com.app.bemmequer.data;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

//Os registros de ponto de UM dia e as regras de quem vem antes de quem.
//Esta classe não usa Firebase nem Android: por isso dá para testá-la com JUnit (PontoDoDiaTest).
public class PontoDoDia {
    //Expediente previsto no protótipo: das 8:00 às 18:00
    public static final int INICIO_EXPEDIENTE_MINUTOS = 8 * 60;
    public static final int FIM_EXPEDIENTE_MINUTOS = 18 * 60;

    //Situação do funcionário agora (texto do "Status de Hoje")
    public enum Situacao { NAO_INICIADO, TRABALHANDO, ALMOCO, INTERVALO, ENCERRADO }

    //Resumo de um dia passado (cartões dos "Últimos 15 dias")
    public enum Resumo { SEM_REGISTRO, INCOMPLETO, COMPLETO }

    //EnumMap: um "dicionário" cujas chaves são os tipos de ponto, guardando o horário de cada um
    private final Map<TipoPonto, Date> horarios = new EnumMap<>(TipoPonto.class);

    public void registrar(TipoPonto tipo, Date horario) {
        horarios.put(tipo, horario);
    }

    public boolean foiRegistrado(TipoPonto tipo) {
        return horarios.containsKey(tipo);
    }

    //Horário do registro, ou null se ainda não foi feito
    public Date horario(TipoPonto tipo) {
        return horarios.get(tipo);
    }

    //Pode registrar se: ainda não registrou, o requisito já foi feito e o expediente não acabou
    public boolean podeRegistrar(TipoPonto tipo) {
        if (foiRegistrado(tipo) || foiRegistrado(TipoPonto.SAIDA)) {
            return false;
        }
        TipoPonto requisito = tipo.getRequisito();
        return requisito == null || foiRegistrado(requisito);
    }

    public Situacao situacao() {
        if (foiRegistrado(TipoPonto.SAIDA)) {
            return Situacao.ENCERRADO;
        }
        if (!foiRegistrado(TipoPonto.ENTRADA)) {
            return Situacao.NAO_INICIADO;
        }
        if (foiRegistrado(TipoPonto.ALMOCO_INICIO) && !foiRegistrado(TipoPonto.ALMOCO_FIM)) {
            return Situacao.ALMOCO;
        }
        if (foiRegistrado(TipoPonto.INTERVALO_INICIO) && !foiRegistrado(TipoPonto.INTERVALO_FIM)) {
            return Situacao.INTERVALO;
        }
        return Situacao.TRABALHANDO;
    }

    public Resumo resumo() {
        if (horarios.isEmpty()) {
            return Resumo.SEM_REGISTRO;
        }
        return foiRegistrado(TipoPonto.SAIDA) ? Resumo.COMPLETO : Resumo.INCOMPLETO;
    }

    //Quanto da barra do expediente (8:00 às 18:00) já está preenchido, de 0 a 100.
    //Antes da entrada a barra fica vazia; depois da saída, cheia.
    public int progressoExpediente(int minutosAgora) {
        if (situacao() == Situacao.NAO_INICIADO) {
            return 0;
        }
        if (situacao() == Situacao.ENCERRADO) {
            return 100;
        }
        return progresso(minutosAgora);
    }

    //Converte um horário do dia (em minutos desde a meia-noite) para a porcentagem da barra
    static int progresso(int minutosDoDia) {
        int duracao = FIM_EXPEDIENTE_MINUTOS - INICIO_EXPEDIENTE_MINUTOS;
        int passado = minutosDoDia - INICIO_EXPEDIENTE_MINUTOS;
        return Math.max(0, Math.min(100, passado * 100 / duracao));
    }

    //Minutos desde a meia-noite (ex.: 12:30 -> 750), no fuso horário do celular
    public static int minutosDoDia(Date horario) {
        Calendar calendario = Calendar.getInstance();
        calendario.setTime(horario);
        return calendario.get(Calendar.HOUR_OF_DAY) * 60 + calendario.get(Calendar.MINUTE);
    }

    //Chave do dia gravada no Firestore, no formato AAAA-MM-DD (ex.: 2026-11-10).
    //Nesse formato, a ordem alfabética é a mesma ordem das datas, o que facilita a consulta.
    public static String chaveDoDia(Date data) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(data);
    }
}
