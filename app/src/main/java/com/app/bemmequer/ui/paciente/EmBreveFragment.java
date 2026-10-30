package com.app.bemmequer.ui.paciente;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

import com.app.bemmequer.R;

//Tela provisória para as abas que ainda estão em desenvolvimento (mostra o nome e o ícone da aba)
public class EmBreveFragment extends Fragment {
    //Chaves usadas para guardar o título e o ícone nos argumentos do Fragment
    private static final String ARG_TITULO = "titulo";
    private static final String ARG_ICONE = "icone";

    //Método que cria a tela já com o título e o ícone da aba.
    //Os dados vão nos "argumentos" para não se perderem quando o Android recria a tela (ex.: girar o celular)
    public static EmBreveFragment novaInstancia(@StringRes int titulo, @DrawableRes int icone) {
        Bundle argumentos = new Bundle();
        argumentos.putInt(ARG_TITULO, titulo);
        argumentos.putInt(ARG_ICONE, icone);
        EmBreveFragment fragment = new EmBreveFragment();
        fragment.setArguments(argumentos);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_em_breve, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle argumentos = requireArguments();
        ((TextView) view.findViewById(R.id.em_breve_titulo)).setText(argumentos.getInt(ARG_TITULO));
        ((ImageView) view.findViewById(R.id.em_breve_icone)).setImageResource(argumentos.getInt(ARG_ICONE));
    }
}
