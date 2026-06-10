package com.example.monitorforno.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.monitorforno.R;
import com.google.android.material.card.MaterialCardView;

public class DashboardFragment extends Fragment {

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_dashboard,
                container,
                false
        );

        MaterialCardView cardEstadoForno =
                view.findViewById(R.id.cardEstadoForno);

        String estado = "FORNO_AQUECENDO";

        switch (estado) {

            case "FORNO_AQUECENDO":
                cardEstadoForno.setCardBackgroundColor(
                        getResources().getColor(R.color.forno_aquecendo)
                );
                break;

            case "FORNO_DESLIGADO":
                cardEstadoForno.setCardBackgroundColor(
                        getResources().getColor(R.color.forno_desligado)
                );
                break;

            case "FORNO_ATIVO":
                cardEstadoForno.setCardBackgroundColor(
                        getResources().getColor(R.color.forno_ativo)
                );
                break;

            case "FORNO_ESFRIANDO":
                cardEstadoForno.setCardBackgroundColor(
                        getResources().getColor(R.color.forno_esfriando)
                );
                break;
        }

        return view;
    }
}