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
import com.example.monitorforno.models.ConviteResponseDTO;
import com.example.monitorforno.models.FornoCompartilhadoRequestDTO;
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
    private List<ConviteResponseDTO> listaConvites = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_convites, container, false);

        recyclerConvites = view.findViewById(R.id.recyclerConvites);
        txtSemConvites = view.findViewById(R.id.txtSemConvites);

        recyclerConvites.setLayoutManager(new LinearLayoutManager(getContext()));

        carregarConvites();

        return view;
    }

    private void carregarConvites() {
        if (getContext() == null) return;

        ApiService apiService = RetrofitClient.getApiService(getContext());
        apiService.listarConvitesPendentes().enqueue(new Callback<List<ConviteResponseDTO>>() {
            @Override
            public void onResponse(Call<List<ConviteResponseDTO>> call, Response<List<ConviteResponseDTO>> response) {
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
                    Toast.makeText(getContext(), "Não foi possível carregar os convites.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ConviteResponseDTO>> call, Throwable t) {
                Toast.makeText(getContext(), "Falha de ligação.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onAceitar(ConviteResponseDTO convite, int position) {
        responderConvite(convite, position, true);
    }

    @Override
    public void onRecusar(ConviteResponseDTO convite, int position) {
        responderConvite(convite, position, false);
    }

    private void responderConvite(ConviteResponseDTO convite, int position, boolean isAceitar) {
        if (getContext() == null) return;

        ApiService apiService = RetrofitClient.getApiService(getContext());
        FornoCompartilhadoRequestDTO request = new FornoCompartilhadoRequestDTO(convite.getSerialNumber(), "");

        // Substituímos o operador ternário por um if/else tradicional
        Call<Void> call;
        if (isAceitar) {
            call = apiService.aceitarConvite(request);
        } else {
            call = apiService.recusarConvite(request);
        }

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    String acao = isAceitar ? "aceite" : "recusado";
                    Toast.makeText(getContext(), "Convite " + acao + " com sucesso!", Toast.LENGTH_SHORT).show();

                    adapter.removerItem(position);

                    if (listaConvites.isEmpty()) {
                        mostrarMensagemVazio();
                    }
                } else {
                    Toast.makeText(getContext(), "Erro ao processar o convite.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Falha de ligação.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarMensagemVazio() {
        txtSemConvites.setVisibility(View.VISIBLE);
        recyclerConvites.setVisibility(View.GONE);
    }
}