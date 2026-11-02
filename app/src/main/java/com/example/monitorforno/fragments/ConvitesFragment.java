
package com.example.monitorforno.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.monitorforno.R;
import com.example.monitorforno.adapters.ConvitesAdapter;
import com.example.monitorforno.models.ApiService;
import com.example.monitorforno.models.FornoCompartilhadoRequestDTO;
import com.example.monitorforno.models.FornoCompartilhadoResponseDTO;
import com.example.monitorforno.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConvitesFragment extends Fragment implements ConvitesAdapter.OnConviteAcaoListener {

    private RecyclerView recyclerConvites;
    private TextView txtSemConvites;
    private ConvitesAdapter adapter;

    private List<FornoCompartilhadoResponseDTO> listaConvites = new ArrayList<>();

    private final List<Call<?>> chamadasAtivas = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_convites, container, false);

        recyclerConvites = view.findViewById(R.id.recyclerConvites);
        txtSemConvites = view.findViewById(R.id.txtSemConvites);

        recyclerConvites.setLayoutManager(new LinearLayoutManager(requireContext()));

        carregarConvites();

        return view;
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

        recyclerConvites = null;
        txtSemConvites = null;
        adapter = null;

        super.onDestroyView();
    }

    private void carregarConvites() {

        if (!viewAtiva()) return;

        ApiService apiService = RetrofitClient.getApiService(requireContext());

        Call<List<FornoCompartilhadoResponseDTO>> call = apiService.listarConvitesPendentes();

        executarComProtecao(call, new Callback<List<FornoCompartilhadoResponseDTO>>() {

            @Override
            public void onResponse(@NonNull Call<List<FornoCompartilhadoResponseDTO>> call,
                                   @NonNull Response<List<FornoCompartilhadoResponseDTO>> response) {

                if (response.isSuccessful() && response.body() != null) {

                    listaConvites = response.body();

                    if (listaConvites.isEmpty()) {

                        mostrarMensagemVazio();

                    } else {

                        txtSemConvites.setVisibility(View.GONE);
                        recyclerConvites.setVisibility(View.VISIBLE);

                        adapter = new ConvitesAdapter(listaConvites, ConvitesFragment.this);
                        recyclerConvites.setAdapter(adapter);
                    }

                } else {

                    Toast.makeText(requireContext(),
                            "Não foi possível carregar os convites.",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<FornoCompartilhadoResponseDTO>> call,
                                  @NonNull Throwable t) {

                Toast.makeText(requireContext(),
                        "Falha de ligação.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onAceitar(FornoCompartilhadoResponseDTO convite, int position) {
        responderConvite(convite, position, true);
    }

    @Override
    public void onRecusar(FornoCompartilhadoResponseDTO convite, int position) {
        responderConvite(convite, position, false);
    }

    private void responderConvite(FornoCompartilhadoResponseDTO convite, int position, boolean isAceitar) {

        if (!viewAtiva() || convite == null) return;

        ApiService apiService = RetrofitClient.getApiService(requireContext());

        FornoCompartilhadoRequestDTO request =
                new FornoCompartilhadoRequestDTO(convite.getSerialNumber(), convite.getEmail());

        Call<FornoCompartilhadoResponseDTO> call;

        if (isAceitar) {
            call = apiService.aceitarConvite(request);
        } else {
            call = apiService.recusarConvite(request);
        }

        executarComProtecao(call, new Callback<FornoCompartilhadoResponseDTO>() {

            @Override
            public void onResponse(@NonNull Call<FornoCompartilhadoResponseDTO> call,
                                   @NonNull Response<FornoCompartilhadoResponseDTO> response) {

                if (response.isSuccessful()) {

                    String acao = isAceitar ? "aceite" : "recusado";

                    Toast.makeText(requireContext(),
                            "Convite " + acao + " com sucesso!",
                            Toast.LENGTH_SHORT).show();

                    if (adapter != null && position >= 0 && position < listaConvites.size()) {

                        adapter.removerItem(position);

                        if (listaConvites.isEmpty()) {
                            mostrarMensagemVazio();
                        }
                    }

                } else {

                    Toast.makeText(requireContext(),
                            "Erro ao processar o convite.",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<FornoCompartilhadoResponseDTO> call,
                                  @NonNull Throwable t) {

                Toast.makeText(requireContext(),
                        "Falha de ligação.",
                        Toast.LENGTH_SHORT).show();
            }private final List<Call<?>> chamadasAtivas = new ArrayList<>();
            private boolean carregamentoEmAndamento = false;
        });
    }

    private void mostrarMensagemVazio() {

        if (txtSemConvites == null || recyclerConvites == null) return;

        txtSemConvites.setVisibility(View.VISIBLE);
        recyclerConvites.setVisibility(View.GONE);
    }
}
