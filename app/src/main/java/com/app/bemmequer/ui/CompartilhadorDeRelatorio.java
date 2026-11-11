package com.app.bemmequer.ui;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.app.bemmequer.R;
import com.app.bemmequer.data.Relatorio;
import com.app.bemmequer.util.RelatorioPdf;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

//Gera o PDF de um relatório e abre a folha de compartilhamento do Android
//(salvar no Drive, enviar por WhatsApp ou e-mail, imprimir: depende dos apps instalados no celular).
public final class CompartilhadorDeRelatorio {
    //Gravar arquivo é um trabalho "lento" para a tela: roda numa segunda linha de execução (thread),
    //para o app não travar enquanto o PDF é montado
    private static final Executor SEGUNDO_PLANO = Executors.newSingleThreadExecutor();
    //Evita gerar dois PDFs se a pessoa tocar duas vezes seguidas no botão (só é lido e alterado na thread da tela)
    private static boolean gerando = false;

    private CompartilhadorDeRelatorio() {
    }

    public static void compartilhar(Activity tela, Relatorio relatorio) {
        if (gerando) {
            return;
        }
        gerando = true;
        SEGUNDO_PLANO.execute(() -> {
            File arquivo = null;
            try {
                arquivo = RelatorioPdf.gerar(tela.getApplicationContext(), relatorio);
            } catch (IOException | RuntimeException erro) {
                //arquivo continua null e a pessoa é avisada logo abaixo
            }
            File pdf = arquivo;
            //Só a thread principal pode mexer na tela (mostrar Toast, abrir outra tela)
            tela.runOnUiThread(() -> {
                gerando = false;
                if (tela.isFinishing() || tela.isDestroyed()) {
                    return;
                }
                if (pdf == null) {
                    Toast.makeText(tela, R.string.erroGerarPdf, Toast.LENGTH_LONG).show();
                    return;
                }
                abrirFolhaDeCompartilhamento(tela, pdf, relatorio);
            });
        });
    }

    private static void abrirFolhaDeCompartilhamento(Activity tela, File pdf, Relatorio relatorio) {
        //Outros apps não enxergam o cache do nosso app. O FileProvider cria um endereço (content://...)
        //que dá ao app escolhido permissão temporária de LEITURA só deste arquivo.
        Uri endereco = FileProvider.getUriForFile(tela, tela.getPackageName() + ".fileprovider", pdf);
        Intent enviar = new Intent(Intent.ACTION_SEND);
        enviar.setType("application/pdf");
        enviar.putExtra(Intent.EXTRA_STREAM, endereco);
        enviar.putExtra(Intent.EXTRA_SUBJECT,
                tela.getString(R.string.assuntoRelatorioPdf, relatorio.getNomePaciente()));
        enviar.setClipData(ClipData.newRawUri(pdf.getName(), endereco)); //Permite a prévia do arquivo na folha
        enviar.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            tela.startActivity(Intent.createChooser(enviar, tela.getString(R.string.compartilharRelatorioTitulo)));
        } catch (ActivityNotFoundException erro) {
            Toast.makeText(tela, R.string.erroSemAppParaCompartilhar, Toast.LENGTH_LONG).show();
        }
    }
}
