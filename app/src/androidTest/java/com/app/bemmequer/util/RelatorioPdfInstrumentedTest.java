package com.app.bemmequer.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.app.bemmequer.data.Relatorio;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.Date;

//Testes que rodam DENTRO do celular (ou emulador), porque o PdfDocument só existe no Android:
//./gradlew connectedDebugAndroidTest
//Geram o PDF de verdade e o abrem de novo com o PdfRenderer para contar as páginas.
@RunWith(AndroidJUnit4.class)
public class RelatorioPdfInstrumentedTest {
    private final Context contexto = InstrumentationRegistry.getInstrumentation().getTargetContext();

    private static Relatorio relatorio(String nomePaciente, String relato) {
        return new Relatorio("uid-paciente", nomePaciente, "uid-funcionario", "Carla Souza (teste)",
                relato, new Date(), "ONG");
    }

    private int contarPaginas(File pdf) throws IOException {
        try (ParcelFileDescriptor arquivo = ParcelFileDescriptor.open(pdf, ParcelFileDescriptor.MODE_READ_ONLY);
             PdfRenderer leitor = new PdfRenderer(arquivo)) {
            return leitor.getPageCount();
        }
    }

    @Test
    public void relatoCurto_geraPdfDeUmaPagina() throws IOException {
        File pdf = RelatorioPdf.gerar(contexto, relatorio("Paciente Curto",
                "A sessão de hoje foi tranquila. Participou de todas as atividades."));
        assertTrue(pdf.length() > 0);
        assertEquals(new File(contexto.getCacheDir(), RelatorioPdf.PASTA), pdf.getParentFile());
        assertEquals(1, contarPaginas(pdf));
    }

    @Test
    public void relatoDe2000Caracteres_emTextoCorrido_cabeNumaPagina() throws IOException {
        StringBuilder relato = new StringBuilder();
        while (relato.length() < 2000) {
            relato.append("Fez os exercícios de coordenação com atenção e pediu para repetir a atividade. ");
        }
        File pdf = RelatorioPdf.gerar(contexto, relatorio("Paciente Corrido", relato.substring(0, 2000)));
        assertEquals(1, contarPaginas(pdf));
    }

    @Test
    public void relatoComMuitasLinhas_passaParaOutrasPaginas() throws IOException {
        //O pior caso que o app aceita: 2000 caracteres quase todos "Enter" (uma linha por caractere)
        StringBuilder relato = new StringBuilder();
        for (int i = 1; relato.length() < 1990; i++) {
            relato.append("Item ").append(i).append('\n');
        }
        File pdf = RelatorioPdf.gerar(contexto, relatorio("Paciente Longo", relato.toString()));
        assertTrue("páginas: " + contarPaginas(pdf), contarPaginas(pdf) > 1);
    }

    @Test
    public void relatorioComCamposVazios_naoQuebra() throws IOException {
        File pdf = RelatorioPdf.gerar(contexto, new Relatorio(null, null, null, null, null, null, null));
        assertEquals(1, contarPaginas(pdf));
    }
}
