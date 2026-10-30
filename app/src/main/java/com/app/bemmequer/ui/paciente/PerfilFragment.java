package com.app.bemmequer.ui.paciente;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.app.bemmequer.R;
import com.app.bemmequer.auth.AuthManager;
import com.app.bemmequer.data.UsuarioRepository;
import com.app.bemmequer.ui.loginActivity;
import com.google.firebase.firestore.DocumentSnapshot;

//Aba Perfil do paciente: mostra os dados do cadastro e o botão de Sair
public class PerfilFragment extends Fragment {
    //DECLARAÇÃO DAS VARIÁVEIS (COMPONENTES DA TELA)
    private TextView perfilNome, perfilEmail, perfilTelefone, perfilPaciente;
    private AuthManager authManager;
    private UsuarioRepository usuarioRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        inicializarComponentes(view);
        configurarListeners(view);
        carregarPerfil();
    }


    //MÉTODOS DE ORGANIZAÇÃO
    //Método que inicializa as variáveis, conectando com os IDs do layout XML
    private void inicializarComponentes(View view) {
        perfilNome = view.findViewById(R.id.perfil_nome);
        perfilEmail = view.findViewById(R.id.perfil_email);
        perfilTelefone = view.findViewById(R.id.perfil_telefone);
        perfilPaciente = view.findViewById(R.id.perfil_paciente);
        authManager = new AuthManager();
        usuarioRepository = new UsuarioRepository();
    }

    //Método que configura todas as ações de clique
    private void configurarListeners(View view) {
        //O acompanhamento (relatórios do paciente) será a próxima etapa
        view.findViewById(R.id.btn_acompanhar_evolucao).setOnClickListener(v ->
                Toast.makeText(requireContext(), R.string.emBreve, Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.btn_sair).setOnClickListener(v -> sair());
    }


    //MÉTODOS DE AÇÃO
    //Método que busca o perfil do usuário logado (usuarios/{uid}) e preenche a tela
    private void carregarPerfil() {
        String uid = authManager.getUid();
        if (uid == null) {
            return; //A tela principal já cuida de voltar para o login
        }
        usuarioRepository.buscarUsuario(uid, task -> {
            //A resposta pode chegar depois que a pessoa já saiu desta aba
            if (!isAdded()) {
                return;
            }
            if (!task.isSuccessful() || !task.getResult().exists()) {
                Toast.makeText(requireContext(), R.string.erroCarregarPerfil, Toast.LENGTH_SHORT).show();
                return;
            }
            preencherTela(task.getResult());
        });
    }

    //Método que mostra os dados do documento do Firestore nos campos da tela
    private void preencherTela(DocumentSnapshot perfil) {
        perfilNome.setText(textoOuTraco(perfil.getString("nomeResponsavel")));
        perfilEmail.setText(textoOuTraco(perfil.getString("email")));
        perfilTelefone.setText(textoOuTraco(perfil.getString("telefone")));
        perfilPaciente.setText(textoOuTraco(perfil.getString("nomePaciente")));
    }

    //Método auxiliar: mostra um traço quando o campo não foi preenchido
    private String textoOuTraco(String valor) {
        return (valor == null || valor.isEmpty()) ? "—" : valor;
    }

    //Método que desloga e volta para o login, limpando a pilha de telas
    private void sair() {
        authManager.logout();
        Intent intent = new Intent(requireContext(), loginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}
