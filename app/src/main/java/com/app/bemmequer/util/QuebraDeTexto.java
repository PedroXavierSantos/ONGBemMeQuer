package com.app.bemmequer.util;

import java.util.ArrayList;
import java.util.List;

//Divide um texto em linhas que cabem numa largura e depois em páginas.
//O PDF não quebra linha sozinho (diferente de um TextView): quem desenha precisa dizer onde cada linha termina.
//Esta classe não usa nada do Android, por isso pode ser testada no computador (QuebraDeTextoTest).
public final class QuebraDeTexto {

    //Quem sabe medir a largura de um texto. No PDF é o Paint (paint::measureText); nos testes, uma conta simples.
    public interface Medidor {
        float largura(String texto);
    }

    private QuebraDeTexto() {
    }

    //Método que quebra o texto em linhas com no máximo larguraMaxima.
    //Respeita os "Enter" que a pessoa digitou e só corta uma palavra no meio se ela sozinha não couber na linha.
    public static List<String> quebrarEmLinhas(String texto, float larguraMaxima, Medidor medidor) {
        List<String> linhas = new ArrayList<>();
        if (texto == null || texto.trim().isEmpty()) {
            return linhas;
        }
        //Cada "Enter" do texto começa um parágrafo novo (o -1 mantém as linhas em branco entre parágrafos)
        String[] paragrafos = texto.trim().replace("\r\n", "\n").replace('\r', '\n').replace('\t', ' ').split("\n", -1);
        for (String paragrafo : paragrafos) {
            if (paragrafo.trim().isEmpty()) {
                linhas.add("");
                continue;
            }
            String linha = "";
            for (String palavra : paragrafo.trim().split(" +")) {
                String tentativa = linha.isEmpty() ? palavra : linha + " " + palavra;
                if (medidor.largura(tentativa) <= larguraMaxima) {
                    linha = tentativa; //A palavra coube: continua na mesma linha
                    continue;
                }
                if (!linha.isEmpty()) {
                    linhas.add(linha); //Não coube: fecha a linha atual e a palavra vai para a próxima
                }
                //Palavra maior que a linha inteira (ex.: um link): corta em pedaços que cabem
                while (medidor.largura(palavra) > larguraMaxima && palavra.length() > 1) {
                    int corte = 1;
                    while (corte < palavra.length()
                            && medidor.largura(palavra.substring(0, corte + 1)) <= larguraMaxima) {
                        corte++;
                    }
                    linhas.add(palavra.substring(0, corte));
                    palavra = palavra.substring(corte);
                }
                linha = palavra;
            }
            linhas.add(linha);
        }
        return linhas;
    }

    //Método que distribui as linhas em páginas. A primeira página tem menos espaço (tem o cabeçalho completo).
    //Sempre devolve pelo menos uma página, mesmo sem linhas.
    public static List<List<String>> paginar(List<String> linhas, int linhasNaPrimeira, int linhasNasDemais) {
        List<List<String>> paginas = new ArrayList<>();
        int inicio = 0;
        int cabem = Math.max(1, linhasNaPrimeira);
        do {
            int fim = Math.min(linhas.size(), inicio + cabem);
            paginas.add(new ArrayList<>(linhas.subList(inicio, fim)));
            inicio = fim;
            cabem = Math.max(1, linhasNasDemais);
        } while (inicio < linhas.size());
        return paginas;
    }
}
