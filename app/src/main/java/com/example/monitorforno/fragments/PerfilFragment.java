package com.example.monitorforno.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.monitorforno.R;
import com.example.monitorforno.activities.LoginActivity;
import com.example.monitorforno.models.ApiService;
import com.example.monitorforno.models.FotoPerfilRequestDTO;
import com.example.monitorforno.models.FotoPerfilResponseDTO;
import com.example.monitorforno.models.NovaSenhaLogadoDTO;
import com.example.monitorforno.network.RetrofitClient;
import com.example.monitorforno.models.PerfilDTO;
import com.example.monitorforno.utils.SessionManager;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerfilFragment extends Fragment {

    private TextView txtNomePerfil, txtEmailPerfil, txtNome, txtEmail, txtNascimento;
    private ImageView imgFotoPerfil;
    private View cardIconeEdicao;
    private androidx.activity.result.ActivityResultLauncher<String> galeriaLauncher;
    private boolean usuarioJaPossuiFoto = false; // Controle para saber se chama POST ou PUT

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Registra o contrato para abrir a galeria de imagens de forma moderna
        galeriaLauncher = registerForActivityResult(
                new androidx.activity.result.contract.ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        processarEEnviarFoto(uri);
                    }
                }
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        // 1. Mapear os IDs da tela
        txtNomePerfil = view.findViewById(R.id.txtNomePerfil);
        txtEmailPerfil = view.findViewById(R.id.txtEmailPerfil);
        txtNome = view.findViewById(R.id.txtNome);
        txtEmail = view.findViewById(R.id.txtEmail);
        txtNascimento = view.findViewById(R.id.txtNascimento);

        // CORREÇÃO: Inicializar componentes da foto de perfil
        View layoutFotoPerfil = view.findViewById(R.id.layoutFotoPerfil);
        imgFotoPerfil = view.findViewById(R.id.imgFotoPerfil);
        cardIconeEdicao = view.findViewById(R.id.cardIconeEdicao);

        MaterialButton btnAlterarSenha = view.findViewById(R.id.btnAlterarSenha);
        MaterialButton btnLogout = view.findViewById(R.id.btnLogout);

        // 2. Chamar as APIs para carregar os dados cadastrados
        buscarPerfilNaApi();
        buscarFotoPerfilNaApi(); // CORREÇÃO: Adicionado para trazer a foto ao entrar na tela

        // 3. Ações dos botões e cliques
        btnAlterarSenha.setOnClickListener(v -> exibirDialogAlterarSenha());

        // CORREÇÃO: Vincula o clique no container da foto para abrir a galeria
        if (layoutFotoPerfil != null) {
            layoutFotoPerfil.setOnClickListener(v -> galeriaLauncher.launch("image/*"));
        }

        btnLogout.setOnClickListener(v -> {
            // Chama a classe que gerencia a sessão e pede para limpar
            SessionManager sessionManager = new SessionManager(requireContext());
            sessionManager.limparSessao();

            // Redireciona para o Login
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        return view;
    }

    private void buscarPerfilNaApi() {
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
                        txtNascimento.setText(dataFormatada);
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

    private void exibirDialogAlterarSenha() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_alterar_senha, null);
        builder.setView(dialogView);

        android.app.AlertDialog dialog = builder.create();

        // TRUQUE DE DESIGN: Deixa o fundo padrão do Dialog transparente para as bordas redondas do nosso XML aparecerem!
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        // Mapear os elementos do nosso layout customizado
        com.google.android.material.textfield.TextInputEditText edtSenhaAtual = dialogView.findViewById(R.id.edtSenhaAtual);
        com.google.android.material.textfield.TextInputEditText edtNovaSenha = dialogView.findViewById(R.id.edtNovaSenha);
        com.google.android.material.button.MaterialButton btnCancelar = dialogView.findViewById(R.id.btnCancelar);
        com.google.android.material.button.MaterialButton btnConfirmar = dialogView.findViewById(R.id.btnConfirmar);

        // Ação de cancelar
        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        // Ação de confirmar
        btnConfirmar.setOnClickListener(v -> {
            String senhaAtual = edtSenhaAtual.getText().toString().trim();
            String novaSenha = edtNovaSenha.getText().toString().trim();

            if (senhaAtual.isEmpty() || novaSenha.isEmpty()) {
                Toast.makeText(getContext(), "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isSenhaValida(novaSenha)) {
                Toast.makeText(getContext(), "A nova senha deve ter no mínimo 8 caracteres, 1 letra maiúscula, 1 minúscula e 1 número.", Toast.LENGTH_LONG).show();
                return;
            }

            // Passamos o dialog para poder fechá-lo de forma segura lá dentro
            alterarSenhaNaApi(senhaAtual, novaSenha, dialog);
        });

        // Mostra o dialog
        dialog.show();
    }

    private void alterarSenhaNaApi(String senhaAtual, String novaSenha, android.app.AlertDialog dialog) {
        ApiService apiService = RetrofitClient.getApiService(requireContext());
        NovaSenhaLogadoDTO dto = new NovaSenhaLogadoDTO(novaSenha, senhaAtual);

        apiService.alterarMinhaSenha(dto).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Senha alterada! Por favor, faça login novamente.", Toast.LENGTH_LONG).show();

                    // 1. Fecha o popup antes de mudar de tela (evita o crash WindowLeaked)
                    dialog.dismiss();

                    // 2. Limpa a sessão e desloga o usuário por segurança
                    fazerLogout();
                } else {
                    Toast.makeText(getContext(), "Erro ao alterar. Verifique se a senha atual está correta.", Toast.LENGTH_LONG).show();
                    Log.e("API_SENHA", "Erro: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Falha na comunicação com o servidor.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fazerLogout() {
        SessionManager sessionManager = new SessionManager(requireContext());
        sessionManager.limparSessao();

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    private boolean isSenhaValida(String senha) {
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$";
        return java.util.regex.Pattern.matches(regex, senha);
    }

    private void buscarFotoPerfilNaApi() {
        ApiService apiService = RetrofitClient.getApiService(requireContext());
        apiService.getFotoPerfil().enqueue(new Callback<FotoPerfilResponseDTO>() {
            @Override
            public void onResponse(Call<FotoPerfilResponseDTO> call, Response<FotoPerfilResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getFotoBase64() != null) {
                    usuarioJaPossuiFoto = true;
                    exibirImagemBase64(response.body().getFotoBase64());
                } else {
                    usuarioJaPossuiFoto = false;
                }
            }

            @Override
            public void onFailure(Call<FotoPerfilResponseDTO> call, Throwable t) {
                Log.e("API_FOTO", "Erro ao buscar foto: " + t.getMessage());
            }
        });
    }

    private void processarEEnviarFoto(android.net.Uri uri) {
        try {
            java.io.InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(inputStream);

            // Redimensiona para evitar Strings Base64 gigantescas que pesam no banco de dados (Max 400x400)
            android.graphics.Bitmap resizedBitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, 400, 400, true);

            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            resizedBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, outputStream);
            byte[] byteArray = outputStream.toByteArray();

            String base64Image = android.util.Base64.encodeToString(byteArray, android.util.Base64.NO_WRAP);

            enviarFotoParaApi(base64Image);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Erro ao processar imagem.", Toast.LENGTH_SHORT).show();
        }
    }

    private void enviarFotoParaApi(String base64Image) {
        ApiService apiService = RetrofitClient.getApiService(requireContext());
        FotoPerfilRequestDTO dto = new FotoPerfilRequestDTO(base64Image);

        Call<FotoPerfilResponseDTO> call;

        if (usuarioJaPossuiFoto) {
            call = apiService.updateFotoPerfil(dto);
        } else {
            call = apiService.setFotoPerfil(dto);
        }

        call.enqueue(new Callback<FotoPerfilResponseDTO>() {
            @Override
            public void onResponse(Call<FotoPerfilResponseDTO> call, Response<FotoPerfilResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(getContext(), "Foto de perfil updated!", Toast.LENGTH_SHORT).show();
                    exibirImagemBase64(response.body().getFotoBase64());
                    usuarioJaPossuiFoto = true;
                } else {
                    // CORREÇÃO: Removido a linha com erro de digitação do 'Toast.org.json...'
                    Log.e("API_FOTO", "Erro ao salvar: " + response.code());
                    Toast.makeText(getContext(), "Erro ao salvar foto no servidor.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FotoPerfilResponseDTO> call, Throwable t) {
                Toast.makeText(getContext(), "Falha de conexão com o servidor.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void exibirImagemBase64(String base64Str) {
        try {
            if (imgFotoPerfil == null) return;
            byte[] decodedString = android.util.Base64.decode(base64Str, android.util.Base64.DEFAULT);
            android.graphics.Bitmap decodedByte = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            imgFotoPerfil.setImageBitmap(decodedByte);

            // Oculta o card do lápis assim que a foto for carregada com sucesso
            if (cardIconeEdicao != null) {
                cardIconeEdicao.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e("DECODE_IMAGE", "Erro ao renderizar base64: " + e.getMessage());
        }
    }
}