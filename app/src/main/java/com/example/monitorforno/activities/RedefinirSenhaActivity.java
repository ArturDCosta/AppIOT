package com.example.monitorforno.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.monitorforno.R;
import com.example.monitorforno.models.NovaSenhaDTO;
import com.example.monitorforno.network.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RedefinirSenhaActivity extends AppCompatActivity {

    private TextInputEditText edtToken, edtNovaSenha;
    private MaterialButton btnSalvarNovaSenha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redefinir_senha);

        // Inicializar Views
        edtToken = findViewById(R.id.edtToken);
        edtNovaSenha = findViewById(R.id.edtNovaSenha);
        btnSalvarNovaSenha = findViewById(R.id.btnSalvarNovaSenha);

        // --- CÓDIGO DO DEEP LINK ---
        // Captura a Intent que abriu a Activity (seja o link ou a navegação normal do app)
        Intent intent = getIntent();
        if (intent != null && intent.getData() != null) {
            Uri data = intent.getData();

            // Extrai o parâmetro "token" da URL (ex: ?token=b35ecca4...)
            String tokenExtraido = data.getQueryParameter("token");

            if (tokenExtraido != null && !tokenExtraido.isEmpty()) {
                edtToken.setText(tokenExtraido); // Preenche o campo automaticamente
                edtNovaSenha.requestFocus(); // Move o cursor direto para a nova senha
                Toast.makeText(this, "Token preenchido automaticamente!", Toast.LENGTH_SHORT).show();
            }
        }
        // ----------------------------

        // Configuração do clique do botão
        btnSalvarNovaSenha.setOnClickListener(v -> executarNovaSenha());
    }

    //validar campos
    private void executarNovaSenha() {
        String token = edtToken.getText() != null ? edtToken.getText().toString().trim() : "";
        String novaSenha = edtNovaSenha.getText() != null ? edtNovaSenha.getText().toString().trim() : "";
        String regexSenha = "^(?=.*[A-Z])(?=.*\\d).{8,}$";

        // Nova Validação Requisitada: Mínimo 8 caracteres, pelo menos 1 maiúscula e 1 número
        if (novaSenha.isEmpty() || !novaSenha.matches(regexSenha)) {
            edtNovaSenha.setError("A senha deve conter 8 caracteres, 1 letra maiúscula e 1 número.");
            edtNovaSenha.requestFocus();
            return;
        }

        // Se passou em todas as validações, envia os dados para a API
        enviarNovaSenhaParaApi(token, novaSenha);
    }

    /**
     * Realiza a comunicação de rede via Retrofit para salvar a nova senha.
     */
    private void enviarNovaSenhaParaApi(String token, String novaSenha) {
        btnSalvarNovaSenha.setEnabled(false);
        NovaSenhaDTO dto = new NovaSenhaDTO(token, novaSenha);

        RetrofitClient.getApiService(this).redefinirSenha(dto).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(RedefinirSenhaActivity.this, "Senha alterada com sucesso! Faça login.", Toast.LENGTH_LONG).show();

                    // Cria a intenção de ir para o Login
                    Intent intent = new Intent(RedefinirSenhaActivity.this, LoginActivity.class);

                    // Limpa a pilha de navegação para não deixar telas fantasmas abertas no fundo
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    // Inicia a tela de Login e encerra a atual
                    startActivity(intent);
                    finish();
                } else {
                    btnSalvarNovaSenha.setEnabled(true);
                    Toast.makeText(RedefinirSenhaActivity.this, "Erro. Verifique se o token está correto e não expirou.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                btnSalvarNovaSenha.setEnabled(true);
                Toast.makeText(RedefinirSenhaActivity.this, "Sem conexão com o servidor.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}