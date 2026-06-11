package com.example.monitorforno.models;

public class Sessao {

    private String data;
    private String horario;
    private String duracao;
    private String estado;

    public Sessao(
            String data,
            String horario,
            String duracao,
            String estado) {

        this.data = data;
        this.horario = horario;
        this.duracao = duracao;
        this.estado = estado;
    }

    public String getData() {
        return data;
    }

    public String getHorario() {
        return horario;
    }

    public String getDuracao() {
        return duracao;
    }

    public String getEstado() {
        return estado;
    }
}