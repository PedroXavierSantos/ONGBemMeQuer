package com.app.bemmequer.ui.paciente;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

import com.app.bemmequer.R;
import com.app.bemmequer.ui.mainPacienteActivity;

//Aba Início do paciente: mostra os serviços da ONG e o convite para doação
public class InicioFragment extends Fragment {

    //Serviços exibidos na tela (nome + ícone). Para incluir um serviço novo, basta adicionar aqui.
    private static final int[][] SERVICOS = {
            {R.string.servicoFisioterapia, R.drawable.ic_servico_fisioterapia},
            {R.string.servicoPedagogia, R.drawable.ic_servico_pedagogia},
            {R.string.servicoAnimais, R.drawable.ic_servico_animais},
            {R.string.servicoPsicologia, R.drawable.ic_servico_psicologia},
            {R.string.servicoArtes, R.drawable.ic_servico_artes},
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_inicio, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        GridLayout gradeServicos = view.findViewById(R.id.grade_servicos);
        for (int[] servico : SERVICOS) {
            adicionarCartaoServico(gradeServicos, servico[0], servico[1]);
        }

        //O botão de doação leva para a aba Doações da barra inferior
        view.findViewById(R.id.btn_fazer_doacao).setOnClickListener(v -> abrirAba(R.id.nav_doacoes));
    }

    //Método que cria o cartão de um serviço e coloca na grade de 2 colunas
    private void adicionarCartaoServico(GridLayout grade, @StringRes int nome, @DrawableRes int icone) {
        View cartao = getLayoutInflater().inflate(R.layout.item_servico, grade, false);
        ((ImageView) cartao.findViewById(R.id.icone_servico)).setImageResource(icone);
        ((TextView) cartao.findViewById(R.id.nome_servico)).setText(nome);

        //Cada cartão ocupa uma coluna e divide o espaço igualmente com o vizinho
        GridLayout.LayoutParams parametros = new GridLayout.LayoutParams(
                GridLayout.spec(GridLayout.UNDEFINED),
                GridLayout.spec(GridLayout.UNDEFINED, 1f));
        parametros.width = 0;
        ViewGroup.MarginLayoutParams margens = (ViewGroup.MarginLayoutParams) cartao.getLayoutParams();
        parametros.setMargins(margens.leftMargin, margens.topMargin, margens.rightMargin, margens.bottomMargin);

        //Ao tocar num serviço, abre a Agenda para marcar um atendimento
        cartao.setOnClickListener(v -> abrirAba(R.id.nav_agenda));
        grade.addView(cartao, parametros);
    }

    //Método que pede para a tela principal trocar de aba
    private void abrirAba(int idAba) {
        if (getActivity() instanceof mainPacienteActivity) {
            ((mainPacienteActivity) getActivity()).abrirAba(idAba);
        }
    }
}
