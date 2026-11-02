
package com.example.monitorforno.fragments;

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
import androidx.annotation.Nullable;

import com.example.monitorforno.R;
import com.example.monitorforno.adapters.EventoAdapter;
import com.example.monitorforno.models.ApiService;
import com.example.monitorforno.models.EventoDTO;
import com.example.monitorforno.network.RetrofitClient;
import com.example.monitorforno.utils.SessionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlertasFragment extends Fragment {

    private RecyclerView recyclerAlertas;
    private TextView txtTotalAlertas, txtTotalCriticos, txtUltimoEvento;
    private SessionManager sessionManager;

    private final List<Call<?>> chamadasAtivas = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_alertas, container, false);

        txtTotalAlertas = view.findViewById(R.id.txtTotalAlertas);
        txtTotalCriticos = view.findViewById(R.id.txtTotalCriticos);
        txtUltimoEvento = view.findViewById(R.id.txtUltimoEvento);
        recyclerAlertas = view.findViewById(R.id.recyclerAlertas);

        recyclerAlertas.setLayoutManager(new LinearLayoutManager(requireContext()));
        sessionManager = new SessionManager(requireContext());

        atualizarEstatisticasVazias();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        buscarAlertasDaApi();
    }

    private boolean viewAtiva() {
        return isAdded() && getView() != null && getContext() != null;
    }

    private <T> void executarComProtecao(Call<T> call, Callback<T> callback) {

        chamadasAtivas.add(call);

        call.enqueue(new Callback<T>() {

            @Override
            public void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {

                chamadasAtivas.remove(call);

                if (call.isCanceled() || !viewAtiva()) return;

                callback.onResponse(call, response);
            }

            @Override
            public void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {

                chamadasAtivas.remove(call);

                if (call.isCanceled() || !viewAtiva()) return;

                callback.onFailure(call, t);
            }
        });
    }

    private void cancelarChamadas() {

        for (Call<?> call : new ArrayList<>(chamadasAtivas)) {
            call.cancel();
        }

        chamadasAtivas.clear();
    }

    @Override
    public void onDestroyView() {

        cancelarChamadas();

        recyclerAlertas = null;
        txtTotalAlertas = null;
        txtTotalCriticos = null;
        txtUltimoEvento = null;
        sessionManager = null;

        super.onDestroyView();
    }

    private void buscarAlertasDaApi() {

        if (!viewAtiva()) return;

        String fornoId = sessionManager.getFornoSelecionadoId();

        if (fornoId == null || fornoId.isEmpty()) {
            Toast.makeText(requireContext(), "Nenhum forno selecionado.", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient.getApiService(requireContext());

        Call<List<EventoDTO>> call = apiService.getAlertasDoForno(fornoId);

        executarComProtecao(call, new Callback<List<EventoDTO>>() {

            @Override
            public void onResponse(@NonNull Call<List<EventoDTO>> call, @NonNull Response<List<EventoDTO>> response) {

                Log.d("ALERTAS_API", "Código HTTP: " + response.code());

                if (response.isSuccessful() && response.body() != null) {

                    List<EventoDTO> eventosDaApi = response.body();

                    Log.d("ALERTAS_API",
                            "Quantidade de eventos recebidos: " +
                                    (eventosDaApi == null ? "null" : eventosDaApi.size()));

                    Collections.sort(eventosDaApi, (e1, e2) -> {
                        if (e1.getCriadoEm() == null) return e2.getCriadoEm() == null ? 0 : 1;
                        if (e2.getCriadoEm() == null) return -1;
                        return e2.getCriadoEm().compareTo(e1.getCriadoEm());
                    });

                    EventoAdapter adapter = new EventoAdapter(eventosDaApi);
                    recyclerAlertas.setAdapter(adapter);

                    calcularEstatisticas(eventosDaApi);

                } else {

                    atualizarEstatisticasVazias();

                    String detalheErro = "";

                    try {
                        if (response.errorBody() != null) {
                            detalheErro = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e("ALERTAS_API", "Erro ao ler resposta da API.", e);
                    }

                    Log.e("ALERTAS_API",
                            "Erro HTTP: " + response.code() + " - " + detalheErro);

                    Toast.makeText(requireContext(),
                            "Erro ao carregar alertas: " + response.code(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<EventoDTO>> call, @NonNull Throwable t) {

                Log.e("ALERTAS_API", "Falha na conexão com a API.", t);

                atualizarEstatisticasVazias();

                Toast.makeText(requireContext(),
                        "Sem conexão com o servidor.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calcularEstatisticas(List<EventoDTO> eventos) {

        if (!viewAtiva()) return;

        if (eventos == null || eventos.isEmpty()) {
            atualizarEstatisticasVazias();
            return;
        }

        int countAlertas = 0;
        int countCriticos = 0;

        String dataHoraUltimo = formatarDataHora(eventos.get(0).getCriadoEm());

        for (EventoDTO evento : eventos) {

            if (evento != null && evento.getTipo() != null) {

                if (evento.getTipo().contains("ALERTA_ENTRADA")) {
                    countAlertas++;
                } else if (evento.getTipo().contains("CRITICO_ENTRADA")) {
                    countCriticos++;
                }
            }
        }

        txtTotalAlertas.setText("Total de Alertas: " + countAlertas);
        txtTotalCriticos.setText("Eventos Críticos: " + countCriticos);
        txtUltimoEvento.setText("Último Evento: " + dataHoraUltimo);
    }

    private void atualizarEstatisticasVazias() {

        if (txtTotalAlertas == null || txtTotalCriticos == null || txtUltimoEvento == null) {
            return;
        }

        txtTotalAlertas.setText("Total de Alertas: 0");
        txtTotalCriticos.setText("Eventos Críticos: 0");
        txtUltimoEvento.setText("Último Evento: --/--/---- - --:--");
    }

    private String formatarDataHora(String dataOriginal) {

        if (dataOriginal == null || !dataOriginal.contains("T")) {
            return "--/--/---- - --:--";
        }

        try {

            String dataLimpa = dataOriginal.split("\\.")[0];

            if (!dataLimpa.endsWith("Z")) {
                dataLimpa += "Z";
            }

            java.text.SimpleDateFormat sdfEntrada =
                    new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault());

            sdfEntrada.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));

            java.util.Date dataObj = sdfEntrada.parse(dataLimpa);

            java.text.SimpleDateFormat sdfSaida =
                    new java.text.SimpleDateFormat("dd/MM/yyyy - HH:mm", java.util.Locale.getDefault());

            sdfSaida.setTimeZone(java.util.TimeZone.getDefault());

            return sdfSaida.format(dataObj);

        } catch (Exception e) {
            return dataOriginal;
        }
    }
}