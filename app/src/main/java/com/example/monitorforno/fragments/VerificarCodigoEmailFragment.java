package com.example.monitorforno.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.monitorforno.R;
import com.example.monitorforno.activities.LoginActivity;
import com.example.monitorforno.models.ApiService;
import com.example.monitorforno.models.ConfirmarTrocaEmailDTO;
import com.example.monitorforno.network.RetrofitClient;
import com.example.monitorforno.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerificarCodigoEmailFragment extends Fragment {

    private TextInputEditText edtCodigo;
    private MaterialButton btnConfirmar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Crie o arquivo res/layout/fragment_verificar_codigo_email.xml seguindo o estilo do app
        View view = inflater.inflate(R.layout.fragment_verificar_codigo_email, container, false);

        edtCodigo = view.findViewById(R.id.edtCodigoVerificacao);
        btnConfirmar = view.findViewById(R.id.btnConfirmarCodigoEmail);

        btnConfirmar.setOnClickListener(v -> validarCodigo());

        return view;
    }

    private void validarCodigo() {
        String codigo = edtCodigo.getText() != null ? edtCodigo.getText().toString().trim() : "";

        if (codigo.isEmpty()) {
            edtCodigo.setError("Insira o código recebido no e-mail.");
            return;
        }

        btnConfirmar.setEnabled(false);
        btnConfirmar.setText("Verificando...");

        ApiService apiService = RetrofitClient.getApiService(requireContext());
        ConfirmarTrocaEmailDTO dto = new ConfirmarTrocaEmailDTO(codigo);

        apiService.confirmarTrocaEmail(dto).enqueue(new Callback<okhttp3.ResponseBody>() {
            @Override
            public void onResponse(Call<okhttp3.ResponseBody> call, Response<okhttp3.ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "E-mail alterado com sucesso! Faça login com o novo e-mail.", Toast.LENGTH_LONG).show();

                    // Limpa a sessão para obrigar login com o novo email
                    SessionManager sessionManager = new SessionManager(requireContext());
                    sessionManager.limparSessao();

                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                    if (getActivity() != null) {
                        getActivity().finish();
                    }
                } else {
                    btnConfirmar.setEnabled(true);
                    btnConfirmar.setText("Confirmar Código");
                    Toast.makeText(getContext(), "Código inválido ou expirado.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) {
                btnConfirmar.setEnabled(true);
                btnConfirmar.setText("Confirmar Código");
                Toast.makeText(getContext(), "Sem conexão com o servidor.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}