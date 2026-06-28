package com.example.monitorforno.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.monitorforno.R;
import com.example.monitorforno.models.UserRequestDTO;
import com.example.monitorforno.network.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CadastroActivity extends AppCompatActivity {

    private TextInputEditText edtNome, edtEmail, edtNascimento, edtSenha;
    private MaterialButton btnCadastrar;
    private TextView txtVoltarLogin;

    // Variável oculta para salvar a data no formato correto do banco (YYYY-MM-DD)
    private String dataFormatadaParaApi = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        // Inicializar Views
        edtNome = findViewById(R.id.edtNomeCadastro);
        edtEmail = findViewById(R.id.edtEmailCadastro);
        edtNascimento = findViewById(R.id.edtNascimentoCadastro);
        edtSenha = findViewById(R.id.edtSenhaCadastro);
        btnCadastrar = findViewById(R.id.btnCadastrar);
        txtVoltarLogin = findViewById(R.id.txtVoltarLogin);

        // Configurar clique para abrir o Seletor de Data Nativo
        edtNascimento.setOnClickListener(v -> abrirSeletorData());

        // Configurar botão de cadastro
        btnCadastrar.setOnClickListener(v -> executarCadastro());

        // Botão voltar para a tela de login
        txtVoltarLogin.setOnClickListener(v -> finish());
    }

    private void abrirSeletorData() {
        final Calendar c = Calendar.getInstance();
        int ano = c.get(Calendar.YEAR);
        int mes = c.get(Calendar.MONTH);
        int dia = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, monthOfYear, dayOfMonth) -> {
                    // Exibe de forma amigável na tela do celular: DD/MM/YYYY
                    String dataExibicao = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, (monthOfYear + 1), year);
                    edtNascimento.setText(dataExibicao);

                    // Guarda no formato que o Spring Boot exige: YYYY-MM-DD
                    dataFormatadaParaApi = String.format(Locale.getDefault(), "%d-%02d-%02d", year, (monthOfYear + 1), dayOfMonth);
                }, ano, mes, dia);
        datePickerDialog.show();
    }

    private void executarCadastro() {
        String nome = edtNome.getText() != null ? edtNome.getText().toString().trim() : "";
        String email = edtEmail.getText() != null ? edtEmail.getText().toString().trim() : "";
        String senha = edtSenha.getText() != null ? edtSenha.getText().toString().trim() : "";
        String regexSenha = "^(?=.*[A-Z])(?=.*\\d).{8,}$";

        // Validações básicas locais
        if (nome.isEmpty()) {
            edtNome.setError("Informe seu nome completo");
            edtNome.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            edtEmail.setError("Informe o e-mail");
            edtEmail.requestFocus();
            return;
        }
        if (dataFormatadaParaApi.isEmpty()) {
            edtNascimento.setError("Selecione sua data de nascimento");
            Toast.makeText(this, "Selecione a data de nascimento", Toast.LENGTH_SHORT).show();
            return;
        }
        if (senha.isEmpty() || !senha.matches(regexSenha) ) {
            edtSenha.setError("A senha deve conter 8 caracteres, 1 letra maiúscula e 1 número.");
            edtSenha.requestFocus();
            return;
        }

        // Bloqueia o botão durante o processamento
        btnCadastrar.setEnabled(false);
        btnCadastrar.setText("Cadastrando...");

        UserRequestDTO dto = new UserRequestDTO(nome, email, dataFormatadaParaApi, senha);

        // Dispara a chamada na API usando a mesma estrutura da LoginActivity
        RetrofitClient.getApiService(this)
                .cadastrarUsuario(dto)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        btnCadastrar.setEnabled(true);
                        btnCadastrar.setText("Cadastrar");

                        if (response.isSuccessful()) {
                            Toast.makeText(CadastroActivity.this, "Conta criada com sucesso!", Toast.LENGTH_LONG).show();
                            // Fecha a tela de cadastro e volta automaticamente para o Login
                            finish();
                        } else if (response.code() == 409) {
                            // Código HTTP comum para conflito (ex: Email já cadastrado)
                            Toast.makeText(CadastroActivity.this, "Este e-mail já está em uso.", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e("CadastroAPI", "Erro inesperado: " + response.code());
                            Toast.makeText(CadastroActivity.this, "Erro ao realizar cadastro. Verifique os dados.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        btnCadastrar.setEnabled(true);
                        btnCadastrar.setText("Cadastrar");
                        Log.e("CadastroAPI", "Falha de rede: " + t.getMessage());
                        Toast.makeText(CadastroActivity.this, "Sem conexão com o servidor", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}