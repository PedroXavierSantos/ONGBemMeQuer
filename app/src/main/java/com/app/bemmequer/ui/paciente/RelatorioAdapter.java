package com.app.bemmequer.ui.paciente;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.bemmequer.R;
import com.app.bemmequer.data.Relatorio;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

//Adapter: a "ponte" entre a lista de relatórios e a RecyclerView.
//A RecyclerView só cria os cartões que cabem na tela e os reaproveita ao rolar.
public class RelatorioAdapter extends RecyclerView.Adapter<RelatorioAdapter.CartaoRelatorio> {
    //Data por extenso no topo do cartão (ex.: quinta-feira, 30 de outubro de 2026)
    private static final SimpleDateFormat FORMATO_DATA =
            new SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", new Locale("pt", "BR"));

    private final List<Relatorio> relatorios;

    public RelatorioAdapter(List<Relatorio> relatorios) {
        this.relatorios = relatorios;
    }

    //Cria um cartão vazio (chamado só algumas vezes, os cartões são reaproveitados)
    @NonNull
    @Override
    public CartaoRelatorio onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View cartao = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_relatorio, parent, false);
        return new CartaoRelatorio(cartao);
    }

    //Preenche um cartão com os dados do relatório daquela posição
    @Override
    public void onBindViewHolder(@NonNull CartaoRelatorio cartao, int posicao) {
        Relatorio relatorio = relatorios.get(posicao);
        String data = relatorio.getData() == null ? "" : FORMATO_DATA.format(relatorio.getData());
        //Deixa a primeira letra maiúscula (o Java escreve "quinta-feira")
        cartao.data.setText(data.isEmpty() ? data : Character.toUpperCase(data.charAt(0)) + data.substring(1));
        cartao.autor.setText(cartao.itemView.getContext().getString(R.string.relatorioAutorLocal,
                relatorio.getNomeFuncionario(), relatorio.getLocal()));
        cartao.relato.setText(relatorio.getRelato());
    }

    @Override
    public int getItemCount() {
        return relatorios.size();
    }

    //Guarda as referências dos componentes de um cartão (evita chamar findViewById toda hora)
    static class CartaoRelatorio extends RecyclerView.ViewHolder {
        final TextView data, autor, relato;

        CartaoRelatorio(@NonNull View itemView) {
            super(itemView);
            data = itemView.findViewById(R.id.item_relatorio_data);
            autor = itemView.findViewById(R.id.item_relatorio_autor);
            relato = itemView.findViewById(R.id.item_relatorio_relato);
        }
    }
}
