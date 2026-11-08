package com.app.bemmequer.data;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Acesso ao ponto do funcionário no Firestore: pontos/{uid}/registros/{dia}_{tipo}
public class PontoRepository {
    //Nomes da coleção e dos campos (os mesmos usados nas regras de segurança)
    public static final String COLECAO_PONTOS = "pontos";
    public static final String SUBCOLECAO_REGISTROS = "registros";
    public static final String CAMPO_TIPO = "tipo";
    public static final String CAMPO_DIA = "dia";
    public static final String CAMPO_HORARIO = "horario";

    //Atributos da classe
    private final FirebaseFirestore db;

    // Inicializa o Firestore
    public PontoRepository() {
        db = FirebaseFirestore.getInstance();
    }

    private CollectionReference registros(String uid) {
        return db.collection(COLECAO_PONTOS).document(uid).collection(SUBCOLECAO_REGISTROS);
    }

    //Método que bate o ponto. O ID "dia_tipo" garante um registro de cada tipo por dia,
    //e o horário é preenchido pelo servidor (o funcionário não consegue mudar o relógio do celular).
    public void registrar(String uid, String dia, TipoPonto tipo, OnCompleteListener<Void> listener) {
        Map<String, Object> dados = new HashMap<>();
        dados.put(CAMPO_TIPO, tipo.getValor());
        dados.put(CAMPO_DIA, dia);
        dados.put(CAMPO_HORARIO, FieldValue.serverTimestamp());
        registros(uid).document(dia + "_" + tipo.getValor())
                .set(dados)
                .addOnCompleteListener(listener);
    }

    //Método que busca todos os registros a partir de um dia (ex.: os últimos 15 dias e hoje).
    //Como "dia" é AAAA-MM-DD, comparar o texto com >= funciona como comparar datas.
    public void listarDesde(String uid, String diaInicial, OnCompleteListener<QuerySnapshot> listener) {
        registros(uid)
                .whereGreaterThanOrEqualTo(CAMPO_DIA, diaInicial)
                .get()
                .addOnCompleteListener(listener);
    }

    //Método que agrupa os documentos lidos por dia: "2026-11-10" -> registros daquele dia
    public static Map<String, PontoDoDia> agruparPorDia(List<DocumentSnapshot> documentos) {
        Map<String, PontoDoDia> dias = new HashMap<>();
        for (DocumentSnapshot documento : documentos) {
            String dia = documento.getString(CAMPO_DIA);
            TipoPonto tipo = TipoPonto.deValor(documento.getString(CAMPO_TIPO));
            Timestamp horario = documento.getTimestamp(CAMPO_HORARIO);
            if (dia == null || tipo == null || horario == null) {
                continue; //Ignora documento incompleto em vez de travar a tela
            }
            PontoDoDia ponto = dias.get(dia);
            if (ponto == null) {
                ponto = new PontoDoDia();
                dias.put(dia, ponto);
            }
            ponto.registrar(tipo, horario.toDate());
        }
        return dias;
    }
}
