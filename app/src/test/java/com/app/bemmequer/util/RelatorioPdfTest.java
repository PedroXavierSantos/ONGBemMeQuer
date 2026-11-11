package com.app.bemmequer.util;

import static org.junit.Assert.assertEquals;

import com.app.bemmequer.data.Relatorio;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

//Testes do nome do arquivo e da data do PDF. O desenho do PDF em si precisa de um celular
//e é testado em RelatorioPdfInstrumentedTest (pasta androidTest).
public class RelatorioPdfTest {

    private static Relatorio relatorio(String nomePaciente, Date data) {
        return new Relatorio("uid-paciente", nomePaciente, "uid-funcionario", "Carla Souza",
                "Relato de teste", data, "ONG");
    }

    private static Date dia(int ano, int mes, int dia) {
        Calendar calendario = Calendar.getInstance();
        calendario.set(ano, mes, dia, 10, 0, 0);
        return calendario.getTime();
    }

    @Test
    public void nomeDoArquivo_tiraAcentosEEspacos() {
        assertEquals("relatorio-joao-antonio-conceicao-2026-10-30.pdf",
                RelatorioPdf.nomeDoArquivo(relatorio("João Antônio Conceição", dia(2026, Calendar.OCTOBER, 30))));
    }

    @Test
    public void nomeDoArquivo_tiraSimbolosQueNaoPodemEmNomeDeArquivo() {
        assertEquals("relatorio-ana-b-2026-01-05.pdf",
                RelatorioPdf.nomeDoArquivo(relatorio(" Ana / B.? ", dia(2026, Calendar.JANUARY, 5))));
    }

    @Test
    public void nomeDoArquivo_semNomeESemData() {
        assertEquals("relatorio.pdf", RelatorioPdf.nomeDoArquivo(relatorio(null, null)));
    }

    @Test
    public void dataPorExtenso_comecaComLetraMaiuscula() {
        assertEquals("Sexta-feira, 30 de outubro de 2026",
                relatorio("Ana", dia(2026, Calendar.OCTOBER, 30)).dataPorExtenso());
    }

    @Test
    public void dataPorExtenso_semData_ficaVazia() {
        assertEquals("", relatorio("Ana", null).dataPorExtenso());
    }
}
