package com.example.monitorforno.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.monitorforno.R;
import com.example.monitorforno.fragments.AlertasFragment;
import com.example.monitorforno.fragments.DashboardFragment;
import com.example.monitorforno.fragments.HistoricoFragment;
import com.example.monitorforno.fragments.PerfilFragment;
import com.example.monitorforno.fragments.TemporizadoresFragment;
import com.example.monitorforno.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SessionManager sessionManager = new SessionManager(this);

        if (!sessionManager.estaLogado()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavigation);

        // Fragment inicial
        replaceFragment(new DashboardFragment());

        bottomNavigationView.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_dashboard) {
                replaceFragment(new DashboardFragment());

            } else if (id == R.id.nav_historico) {
                replaceFragment(new HistoricoFragment());

            } else if (id == R.id.nav_alertas) {
                replaceFragment(new AlertasFragment());

            } else if (id == R.id.nav_temporizadores) {
                replaceFragment(new TemporizadoresFragment());

            } else if (id == R.id.nav_perfil) {
                replaceFragment(new PerfilFragment());
            }

            return true;
        });
    }

    private void replaceFragment(Fragment fragment) {

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}