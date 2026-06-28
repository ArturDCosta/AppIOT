package com.example.monitorforno.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.monitorforno.R;
import com.example.monitorforno.models.EsqueciSenhaDTO;
import com.example.monitorforno.models.LoginRequestDTO;
import com.example.monitorforno.models.LoginResponseDTO;
import com.example.monitorforno.network.RetrofitClient;
import com.example.monitorforno.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import okhttp3.ResponseBody;
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

        // Clique no Esqueci a Senha
        txtEsqueciSenha.setOnClickListener(v -> abrirDialogEsqueciSenha());

        txtCadastro.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, CadastroActivity.class);
            startActivity(intent);
        });
    }

    private void tentarLogin() {
        String email = edtEmail.getText() != null ? edtEmail.getText().toString().trim() : "";
        String senha = edtSenha.getText() != null ? edtSenha.getText().toString().trim() : "";

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

        btnEntrar.setEnabled(false);
        btnEntrar.setText("Entrando...");

        LoginRequestDTO dto = new LoginRequestDTO(email, senha);

        RetrofitClient.getApiService(this).login(dto).enqueue(new Callback<LoginResponseDTO>() {
            @Override
            public void onResponse(Call<LoginResponseDTO> call, Response<LoginResponseDTO> response) {
                btnEntrar.setEnabled(true);
                btnEntrar.setText("Entrar");

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponseDTO dados = response.body();
                    // Mantive exatamente como estava no seu código original:
                    sessionManager.salvarSessao(dados.getToken(), dados.getId());

                    Toast.makeText(LoginActivity.this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show();
                    irParaMain();
                } else {
                    // --- TRATAMENTO CORRETO DOS ERROS VIA CÓDIGO HTTP ---
                    int codigoErro = response.code();
                    Log.e("Login", "Erro da API. Código: " + codigoErro);

                    if (codigoErro == 401) {
                        Toast.makeText(LoginActivity.this, "Senha incorreta. Verifique os dados e tente novamente.", Toast.LENGTH_LONG).show();
                    } else if (codigoErro == 404) {
                        Toast.makeText(LoginActivity.this, "Esta conta não está cadastrada. Verifique o e-mail.", Toast.LENGTH_LONG).show();
                    } else if (codigoErro == 400) {
                        Toast.makeText(LoginActivity.this, "Dados inválidos. Verifique o formato do e-mail.", Toast.LENGTH_SHORT).show();
                    } else if (codigoErro == 500) {
                        Toast.makeText(LoginActivity.this, "O servidor está temporariamente indisponível. Tente novamente mais tarde.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "Não foi possível realizar o login (Erro " + codigoErro + ").", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<LoginResponseDTO> call, Throwable t) {
                btnEntrar.setEnabled(true);
                btnEntrar.setText("Entrar");
                Log.e("Login", "Falha física de rede: " + t.getMessage());

                Toast.makeText(LoginActivity.this, "Falha na conexão. Verifique sua internet ou tente novamente.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void irParaMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // --- MÉTODOS DO ESQUECI A SENHA ---

    private void abrirDialogEsqueciSenha() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Recuperar Senha");
        builder.setMessage("Informe o e-mail cadastrado. Enviaremos um código (token) para redefinição.");

        final android.widget.EditText inputEmail = new android.widget.EditText(this);
        inputEmail.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        inputEmail.setHint("Digite seu e-mail");

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(60, 20, 60, 0);
        layout.addView(inputEmail);
        builder.setView(layout);

        builder.setPositiveButton("Enviar Código", (dialog, which) -> {
            String email = inputEmail.getText().toString().trim();
            if (!email.isEmpty()) {
                chamarApiRecuperacao(email);
            } else {
                Toast.makeText(this, "Por favor, insira um e-mail válido.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void chamarApiRecuperacao(String email) {
        EsqueciSenhaDTO dto = new EsqueciSenhaDTO(email);

        RetrofitClient.getApiService(this).solicitarRecuperacaoSenha(dto).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "E-mail enviado! Verifique sua caixa de entrada.", Toast.LENGTH_LONG).show();
                    // Redireciona para a tela de colar o token
                    Intent intent = new Intent(LoginActivity.this, RedefinirSenhaActivity.class);
                    startActivity(intent);
                } else {
                    Log.e("RecuperarSenha", "Erro da API: " + response.code());
                    if (response.code() == 500) {
                        Toast.makeText(LoginActivity.this, "Erro no servidor de E-mail (Backend SMTP).", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "Erro. Verifique se o e-mail está cadastrado.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Sem conexão com o servidor.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}