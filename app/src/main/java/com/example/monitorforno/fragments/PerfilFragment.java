package com.example.monitorforno.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.monitorforno.R;
import com.example.monitorforno.activities.LoginActivity;
import com.google.android.material.button.MaterialButton;

public class PerfilFragment extends Fragment {

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_perfil,
                container,
                false
        );

        TextView txtNomePerfil =
                view.findViewById(R.id.txtNomePerfil);

        TextView txtEmailPerfil =
                view.findViewById(R.id.txtEmailPerfil);

        TextView txtNome =
                view.findViewById(R.id.txtNome);

        TextView txtEmail =
                view.findViewById(R.id.txtEmail);

        TextView txtNascimento =
                view.findViewById(R.id.txtNascimento);

        txtNomePerfil.setText("Rafael Fiorio");
        txtEmailPerfil.setText("rafa@gmail.com");

        txtNome.setText("Rafael Fiorio");
        txtEmail.setText("rafa@gmail.com");
        txtNascimento.setText("13/02/2009");

        MaterialButton btnAlterarSenha =
                view.findViewById(R.id.btnAlterarSenha);

        btnAlterarSenha.setOnClickListener(v ->
                Toast.makeText(
                        getContext(),
                        "Em desenvolvimento",
                        Toast.LENGTH_SHORT
                ).show()
        );

        MaterialButton btnLogout =
                view.findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            getActivity(),
                            LoginActivity.class
                    );

            startActivity(intent);

            requireActivity().finish();

        });

        return view;
    }
}