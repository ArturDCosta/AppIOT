package com.example.monitorforno.models;

public class Evento {

    private String descricao;
    private String horario;

    public Evento(String descricao, String horario) {
        this.descricao = descricao;
        this.horario = horario;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getHorario() {
        return horario;
    }
}