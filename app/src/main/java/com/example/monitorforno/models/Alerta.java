package com.example.monitorforno.models;

public class Alerta {

    private String tipo;
    private String horario;

    public Alerta(String tipo, String horario) {
        this.tipo = tipo;
        this.horario = horario;
    }

    public String getTipo() {
        return tipo;
    }

    public String getHorario() {
        return horario;
    }
}