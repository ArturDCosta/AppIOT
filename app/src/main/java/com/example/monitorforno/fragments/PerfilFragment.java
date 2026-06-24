package com.example.monitorforno.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.monitorforno.R;
import com.example.monitorforno.activities.LoginActivity;
// Lembre-se de importar suas classes corretas de API aqui!
import com.example.monitorforno.models.ApiService;
import com.example.monitorforno.network.RetrofitClient;
import com.example.monitorforno.models.PerfilDTO;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerfilFragment extends Fragment {

    private TextView txtNomePerfil, txtEmailPerfil, txtNome, txtEmail, txtNascimento;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        // 1. Mapear os IDs da tela
        txtNomePerfil = view.findViewById(R.id.txtNomePerfil);
        txtEmailPerfil = view.findViewById(R.id.txtEmailPerfil);
        txtNome = view.findViewById(R.id.txtNome);
        txtEmail = view.findViewById(R.id.txtEmail);
        txtNascimento = view.findViewById(R.id.txtNascimento);

        MaterialButton btnAlterarSenha = view.findViewById(R.id.btnAlterarSenha);
        MaterialButton btnLogout = view.findViewById(R.id.btnLogout);

        // Limpar os textos genéricos ("Rafael Fiorio") e colocar estado de carregamento
        limparTextos();

        // 2. Chamar a API
        buscarPerfilNaApi();

        // 3. Ações dos botões
        btnAlterarSenha.setOnClickListener(v ->
                Toast.makeText(getContext(), "Em desenvolvimento", Toast.LENGTH_SHORT).show()
        );

        btnLogout.setOnClickListener(v -> {
            // DICA: Adicione aqui a lógica para apagar o Token salvo no Android (SharedPreferences)
            // antes de mandar o usuário para a LoginActivity!

            Intent intent = new Intent(getActivity(), LoginActivity.class);
            // Isso impede que o usuário volte para o perfil apertando o botão "Voltar" do celular
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        return view;
    }

    private void buscarPerfilNaApi() {
        // Inicializa o Retrofit
        ApiService apiService = RetrofitClient.getApiService(requireContext());

        apiService.getMeuPerfil().enqueue(new Callback<PerfilDTO>() {
            @Override
            public void onResponse(Call<PerfilDTO> call, Response<PerfilDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PerfilDTO perfil = response.body();

                    txtNomePerfil.setText(perfil.getNome());
                    txtEmailPerfil.setText(perfil.getEmail());
                    txtNome.setText(perfil.getNome());
                    txtEmail.setText(perfil.getEmail());

                    // Formatador rápido para converter YYYY-MM-DD para DD/MM/YYYY
                    String dataDoBackend = perfil.getNascimento();
                    if (dataDoBackend != null && dataDoBackend.contains("-")) {
                        String[] partes = dataDoBackend.split("-");
                        String dataFormatada = partes[2] + "/" + partes[1] + "/" + partes[0];
                        txtNascimento.setText(dataFormatada); // Vai exibir: 01/01/2000
                    } else {
                        txtNascimento.setText(dataDoBackend);
                    }
                } else {
                    Log.e("API_PERFIL", "Erro da API: " + response.code());
                    Toast.makeText(getContext(), "Erro ao carregar perfil.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PerfilDTO> call, Throwable t) {
                Log.e("API_PERFIL", "Falha na comunicação: " + t.getMessage());
                Toast.makeText(getContext(), "Erro de conexão com servidor.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void limparTextos() {
        txtNomePerfil.setText("Carregando...");
        txtEmailPerfil.setText("...");
        txtNome.setText("...");
        txtEmail.setText("...");
        txtNascimento.setText("...");
    }
}