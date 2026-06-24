package com.example.monitorforno.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.monitorforno.models.Evento;
import com.example.monitorforno.network.RetrofitClient;
import com.example.monitorforno.utils.CustomDivisor;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardFragment extends Fragment {

    private TextView txtSistemaAtual, txtEstadoSistema, txtTemperaturaAtual, txtEstadoForno, txtAtual, txtUltima, txtTempoLigado, txtTemporizador;
    private MaterialCardView cardEstadoForno, cardTemperaturaAtual, cardUltimaTemperatura;
    private RecyclerView recyclerAlertas;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // Mapeando componentes do XML
        txtSistemaAtual = view.findViewById(R.id.txtSistema);
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

        configurarRecyclerAlertas();
        configurarCliquesCards();

        // EXECUÇÃO DO DIAGRAMA: Chama o Dashboard vindo da API Spring
        // Substitua pelo UUID real de um forno cadastrado no seu banco para testes
        String fornoIdDeTeste = "b06f3899-1060-4bec-b0f7-1bd6c5fcce90";
        carregarDadosDoDashboard(fornoIdDeTeste);

        return view;
    }

    private void carregarDadosDoDashboard(String fornoId) {
        // 1. Rastreador antes de iniciar
        Log.d("DEBUG_API", "Iniciando chamada para o dashboard. FornoID: " + fornoId);

        ApiService apiService = RetrofitClient.getApiService(requireContext());

        apiService.getDashboard(fornoId).enqueue(new Callback<DashboardDTO>() { // Confirme o nome do seu DTO
            @Override
            public void onResponse(Call<DashboardDTO> call, Response<DashboardDTO> response) {

                // 2. Rastreador quando a resposta chega
                Log.d("DEBUG_API", "Resposta recebida! Código: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    // Sucesso
                    DashboardDTO dados = response.body();
                    vincularDadosNaTela(dados);
                    Toast.makeText(requireContext(), "Sucesso!", Toast.LENGTH_SHORT).show();

                } else {
                    try {
                        String erroExato = response.errorBody() != null
                                ? response.errorBody().string()
                                : "";

                        // Se o Spring Security mandar vazio, nós avisamos!
                        if (erroExato.trim().isEmpty()) {
                            erroExato = "O servidor bloqueou (403) mas não enviou texto de erro.";
                        }

                        Log.e("DEBUG_API", "Detalhes do Erro: " + erroExato);
                        Toast.makeText(requireContext(), "Erro " + response.code() + ": " + erroExato, Toast.LENGTH_LONG).show();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<DashboardDTO> call, Throwable t) {
                // 3. Rastreador de falha de rede
                Log.e("DEBUG_API", "Caiu no onFailure! Motivo: " + t.getMessage());
                Toast.makeText(requireContext(), "Falha: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void vincularDadosNaTela(DashboardDTO dados) {
        // 1. Atualiza Temperaturas (Arredondando para não mostrar casas decimais longas)
        String tempAtualTexto = (dados.getTemperaturaAtual() != null ? Math.round(dados.getTemperaturaAtual()) : 0) + "°C";
        String tempUltimaTexto = (dados.getTemperaturaUltima() != null ? Math.round(dados.getTemperaturaUltima()) : 0) + "°C";

        txtTemperaturaAtual.setText(tempAtualTexto);
        txtAtual.setText(tempAtualTexto);
        txtUltima.setText(tempUltimaTexto);

        // 2. Formata Tempo Ligado (De Long minutos para formato "Xh Ym")
        if (dados.getTempoLigadoMinutos() != null) {
            long totalMinutos = dados.getTempoLigadoMinutos();
            long horas = totalMinutos / 60;
            long minutos = totalMinutos % 60;

            if (horas > 0) {
                txtTempoLigado.setText(horas + "h " + minutos + "m");
            } else {
                txtTempoLigado.setText(minutos + "m");
            }
        } else {
            txtTempoLigado.setText("--");
        }

        // Próximo temporizador
        txtTemporizador.setText(dados.getProximoTemporizador() != null ? dados.getProximoTemporizador() : "--");

        // 3. Atualiza Estado do Sistema (Cores dinâmicas baseadas no DTO)
        String estadoSistema = dados.getEstadoSistema() != null ? dados.getEstadoSistema() : "DESCONHECIDO";
        txtSistemaAtual.setText(estadoSistema);
        txtEstadoSistema.setText(estadoSistema);

        if ("SEGURO".equals(estadoSistema) || "OPERACAO_NORMAL".equals(estadoSistema)) {
            txtSistemaAtual.setTextColor(getResources().getColor(R.color.alerta_verde));
        } else if ("ALERTA".equals(estadoSistema)) {
            txtSistemaAtual.setTextColor(getResources().getColor(R.color.alerta_laranja));
        } else if ("CRITICO".equals(estadoSistema)) {
            txtSistemaAtual.setTextColor(getResources().getColor(R.color.alerta_vermelho));
        } else {
            txtSistemaAtual.setTextColor(Color.GRAY);
        }

        // 4. Atualiza Estado do Forno (CardBackground dinâmico baseado no DTO)
        String estadoForno = dados.getEstadoForno() != null ? dados.getEstadoForno() : "FORNO_DESLIGADO";

        // Remove os underlines para exibir bonitinho na tela (Ex: "FORNO AQUECENDO")
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

    private void configurarRecyclerAlertas() {
        List<Evento> eventos = new ArrayList<>();
        eventos.add(new Evento("Sistema entrou em alerta", "18:30"));
        eventos.add(new Evento("Estado crítico", "18:20"));
        recyclerAlertas.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerAlertas.addItemDecoration(new CustomDivisor(getContext()));
        recyclerAlertas.setAdapter(new EventoAdapter(eventos));
    }

    private void configurarCliquesCards() {
        // Abre o fragment de temperatura ao clicar nos cards de temperatura
        View.OnClickListener abrirTemperatura = v -> getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new TemperaturaFragment())
                .addToBackStack(null)
                .commit();

        cardTemperaturaAtual.setOnClickListener(abrirTemperatura);
        cardUltimaTemperatura.setOnClickListener(abrirTemperatura);
    }
}