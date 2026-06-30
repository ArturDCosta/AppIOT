package com.example.monitorforno.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.monitorforno.R;
import com.example.monitorforno.adapters.EventoAdapter;
import com.example.monitorforno.models.ApiService;
import com.example.monitorforno.models.DashboardDTO;
import com.example.monitorforno.models.EventoDTO;
import com.example.monitorforno.models.FornoResponseDTO;
import com.example.monitorforno.models.VincularFornoDTO;
import com.example.monitorforno.network.RetrofitClient;
import com.example.monitorforno.utils.CustomDivisor;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardFragment extends Fragment {

    private com.example.monitorforno.utils.SessionManager sessionManager;

    // Componentes do XML mapeados
    private TextView txtNomeForno, txtSistema, txtEstadoSistema, txtTemperaturaAtual, txtEstadoForno, txtAtual, txtUltima, txtTempoLigado, txtTemporizador;
    private MaterialCardView cardEstadoForno, cardTemperaturaAtual, cardUltimaTemperatura;
    private RecyclerView recyclerAlertas;

    private Spinner spinnerFornos;
    private MaterialButton btnEscaneadorQr;
    private List<FornoResponseDTO> listaDeFornosDoUsuario = new ArrayList<>();

    // 1. OUVINTE DO ZXING PARA CAPTURAR O RESULTADO DO QR CODE
    private final ActivityResultLauncher<ScanOptions> leitorDeQrCode = registerForActivityResult(
            new ScanContract(),
            result -> {
                if (result.getContents() == null) {
                    Toast.makeText(requireContext(), "Leitura cancelada", Toast.LENGTH_SHORT).show();
                } else {
                    String conteudo = result.getContents();
                    if (conteudo != null && conteudo.contains(";")) {
                        String[] partes = conteudo.split(";");
                        enviarVinculoParaApi(partes[0].trim(), partes[1].trim());
                    } else {
                        Toast.makeText(requireContext(), "Formato de QR Code Inválido.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // Mapeando componentes
        txtNomeForno = view.findViewById(R.id.txtNomeForno);
        txtSistema = view.findViewById(R.id.txtSistema);
        txtEstadoSistema = view.findViewById(R.id.txtEstadoSistema);
        txtTemperaturaAtual = view.findViewById(R.id.txtTemperaturaAtual);
        txtEstadoForno = view.findViewById(R.id.txtEstadoForno);
        txtAtual = view.findViewById(R.id.txtAtual);
        txtUltima = view.findViewById(R.id.txtUltima);
        txtTempoLigado = view.findViewById(R.id.txtTempoLigado);
        txtTemporizador = view.findViewById(R.id.txtTemporizador);

        cardEstadoForno = view.findViewById(R.id.cardEstadoForno);
        cardTemperaturaAtual = view.findViewById(R.id.cardTemperaturaAtual);
        cardUltimaTemperatura = view.findViewById(R.id.cardUltimaTemperatura);
        recyclerAlertas = view.findViewById(R.id.recyclerAlertas);
        spinnerFornos = view.findViewById(R.id.spinnerFornos);
        btnEscaneadorQr = view.findViewById(R.id.btnEscaneadorQr);

        sessionManager = new com.example.monitorforno.utils.SessionManager(requireContext());

        // Prepara o visual do Recycler de alertas para receber os dados
        recyclerAlertas.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerAlertas.addItemDecoration(new CustomDivisor(getContext()));

        configurarCliquesCards();

        btnEscaneadorQr.setOnClickListener(v -> abrirLeitorQrCode());

        // Carrega a listagem de fornos associados ao usuário
        carregarListaDeFornos();

        return view;
    }

    private void carregarListaDeFornos() {
        ApiService apiService = RetrofitClient.getApiService(requireContext());
        apiService.buscarMeusFornos().enqueue(new Callback<List<FornoResponseDTO>>() {
            @Override
            public void onResponse(Call<List<FornoResponseDTO>> call, Response<List<FornoResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaDeFornosDoUsuario = response.body();

                    if (!listaDeFornosDoUsuario.isEmpty()) {
                        configurarSpinnerFornos();
                    } else {
                        vincularDadosNaTela(null, "Nenhum Forno");
                        Toast.makeText(requireContext(), "Escaneie o QR Code do forno para começar!", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<FornoResponseDTO>> call, Throwable t) {
                Log.e("DEBUG_API", "Erro ao buscar lista: " + t.getMessage());
                vincularDadosNaTela(null, "Erro de Conexão");
            }
        });
    }

    private void configurarSpinnerFornos() {
        ArrayAdapter<FornoResponseDTO> adapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, listaDeFornosDoUsuario);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFornos.setAdapter(adapter);

        String idSalvo = sessionManager.getFornoSelecionadoId();
        int posicaoParaSelecionar = 0;

        if (idSalvo != null) {
            for (int i = 0; i < listaDeFornosDoUsuario.size(); i++) {
                if (idSalvo.equals(listaDeFornosDoUsuario.get(i).getId())) {
                    posicaoParaSelecionar = i;
                    break;
                }
            }
        }
        spinnerFornos.setSelection(posicaoParaSelecionar);

        spinnerFornos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                FornoResponseDTO fornoSelecionado = listaDeFornosDoUsuario.get(position);
                sessionManager.salvarFornoSelecionado(fornoSelecionado.getId());
                carregarDadosDoDashboard(fornoSelecionado.getId(), fornoSelecionado.getNome());
                carregarAlertasReaisNoDashboard(fornoSelecionado.getId());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void carregarDadosDoDashboard(String fornoId, String nomeForno) {
        ApiService apiService = RetrofitClient.getApiService(requireContext());
        apiService.getDashboard(fornoId).enqueue(new Callback<DashboardDTO>() {
            @Override
            public void onResponse(Call<DashboardDTO> call, Response<DashboardDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    vincularDadosNaTela(response.body(), nomeForno);
                } else {
                    vincularDadosNaTela(null, nomeForno);
                }
            }

            @Override
            public void onFailure(Call<DashboardDTO> call, Throwable t) {
                vincularDadosNaTela(null, nomeForno);
            }
        });
    }

    private void carregarAlertasReaisNoDashboard(String fornoId) {
        ApiService apiService = RetrofitClient.getApiService(requireContext());

        apiService.getAlertasDoForno(fornoId).enqueue(new Callback<List<EventoDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<EventoDTO>> call, @NonNull Response<List<EventoDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<EventoDTO> todosEventos = response.body();
                    List<EventoDTO> ultimosEventos;
                    if (todosEventos.size() > 3) {
                        ultimosEventos = todosEventos.subList(0, 3);
                    } else {
                        ultimosEventos = todosEventos;
                    }
                    recyclerAlertas.setAdapter(new EventoAdapter(ultimosEventos));
                } else {
                    recyclerAlertas.setAdapter(new EventoAdapter(new ArrayList<>()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<EventoDTO>> call, @NonNull Throwable t) {
                recyclerAlertas.setAdapter(new EventoAdapter(new ArrayList<>()));
            }
        });
    }

    private void vincularDadosNaTela(DashboardDTO dados, String nomeForno) {
        txtNomeForno.setText(nomeForno != null ? nomeForno : "--");

        if (dados == null) {
            txtTemperaturaAtual.setText("--");
            txtAtual.setText("--");
            txtUltima.setText("--");
            txtTempoLigado.setText("--");
            txtTemporizador.setText("--");
            txtSistema.setText("--");
            txtEstadoSistema.setText("--");
            txtEstadoSistema.setTextColor(Color.GRAY);

            txtEstadoForno.setText("FORNO DESLIGADO");
            cardEstadoForno.setCardBackgroundColor(getResources().getColor(R.color.forno_desligado));
            return;
        }

        String tempAtualTexto = dados.getTemperaturaAtual() != null ? Math.round(dados.getTemperaturaAtual()) + "°C" : "--";
        String tempUltimaTexto = dados.getTemperaturaUltima() != null ? Math.round(dados.getTemperaturaUltima()) + "°C" : "--";
        txtTemperaturaAtual.setText(tempAtualTexto);
        txtAtual.setText(tempAtualTexto);
        txtUltima.setText(tempUltimaTexto);

        if (dados.getTempoLigadoMinutos() != null) {
            long totalMinutos = dados.getTempoLigadoMinutos();
            long horas = totalMinutos / 60;
            long minutos = totalMinutos % 60;
            txtTempoLigado.setText(horas > 0 ? horas + "h " + minutos + "m" : minutos + "m");
        } else {
            txtTempoLigado.setText("--");
        }

        txtTemporizador.setText(dados.getProximoTemporizador() != null ? formatarTemporizador(dados.getProximoTemporizador()) : "--");

        String estadoSistema = dados.getEstadoSistema() != null ? dados.getEstadoSistema() : "--";
        txtSistema.setText(estadoSistema);
        txtEstadoSistema.setText(estadoSistema);

        if ("SEGURO".equals(estadoSistema) || "OPERACAO_NORMAL".equals(estadoSistema)) {
            txtEstadoSistema.setTextColor(getResources().getColor(R.color.alerta_verde));
        } else if ("ALERTA".equals(estadoSistema)) {
            txtEstadoSistema.setTextColor(getResources().getColor(R.color.alerta_laranja));
        } else if ("CRITICO".equals(estadoSistema)) {
            txtEstadoSistema.setTextColor(getResources().getColor(R.color.alerta_vermelho));
        } else {
            txtEstadoSistema.setTextColor(Color.GRAY);
        }

        String estadoForno = dados.getEstadoForno() != null ? dados.getEstadoForno() : "FORNO_DESLIGADO";
        txtEstadoForno.setText(estadoForno.replace("_", " "));

        switch (estadoForno) {
            case "FORNO_AQUECENDO":
                cardEstadoForno.setCardBackgroundColor(getResources().getColor(R.color.forno_aquecendo));
                break;
            case "FORNO_ATIVO":
                cardEstadoForno.setCardBackgroundColor(getResources().getColor(R.color.forno_ativo));
                break;
            case "FORNO_ESFRIANDO":
                cardEstadoForno.setCardBackgroundColor(getResources().getColor(R.color.forno_esfriando));
                break;
            default:
            case "FORNO_DESLIGADO":
                cardEstadoForno.setCardBackgroundColor(getResources().getColor(R.color.forno_desligado));
                break;
        }
    }

    // 2. NOVA FUNÇÃO QUE ABRE O SCANNER DO ZXING
    private void abrirLeitorQrCode() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Aponte para o QR Code do forno");
        options.setBeepEnabled(true);

        // 1. Mude para true para impedir que a tela gire se o usuário deitar o celular
        options.setOrientationLocked(true);

        // 2. Chame a SUA nova classe, e não a padrão do ZXing!
        options.setCaptureActivity(com.example.monitorforno.activities.CustomScannerActivity.class);

        leitorDeQrCode.launch(options);
    }

    private void enviarVinculoParaApi(String serial, String pin) {
        ApiService apiService = RetrofitClient.getApiService(requireContext());
        apiService.vincularForno(new VincularFornoDTO(serial, pin)).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Forno adicionado!", Toast.LENGTH_SHORT).show();
                    carregarListaDeFornos();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {}
        });
    }

    private void configurarCliquesCards() {
        View.OnClickListener abrirTemperatura = v -> {
            if (spinnerFornos != null && spinnerFornos.getSelectedItem() != null) {
                FornoResponseDTO fornoSelecionado = (FornoResponseDTO) spinnerFornos.getSelectedItem();
                String idDoForno = fornoSelecionado.getId();

                TemperaturaFragment fragment = new TemperaturaFragment();
                Bundle args = new Bundle();
                args.putString("FORNO_ID", idDoForno);
                fragment.setArguments(args);

                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            } else {
                Toast.makeText(requireContext(), "Por favor, selecione um forno primeiro.", Toast.LENGTH_SHORT).show();
            }
        };

        cardTemperaturaAtual.setOnClickListener(abrirTemperatura);
        cardUltimaTemperatura.setOnClickListener(abrirTemperatura);
    }

    private String formatarTemporizador(String dataIso) {
        if (dataIso == null || !dataIso.contains("T")) {
            return dataIso;
        }

        try {
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            java.text.SimpleDateFormat formatoHora = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
            String horaInicio = formatoHora.format(calendar.getTime());

            String dataLimpa = dataIso.split("\\.")[0];
            java.text.SimpleDateFormat formatoEntrada = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault());
            java.util.Date dataFimObj = formatoEntrada.parse(dataLimpa);

            java.text.SimpleDateFormat formatoHoraFim = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
            java.text.SimpleDateFormat formatoDia = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());

            String horaFim = formatoHoraFim.format(dataFimObj);
            String diaFim = formatoDia.format(dataFimObj);

            return horaInicio + " às " + horaFim + " - " + diaFim;

        } catch (Exception e) {
            e.printStackTrace();
            return dataIso;
        }
    }
}