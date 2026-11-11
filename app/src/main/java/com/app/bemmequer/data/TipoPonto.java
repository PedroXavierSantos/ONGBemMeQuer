package com.app.bemmequer.data;

//Os seis registros de ponto de um dia, na ordem em que acontecem.
//O "valor" é o texto gravado no Firestore (o mesmo aceito pelas regras de segurança).
public enum TipoPonto {
    ENTRADA("entrada", null),
    ALMOCO_INICIO("almoco_inicio", ENTRADA),
    ALMOCO_FIM("almoco_fim", ALMOCO_INICIO),
    INTERVALO_INICIO("intervalo_inicio", ENTRADA),
    INTERVALO_FIM("intervalo_fim", INTERVALO_INICIO),
    SAIDA("saida", ENTRADA);

    private final String valor;
    //Registro que precisa existir antes deste (ex.: só finaliza o almoço depois de iniciar)
    private final TipoPonto requisito;

    TipoPonto(String valor, TipoPonto requisito) {
        this.valor = valor;
        this.requisito = requisito;
    }

    public String getValor() {
        return valor;
    }

    public TipoPonto getRequisito() {
        return requisito;
    }

    //Método que converte o texto do Firestore de volta para o tipo (null se for desconhecido)
    public static TipoPonto deValor(String valor) {
        for (TipoPonto tipo : values()) {
            if (tipo.valor.equals(valor)) {
                return tipo;
            }
        }
        return null;
    }
}
