package com.example.monitorforno.models;

public class EventoSessao {

    private String descricao;
    private String horario;

    public EventoSessao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}