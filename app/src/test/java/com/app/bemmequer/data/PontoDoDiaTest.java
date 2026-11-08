package com.app.bemmequer.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

//Testes da lógica do ponto (rodam no computador, sem celular): ./gradlew testDebugUnitTest
public class PontoDoDiaTest {

    private static Date horario(int hora, int minuto) {
        Calendar calendario = Calendar.getInstance();
        calendario.set(2026, Calendar.NOVEMBER, 10, hora, minuto, 0);
        return calendario.getTime();
    }

    @Test
    public void diaNovo_soPermiteAEntrada() {
        PontoDoDia ponto = new PontoDoDia();
        assertTrue(ponto.podeRegistrar(TipoPonto.ENTRADA));
        assertFalse(ponto.podeRegistrar(TipoPonto.ALMOCO_INICIO));
        assertFalse(ponto.podeRegistrar(TipoPonto.INTERVALO_INICIO));
        assertFalse(ponto.podeRegistrar(TipoPonto.SAIDA));
        assertEquals(PontoDoDia.Situacao.NAO_INICIADO, ponto.situacao());
        assertEquals(PontoDoDia.Resumo.SEM_REGISTRO, ponto.resumo());
    }

    @Test
    public void depoisDaEntrada_liberaAlmocoIntervaloESaida() {
        PontoDoDia ponto = new PontoDoDia();
        ponto.registrar(TipoPonto.ENTRADA, horario(8, 0));
        assertFalse(ponto.podeRegistrar(TipoPonto.ENTRADA));
        assertTrue(ponto.podeRegistrar(TipoPonto.ALMOCO_INICIO));
        assertFalse(ponto.podeRegistrar(TipoPonto.ALMOCO_FIM));
        assertTrue(ponto.podeRegistrar(TipoPonto.INTERVALO_INICIO));
        assertTrue(ponto.podeRegistrar(TipoPonto.SAIDA));
        assertEquals(PontoDoDia.Situacao.TRABALHANDO, ponto.situacao());
        assertEquals(PontoDoDia.Resumo.INCOMPLETO, ponto.resumo());
    }

    @Test
    public void almocoIniciado_mostraSituacaoAlmocoELiberaOFim() {
        PontoDoDia ponto = new PontoDoDia();
        ponto.registrar(TipoPonto.ENTRADA, horario(8, 0));
        ponto.registrar(TipoPonto.ALMOCO_INICIO, horario(12, 0));
        assertEquals(PontoDoDia.Situacao.ALMOCO, ponto.situacao());
        assertTrue(ponto.podeRegistrar(TipoPonto.ALMOCO_FIM));

        ponto.registrar(TipoPonto.ALMOCO_FIM, horario(13, 0));
        assertEquals(PontoDoDia.Situacao.TRABALHANDO, ponto.situacao());
    }

    @Test
    public void intervaloIniciado_mostraSituacaoIntervalo() {
        PontoDoDia ponto = new PontoDoDia();
        ponto.registrar(TipoPonto.ENTRADA, horario(8, 0));
        ponto.registrar(TipoPonto.INTERVALO_INICIO, horario(15, 0));
        assertEquals(PontoDoDia.Situacao.INTERVALO, ponto.situacao());
    }

    @Test
    public void depoisDaSaida_nadaMaisPodeSerRegistrado() {
        PontoDoDia ponto = new PontoDoDia();
        ponto.registrar(TipoPonto.ENTRADA, horario(8, 0));
        ponto.registrar(TipoPonto.SAIDA, horario(18, 0));
        for (TipoPonto tipo : TipoPonto.values()) {
            assertFalse(tipo.name(), ponto.podeRegistrar(tipo));
        }
        assertEquals(PontoDoDia.Situacao.ENCERRADO, ponto.situacao());
        assertEquals(PontoDoDia.Resumo.COMPLETO, ponto.resumo());
    }

    @Test
    public void barraDoExpediente_vaiDe8As18() {
        assertEquals(0, PontoDoDia.progresso(7 * 60));
        assertEquals(0, PontoDoDia.progresso(8 * 60));
        assertEquals(50, PontoDoDia.progresso(13 * 60));
        assertEquals(100, PontoDoDia.progresso(18 * 60));
        assertEquals(100, PontoDoDia.progresso(20 * 60));
    }

    @Test
    public void barraDoExpediente_vaziaAntesDaEntradaECheiaDepoisDaSaida() {
        PontoDoDia ponto = new PontoDoDia();
        assertEquals(0, ponto.progressoExpediente(13 * 60));

        ponto.registrar(TipoPonto.ENTRADA, horario(8, 0));
        assertEquals(50, ponto.progressoExpediente(13 * 60));

        ponto.registrar(TipoPonto.SAIDA, horario(17, 0));
        assertEquals(100, ponto.progressoExpediente(17 * 60));
    }

    @Test
    public void minutosDoDia_contaDesdeAMeiaNoite() {
        assertEquals(12 * 60 + 30, PontoDoDia.minutosDoDia(horario(12, 30)));
    }

    @Test
    public void chaveDoDia_usaOFormatoDoFirestore() {
        assertEquals("2026-11-10", PontoDoDia.chaveDoDia(horario(9, 0)));
    }

    @Test
    public void tipoPonto_converteOTextoDoFirestore() {
        assertEquals(TipoPonto.ALMOCO_FIM, TipoPonto.deValor("almoco_fim"));
        assertNull(TipoPonto.deValor("hora_extra"));
    }
}
