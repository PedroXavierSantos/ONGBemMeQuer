package com.app.bemmequer.data;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Map;

public class RelatorioRepository {
    //Nome da coleção no Firestore
    public static final String COLECAO_RELATORIOS = "relatorios";

    //Atributos da classe
    private final FirebaseFirestore db;

    // Inicializa o Firestore
    public RelatorioRepository() {
        db = FirebaseFirestore.getInstance();
    }



    //Método que grava um relatório novo (usado pelo funcionário)
    public void salvarRelatorio(Relatorio relatorio, OnCompleteListener<DocumentReference> listener) {
        Map<String, Object> dados = relatorio.paraDocumento();
        dados.put(Relatorio.CAMPO_CRIADO_EM, FieldValue.serverTimestamp());
        db.collection(COLECAO_RELATORIOS)
                .add(dados)
                .addOnCompleteListener(listener);
    }

    //Método que lista os relatórios de um paciente, do mais recente para o mais antigo.
    //O filtro por pacienteUid é obrigatório: as regras só deixam a família ler os próprios relatórios.
    public void listarRelatoriosDoPaciente(String pacienteUid, OnCompleteListener<QuerySnapshot> listener) {
        db.collection(COLECAO_RELATORIOS)
                .whereEqualTo(Relatorio.CAMPO_PACIENTE_UID, pacienteUid)
                .orderBy(Relatorio.CAMPO_DATA, Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(listener);
    }
}
