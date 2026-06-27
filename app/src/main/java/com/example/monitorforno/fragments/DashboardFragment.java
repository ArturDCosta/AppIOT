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
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // Mapeando componentes conforme IDs exatos do seu XML atualizado
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
                        // Se a lista vier vazia, limpa a tela e avisa o usuário
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
        int posicaoParaSelecionar = 0; // Padrão é o primeiro se não achar nada

        if (idSalvo != null) {
            for (int i = 0; i < listaDeFornosDoUsuario.size(); i++) {
                if (idSalvo.equals(listaDeFornosDoUsuario.get(i).getId())) {
                    posicaoParaSelecionar = i;
                    break;
                }
            }
        }
        // Força o spinner a apontar para a posição correta antes do usuário interagir
        spinnerFornos.setSelection(posicaoParaSelecionar);

        spinnerFornos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                FornoResponseDTO fornoSelecionado = listaDeFornosDoUsuario.get(position);

                // GRAVA NA SESSÃO: Toda vez que o usuário mudar o seletor, salvamos no SharedPreferences
                sessionManager.salvarFornoSelecionado(fornoSelecionado.getId());

                // 100% API: Carrega as telemetrias e os alertas
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
                    // Se a requisição falhar ou retornar corpo vazio, assume-se forno offline/desligado
                    vincularDadosNaTela(null, nomeForno);
                }
            }

            @Override
            public void onFailure(Call<DashboardDTO> call, Throwable t) {
                // Falha de rede trata como offline mantendo o nome do forno selecionado
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

                    // Pega apenas os 3 alertas mais recentes para o Dashboard
                    List<EventoDTO> ultimosEventos;
                    if (todosEventos.size() > 3) {
                        ultimosEventos = todosEventos.subList(0, 3);
                    } else {
                        ultimosEventos = todosEventos;
                    }

                    // Joga os dados reais no Adapter
                    recyclerAlertas.setAdapter(new EventoAdapter(ultimosEventos));
                } else {
                    // Limpa a lista caso não tenha dados (sem fakes)
                    recyclerAlertas.setAdapter(new EventoAdapter(new ArrayList<>()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<EventoDTO>> call, @NonNull Throwable t) {
                // Limpa a lista em caso de falha de conexão
                recyclerAlertas.setAdapter(new EventoAdapter(new ArrayList<>()));
            }
        });
    }

    private void vincularDadosNaTela(DashboardDTO dados, String nomeForno) {
        // 1. Vincula o nome vindo do seletor da API
        txtNomeForno.setText(nomeForno != null ? nomeForno : "--");

        // Caso o objeto de telemetria seja inteiramente nulo (Forno Desligado / Sem dados)
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

        // Temperaturas
        String tempAtualTexto = dados.getTemperaturaAtual() != null ? Math.round(dados.getTemperaturaAtual()) + "°C" : "--";
        String tempUltimaTexto = dados.getTemperaturaUltima() != null ? Math.round(dados.getTemperaturaUltima()) + "°C" : "--";
        txtTemperaturaAtual.setText(tempAtualTexto);
        txtAtual.setText(tempAtualTexto);
        txtUltima.setText(tempUltimaTexto);

        // Tempo Ligado
        if (dados.getTempoLigadoMinutos() != null) {
            long totalMinutos = dados.getTempoLigadoMinutos();
            long horas = totalMinutos / 60;
            long minutos = totalMinutos % 60;
            txtTempoLigado.setText(horas > 0 ? horas + "h " + minutos + "m" : minutos + "m");
        } else {
            txtTempoLigado.setText("--");
        }

        // Temporizador
        txtTemporizador.setText(dados.getProximoTemporizador() != null ? dados.getProximoTemporizador() : "--");

        // Status do Sistema
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

        // Status do Forno
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

    private void abrirLeitorQrCode() {
        GmsBarcodeScanner scanner = GmsBarcodeScanning.getClient(requireContext());
        scanner.startScan()
                .addOnSuccessListener(barcode -> {
                    String conteudo = barcode.getRawValue();
                    if (conteudo != null && conteudo.contains(";")) {
                        String[] partes = conteudo.split(";");
                        enviarVinculoParaApi(partes[0].trim(), partes[1].trim());
                    } else {
                        Toast.makeText(requireContext(), "Formato de QR Code Inválido.", Toast.LENGTH_SHORT).show();
                    }
                });
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
        View.OnClickListener abrirTemperatura = v -> getParentFragmentManager()
                .beginTransaction().replace(R.id.fragment_container, new TemperaturaFragment())
                .addToBackStack(null).commit();
        cardTemperaturaAtual.setOnClickListener(abrirTemperatura);
        cardUltimaTemperatura.setOnClickListener(abrirTemperatura);
    }
}