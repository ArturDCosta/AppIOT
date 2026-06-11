package com.example.monitorforno.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import com.example.monitorforno.R;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.monitorforno.R;
import com.example.monitorforno.adapters.SessaoAdapter;
import com.example.monitorforno.models.Sessao;

import java.util.ArrayList;
import java.util.List;

public class HistoricoFragment extends Fragment {

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_historico,
                container,
                false
        );

        RecyclerView recyclerView =
                view.findViewById(R.id.recyclerHistorico);

        recyclerView.setLayoutManager(
                new LinearLayoutManager(getContext())
        );

        List<Sessao> sessoes = new ArrayList<>();

        sessoes.add(
                new Sessao(
                        "09/06/2026",
                        "14:31 - 15:42",
                        "1h 11min",
                        "FORNO_ATIVO"
                )
        );

        sessoes.add(
                new Sessao(
                        "08/06/2026",
                        "18:00 - 19:15",
                        "1h 15min",
                        "FORNO_ESFRIANDO"
                )
        );

        sessoes.add(
                new Sessao(
                        "07/06/2026",
                        "16:00 - 17:05",
                        "1h 05min",
                        "FORNO_AQUECENDO"
                )
        );

        SessaoAdapter adapter =
                new SessaoAdapter(sessoes);

        recyclerView.setAdapter(adapter);

        return view;
    }
}