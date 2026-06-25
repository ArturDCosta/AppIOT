package com.example.monitorforno.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.monitorforno.R;
import com.example.monitorforno.models.LoginRequestDTO;
import com.example.monitorforno.models.LoginResponseDTO;
import com.example.monitorforno.network.RetrofitClient;
import com.example.monitorforno.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText edtEmail;
    private TextInputEditText edtSenha;
    private MaterialButton btnEntrar;
    private TextView txtEsqueciSenha;
    private TextView txtCadastro;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);

        // Se já está logado, vai direto para MainActivity
        if (sessionManager.estaLogado()) {
            irParaMain();
            return;
        }

        edtEmail       = findViewById(R.id.edtEmail);
        edtSenha       = findViewById(R.id.edtSenha);
        btnEntrar      = findViewById(R.id.btnEntrar);
        txtEsqueciSenha = findViewById(R.id.txtEsqueciSenha);
        txtCadastro    = findViewById(R.id.txtCadastro);

        btnEntrar.setOnClickListener(v -> tentarLogin());

        txtEsqueciSenha.setOnClickListener(v -> {
            // Implementar depois
            Toast.makeText(this, "Em breve", Toast.LENGTH_SHORT).show();
        });

        txtCadastro.setOnClickListener(v -> {
            // Implementar depois
            Intent intent = new Intent(LoginActivity.this, CadastroActivity.class);
            startActivity(intent);
        });
    }

    private void tentarLogin() {
        String email = edtEmail.getText() != null
                ? edtEmail.getText().toString().trim()
                : "";

        String senha = edtSenha.getText() != null
                ? edtSenha.getText().toString().trim()
                : "";

        // Validação local — antes de bater na API
        if (email.isEmpty()) {
            edtEmail.setError("Informe o e-mail");
            edtEmail.requestFocus();
            return;
        }

        if (senha.isEmpty()) {
            edtSenha.setError("Informe a senha");
            edtSenha.requestFocus();
            return;
        }

        // Desabilita botão para evitar cliques duplos
        btnEntrar.setEnabled(false);
        btnEntrar.setText("Entrando...");

        LoginRequestDTO dto = new LoginRequestDTO(email, senha);

        RetrofitClient.getApiService(this)
                .login(dto)
                .enqueue(new Callback<LoginResponseDTO>() {

                    @Override
                    public void onResponse(Call<LoginResponseDTO> call,
                                           Response<LoginResponseDTO> response) {

                        // Reabilita botão independente do resultado
                        btnEntrar.setEnabled(true);
                        btnEntrar.setText("Entrar");

                        if (response.isSuccessful() && response.body() != null) {

                            LoginResponseDTO dados = response.body();

                            // Salva token e ID do usuário
                            sessionManager.salvarSessao(
                                    dados.getToken(),
                                    dados.getId()
                            );

                            irParaMain();

                        } else if (response.code() == 401) {
                            Toast.makeText(
                                    LoginActivity.this,
                                    "E-mail ou senha incorretos",
                                    Toast.LENGTH_SHORT
                            ).show();

                        } else {
                            Log.e("Login", "Erro inesperado: " + response.code());
                            Toast.makeText(
                                    LoginActivity.this,
                                    "Erro ao conectar. Tente novamente.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<LoginResponseDTO> call, Throwable t) {
                        btnEntrar.setEnabled(true);
                        btnEntrar.setText("Entrar");

                        Log.e("Login", "Falha: " + t.getMessage());

                        Toast.makeText(
                                LoginActivity.this,
                                "Sem conexão com o servidor",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void irParaMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(
                // Limpa a pilha — usuário não volta para o login com botão voltar
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );
        startActivity(intent);
        finish();
    }
}