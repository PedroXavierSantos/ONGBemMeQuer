package com.app.bemmequer.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;

import androidx.core.content.ContextCompat;

import com.app.bemmequer.R;
import com.app.bemmequer.data.Relatorio;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

//Monta o PDF de um relatório diário no próprio aparelho, com o PdfDocument do Android (sem internet e sem custo).
//O PdfDocument funciona como uma folha em branco: a gente desenha cada texto e imagem na posição (x, y) desejada.
//As medidas são em pontos (1 ponto = 1/72 de polegada); uma folha A4 tem 595 x 842 pontos.
public final class RelatorioPdf {
    //Pasta dentro do cache do app onde os PDFs ficam (a mesma declarada em res/xml/caminhos_compartilhados.xml)
    public static final String PASTA = "relatorios_pdf";

    private static final int LARGURA_PAGINA = 595;
    private static final int ALTURA_PAGINA = 842;
    private static final float MARGEM = 48;
    private static final float LARGURA_UTIL = LARGURA_PAGINA - 2 * MARGEM;
    private static final float ALTURA_LOGO = 60;
    private static final float ALTURA_LINHA = 18; //Distância entre uma linha do relato e a próxima
    private static final float Y_RODAPE = ALTURA_PAGINA - MARGEM; //Onde fica o texto do rodapé
    private static final float Y_LIMITE_TEXTO = Y_RODAPE - 28; //O relato não passa daqui (deixa espaço para o rodapé)
    private static final long UM_DIA_EM_MS = 24L * 60 * 60 * 1000;

    private RelatorioPdf() {
    }

    //Método que gera o PDF do relatório e devolve o arquivo gravado no cache do app.
    //O cache é uma pasta particular do app: não precisa de permissão e o Android pode limpar quando faltar espaço.
    public static File gerar(Context contexto, Relatorio relatorio) throws IOException {
        //"Canetas" usadas para desenhar: cada Paint guarda tamanho, cor e estilo de um tipo de texto
        int azul = ContextCompat.getColor(contexto, R.color.azul_bem_me_quer);
        int cinza = ContextCompat.getColor(contexto, R.color.cinza_texto);
        int preto = ContextCompat.getColor(contexto, R.color.black);
        Paint canetaOng = caneta(11, azul, true);
        Paint canetaTitulo = caneta(22, preto, true);
        Paint canetaRotulo = caneta(9, cinza, true);
        Paint canetaValor = caneta(12, preto, false);
        Paint canetaRodape = caneta(9, cinza, false);
        Paint canetaTraco = caneta(0, azul, false);
        canetaTraco.setStrokeWidth(1.5f);

        String nomePaciente = textoOuVazio(relatorio.getNomePaciente());
        String data = relatorio.dataPorExtenso();
        //Primeiro descobre onde cada linha do relato termina (o PDF não quebra linha sozinho)
        List<String> linhasDoRelato = QuebraDeTexto.quebrarEmLinhas(
                relatorio.getRelato(), LARGURA_UTIL, canetaValor::measureText);

        PdfDocument documento = new PdfDocument();
        try {
            //PÁGINA 1: cabeçalho completo
            PdfDocument.Page pagina = documento.startPage(novaPagina(1));
            Canvas folha = pagina.getCanvas();
            float y = desenharCabecalho(contexto, folha, canetaOng, canetaTitulo, canetaTraco);
            y = desenharCampo(folha, contexto.getString(R.string.campoPaciente), nomePaciente, y, canetaRotulo, canetaValor);
            y = desenharCampo(folha, contexto.getString(R.string.campoData), data, y, canetaRotulo, canetaValor);
            y = desenharCampo(folha, contexto.getString(R.string.pdfProfissional),
                    textoOuVazio(relatorio.getNomeFuncionario()), y, canetaRotulo, canetaValor);
            y = desenharCampo(folha, contexto.getString(R.string.campoLocal),
                    textoOuVazio(relatorio.getLocal()), y, canetaRotulo, canetaValor);
            y += 8;
            folha.drawText(contexto.getString(R.string.campoRelato).toUpperCase(new Locale("pt", "BR")),
                    MARGEM, y, canetaRotulo);
            y += 4;

            //Agora que sabemos onde o relato começa, dá para calcular quantas linhas cabem em cada página
            float yInicioContinuacao = MARGEM + 24;
            int linhasNaPrimeira = (int) ((Y_LIMITE_TEXTO - y) / ALTURA_LINHA);
            int linhasNasDemais = (int) ((Y_LIMITE_TEXTO - yInicioContinuacao) / ALTURA_LINHA);
            List<List<String>> paginas = QuebraDeTexto.paginar(linhasDoRelato, linhasNaPrimeira, linhasNasDemais);

            for (int i = 0; i < paginas.size(); i++) {
                if (i > 0) {
                    //PÁGINAS SEGUINTES: cabeçalho pequeno, só para a folha solta não ficar sem identificação
                    pagina = documento.startPage(novaPagina(i + 1));
                    folha = pagina.getCanvas();
                    folha.drawText(contexto.getString(R.string.pdfCabecalhoContinuacao, nomePaciente, data),
                            MARGEM, MARGEM + 4, canetaRodape);
                    folha.drawLine(MARGEM, MARGEM + 12, LARGURA_PAGINA - MARGEM, MARGEM + 12, canetaTraco);
                    y = yInicioContinuacao;
                }
                for (String linha : paginas.get(i)) {
                    y += ALTURA_LINHA;
                    folha.drawText(linha, MARGEM, y, canetaValor);
                }
                desenharRodape(contexto, folha, i + 1, paginas.size(), canetaRodape);
                documento.finishPage(pagina);
            }

            File arquivo = new File(prepararPasta(contexto), nomeDoArquivo(relatorio));
            try (FileOutputStream saida = new FileOutputStream(arquivo)) {
                documento.writeTo(saida);
            }
            return arquivo;
        } finally {
            documento.close(); //Libera a memória do documento, mesmo se der erro no meio
        }
    }

    //Método que monta o nome do arquivo, ex.: relatorio-ana-clara-2026-10-30.pdf
    //Tira acentos e símbolos do nome do paciente, porque nem todo app aceita esses caracteres em nome de arquivo.
    public static String nomeDoArquivo(Relatorio relatorio) {
        StringBuilder nome = new StringBuilder("relatorio");
        String paciente = Normalizer.normalize(textoOuVazio(relatorio.getNomePaciente()), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "") //Remove os acentos (que o NFD separou das letras)
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        if (!paciente.isEmpty()) {
            nome.append('-').append(paciente);
        }
        if (relatorio.getData() != null) {
            nome.append('-').append(new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(relatorio.getData()));
        }
        return nome.append(".pdf").toString();
    }

    //Desenha o logo, o nome da ONG, o título e o traço azul. Devolve o y onde o próximo conteúdo começa.
    private static float desenharCabecalho(Context contexto, Canvas folha, Paint canetaOng, Paint canetaTitulo,
                                           Paint canetaTraco) {
        float xTexto = MARGEM;
        Bitmap logo = BitmapFactory.decodeResource(contexto.getResources(), R.drawable.logo_bmq);
        if (logo != null) {
            float larguraLogo = ALTURA_LOGO * logo.getWidth() / logo.getHeight(); //Mantém a proporção da imagem
            //Reduz a imagem antes de colocar no PDF (3x o tamanho final basta para imprimir e o arquivo fica leve)
            Bitmap logoReduzido = Bitmap.createScaledBitmap(logo,
                    Math.round(larguraLogo * 3), Math.round(ALTURA_LOGO * 3), true);
            folha.drawBitmap(logoReduzido, null,
                    new RectF(MARGEM, MARGEM, MARGEM + larguraLogo, MARGEM + ALTURA_LOGO),
                    new Paint(Paint.FILTER_BITMAP_FLAG));
            xTexto = MARGEM + larguraLogo + 16;
        }
        folha.drawText(contexto.getString(R.string.pdfNomeOng), xTexto, MARGEM + 24, canetaOng);
        folha.drawText(contexto.getString(R.string.relatorioDiario), xTexto, MARGEM + 50, canetaTitulo);
        float yTraco = MARGEM + ALTURA_LOGO + 12;
        folha.drawLine(MARGEM, yTraco, LARGURA_PAGINA - MARGEM, yTraco, canetaTraco);
        return yTraco + 24;
    }

    //Desenha um campo (rótulo pequeno em cima, valor embaixo). Devolve o y do próximo campo.
    private static float desenharCampo(Canvas folha, String rotulo, String valor, float y,
                                       Paint canetaRotulo, Paint canetaValor) {
        folha.drawText(rotulo.toUpperCase(new Locale("pt", "BR")), MARGEM, y, canetaRotulo);
        //Um valor muito comprido (ex.: um local com endereço) também é quebrado em linhas
        List<String> linhas = QuebraDeTexto.quebrarEmLinhas(valor, LARGURA_UTIL, canetaValor::measureText);
        if (linhas.isEmpty()) {
            linhas.add("—");
        }
        for (String linha : linhas) {
            y += 15;
            folha.drawText(linha, MARGEM, y, canetaValor);
        }
        return y + 22;
    }

    //Desenha o rodapé: de onde o documento veio (à esquerda) e o número da página (à direita)
    private static void desenharRodape(Context contexto, Canvas folha, int numero, int total, Paint canetaRodape) {
        String hoje = new SimpleDateFormat("dd/MM/yyyy", new Locale("pt", "BR")).format(new Date());
        folha.drawLine(MARGEM, Y_RODAPE - 14, LARGURA_PAGINA - MARGEM, Y_RODAPE - 14, canetaRodape);
        folha.drawText(contexto.getString(R.string.pdfRodapeGerado, hoje), MARGEM, Y_RODAPE, canetaRodape);
        String pagina = contexto.getString(R.string.pdfPagina, numero, total);
        folha.drawText(pagina, LARGURA_PAGINA - MARGEM - canetaRodape.measureText(pagina), Y_RODAPE, canetaRodape);
    }

    //Cria a pasta dos PDFs no cache e apaga os que têm mais de um dia, para o cache não crescer sem parar
    private static File prepararPasta(Context contexto) throws IOException {
        File pasta = new File(contexto.getCacheDir(), PASTA);
        if (!pasta.isDirectory() && !pasta.mkdirs()) {
            throw new IOException("Não foi possível criar a pasta " + pasta);
        }
        File[] antigos = pasta.listFiles();
        if (antigos != null) {
            long limite = System.currentTimeMillis() - UM_DIA_EM_MS;
            for (File antigo : antigos) {
                if (antigo.lastModified() < limite) {
                    //noinspection ResultOfMethodCallIgnored
                    antigo.delete();
                }
            }
        }
        return pasta;
    }

    private static PdfDocument.PageInfo novaPagina(int numero) {
        return new PdfDocument.PageInfo.Builder(LARGURA_PAGINA, ALTURA_PAGINA, numero).create();
    }

    private static Paint caneta(float tamanho, int cor, boolean negrito) {
        Paint caneta = new Paint(Paint.ANTI_ALIAS_FLAG);
        caneta.setTextSize(tamanho);
        caneta.setColor(cor);
        caneta.setTypeface(Typeface.create(Typeface.SANS_SERIF, negrito ? Typeface.BOLD : Typeface.NORMAL));
        return caneta;
    }

    private static String textoOuVazio(String texto) {
        return texto == null ? "" : texto.trim();
    }
}
