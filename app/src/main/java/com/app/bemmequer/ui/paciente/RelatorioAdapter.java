package com.app.bemmequer.ui.paciente;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.app.bemmequer.R;
import com.app.bemmequer.data.Relatorio;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

//Adapter: a "ponte" entre a lista de relatórios e a RecyclerView.
//A RecyclerView só cria os cartões que cabem na tela e os reaproveita ao rolar.
//É usado em dois lugares: na lista da família (azul) e em "Meus relatórios" do funcionário (roxo).
public class RelatorioAdapter extends RecyclerView.Adapter<RelatorioAdapter.CartaoRelatorio> {
    //Quem quer ser avisado quando a pessoa toca em "Compartilhar PDF" num cartão (a Activity)
    public interface AoCompartilhar {
        void compartilhar(Relatorio relatorio);
    }

    private final List<Relatorio> relatorios;
    //true na tela do funcionário: mostra o nome do PACIENTE (o autor é ele mesmo) e usa as cores roxas
    private final boolean modoFuncionario;
    //Ação do botão "Compartilhar PDF". Se for null, o botão não aparece no cartão.
    private final AoCompartilhar aoCompartilhar;

    //Lista sem o botão "Compartilhar PDF"
    public RelatorioAdapter(List<Relatorio> relatorios, boolean modoFuncionario) {
        this(relatorios, modoFuncionario, null);
    }

    public RelatorioAdapter(List<Relatorio> relatorios, boolean modoFuncionario, AoCompartilhar aoCompartilhar) {
        this.relatorios = relatorios;
        this.modoFuncionario = modoFuncionario;
        this.aoCompartilhar = aoCompartilhar;
    }

    //Cria um cartão vazio (chamado só algumas vezes, os cartões são reaproveitados)
    @NonNull
    @Override
    public CartaoRelatorio onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View cartao = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_relatorio, parent, false);
        if (modoFuncionario) {
            ((MaterialCardView) cartao).setCardBackgroundColor(
                    ContextCompat.getColor(parent.getContext(), R.color.roxo_claro_bem_me_quer));
            ((TextView) cartao.findViewById(R.id.item_relatorio_data)).setTextColor(
                    ContextCompat.getColor(parent.getContext(), R.color.roxo_bem_me_quer));
            MaterialButton compartilhar = cartao.findViewById(R.id.item_relatorio_compartilhar);
            ColorStateList roxo = ColorStateList.valueOf(
                    ContextCompat.getColor(parent.getContext(), R.color.roxo_bem_me_quer));
            compartilhar.setTextColor(roxo);
            compartilhar.setIconTint(roxo);
        }
        return new CartaoRelatorio(cartao);
    }

    //Preenche um cartão com os dados do relatório daquela posição
    @Override
    public void onBindViewHolder(@NonNull CartaoRelatorio cartao, int posicao) {
        Relatorio relatorio = relatorios.get(posicao);
        cartao.data.setText(relatorio.dataPorExtenso());
        String pessoa = modoFuncionario ? relatorio.getNomePaciente() : relatorio.getNomeFuncionario();
        cartao.autor.setText(cartao.itemView.getContext().getString(R.string.relatorioAutorLocal,
                pessoa, relatorio.getLocal()));
        cartao.relato.setText(relatorio.getRelato());
        //O cartão é reaproveitado ao rolar a lista, por isso o clique é religado ao relatório desta posição
        if (aoCompartilhar == null) {
            cartao.compartilhar.setVisibility(View.GONE);
        } else {
            cartao.compartilhar.setVisibility(View.VISIBLE);
            cartao.compartilhar.setOnClickListener(v -> aoCompartilhar.compartilhar(relatorio));
        }
    }

    @Override
    public int getItemCount() {
        return relatorios.size();
    }

    //Guarda as referências dos componentes de um cartão (evita chamar findViewById toda hora)
    static class CartaoRelatorio extends RecyclerView.ViewHolder {
        final TextView data, autor, relato;
        final MaterialButton compartilhar;

        CartaoRelatorio(@NonNull View itemView) {
            super(itemView);
            data = itemView.findViewById(R.id.item_relatorio_data);
            autor = itemView.findViewById(R.id.item_relatorio_autor);
            relato = itemView.findViewById(R.id.item_relatorio_relato);
            compartilhar = itemView.findViewById(R.id.item_relatorio_compartilhar);
        }
    }
}
