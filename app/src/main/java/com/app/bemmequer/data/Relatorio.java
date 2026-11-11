package com.app.bemmequer.data;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

//Representa um relatório diário (documento da coleção "relatorios" no Firestore)
public class Relatorio {
    //Nomes dos campos no Firestore (os mesmos usados nas regras de segurança)
    public static final String CAMPO_PACIENTE_UID = "pacienteUid";
    public static final String CAMPO_NOME_PACIENTE = "nomePaciente";
    public static final String CAMPO_FUNCIONARIO_UID = "funcionarioUid";
    public static final String CAMPO_NOME_FUNCIONARIO = "nomeFuncionario";
    public static final String CAMPO_RELATO = "relato";
    public static final String CAMPO_DATA = "data";
    public static final String CAMPO_LOCAL = "local";
    public static final String CAMPO_CRIADO_EM = "criadoEm";

    //Atributos da classe
    private final String pacienteUid;
    private final String nomePaciente;
    private final String funcionarioUid;
    private final String nomeFuncionario;
    private final String relato;
    private final Date data;
    private final String local;

    public Relatorio(String pacienteUid, String nomePaciente, String funcionarioUid,
                     String nomeFuncionario, String relato, Date data, String local) {
        this.pacienteUid = pacienteUid;
        this.nomePaciente = nomePaciente;
        this.funcionarioUid = funcionarioUid;
        this.nomeFuncionario = nomeFuncionario;
        this.relato = relato;
        this.data = data;
        this.local = local;
    }

    //Método que monta um Relatorio a partir de um documento lido do Firestore
    public static Relatorio deDocumento(DocumentSnapshot documento) {
        Timestamp data = documento.getTimestamp(CAMPO_DATA);
        return new Relatorio(
                documento.getString(CAMPO_PACIENTE_UID),
                documento.getString(CAMPO_NOME_PACIENTE),
                documento.getString(CAMPO_FUNCIONARIO_UID),
                documento.getString(CAMPO_NOME_FUNCIONARIO),
                documento.getString(CAMPO_RELATO),
                data != null ? data.toDate() : null,
                documento.getString(CAMPO_LOCAL));
    }

    //Método que transforma o Relatorio nos dados que serão gravados no Firestore.
    //A data de criação é preenchida pelo servidor (FieldValue.serverTimestamp) no repositório.
    public Map<String, Object> paraDocumento() {
        Map<String, Object> dados = new HashMap<>();
        dados.put(CAMPO_PACIENTE_UID, pacienteUid);
        dados.put(CAMPO_NOME_PACIENTE, nomePaciente);
        dados.put(CAMPO_FUNCIONARIO_UID, funcionarioUid);
        dados.put(CAMPO_NOME_FUNCIONARIO, nomeFuncionario);
        dados.put(CAMPO_RELATO, relato);
        dados.put(CAMPO_DATA, new Timestamp(data));
        dados.put(CAMPO_LOCAL, local);
        return dados;
    }

    //Método que escreve a data por extenso, com a primeira letra maiúscula
    //(ex.: Sexta-feira, 30 de outubro de 2026). Usado no cartão da lista e no PDF.
    public String dataPorExtenso() {
        if (data == null) {
            return "";
        }
        String texto = new SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", new Locale("pt", "BR")).format(data);
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1); //O Java escreve "sexta-feira"
    }

    public String getNomePaciente() {
        return nomePaciente;
    }

    public String getNomeFuncionario() {
        return nomeFuncionario;
    }

    public String getRelato() {
        return relato;
    }

    public Date getData() {
        return data;
    }

    public String getLocal() {
        return local;
    }
}
