package com.app.bemmequer.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

//Testes da quebra de linhas e de páginas do PDF (rodam no computador, sem celular): ./gradlew testDebugUnitTest
public class QuebraDeTextoTest {
    //Medidor de mentira: cada letra "mede" 1. Assim, largura máxima 10 = no máximo 10 letras por linha.
    private static final QuebraDeTexto.Medidor UMA_UNIDADE_POR_LETRA = String::length;

    private static List<String> quebrar(String texto, int largura) {
        return QuebraDeTexto.quebrarEmLinhas(texto, largura, UMA_UNIDADE_POR_LETRA);
    }

    @Test
    public void textoCurto_ficaNumaLinhaSo() {
        assertEquals(Collections.singletonList("Foi bem"), quebrar("Foi bem", 10));
    }

    @Test
    public void textoVazioOuNulo_naoTemLinhas() {
        assertTrue(quebrar(null, 10).isEmpty());
        assertTrue(quebrar("   \n ", 10).isEmpty());
    }

    @Test
    public void textoLongo_quebraEntreAsPalavras() {
        assertEquals(Arrays.asList("A Ana fez", "os", "exercicios", "hoje"),
                quebrar("A Ana fez os exercicios hoje", 10));
    }

    @Test
    public void nenhumaLinhaPassaDaLarguraMaxima() {
        String relato = "Hoje a sessão de fisioterapia foi muito produtiva e a paciente participou de tudo.";
        for (String linha : quebrar(relato, 20)) {
            assertTrue(linha, linha.length() <= 20);
        }
    }

    @Test
    public void enterDoTexto_comecaOutraLinhaEMantemLinhaEmBranco() {
        assertEquals(Arrays.asList("Manhã", "", "Tarde"), quebrar("Manhã\n\nTarde", 10));
        assertEquals(Arrays.asList("Manhã", "Tarde"), quebrar("Manhã\r\nTarde", 10));
    }

    @Test
    public void palavraMaiorQueALinha_eCortadaEmPedacos() {
        assertEquals(Arrays.asList("abcdef", "ghijkl", "fim"), quebrar("abcdefghijkl fim", 6));
    }

    @Test
    public void espacosRepetidos_viramUmSo() {
        assertEquals(Collections.singletonList("um dois"), quebrar("  um    dois  ", 10));
    }

    @Test
    public void paginar_primeiraPaginaTemMenosLinhas() {
        List<String> linhas = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            linhas.add("linha " + i);
        }
        List<List<String>> paginas = QuebraDeTexto.paginar(linhas, 3, 4);
        assertEquals(3, paginas.size());
        assertEquals(Arrays.asList("linha 1", "linha 2", "linha 3"), paginas.get(0));
        assertEquals(4, paginas.get(1).size());
        assertEquals(Arrays.asList("linha 8", "linha 9", "linha 10"), paginas.get(2));
    }

    @Test
    public void paginar_textoQueCabe_usaUmaPaginaSo() {
        assertEquals(1, QuebraDeTexto.paginar(Arrays.asList("a", "b", "c"), 3, 4).size());
    }

    @Test
    public void paginar_semLinhas_aindaTemUmaPagina() {
        List<List<String>> paginas = QuebraDeTexto.paginar(new ArrayList<>(), 3, 4);
        assertEquals(1, paginas.size());
        assertTrue(paginas.get(0).isEmpty());
    }
}
