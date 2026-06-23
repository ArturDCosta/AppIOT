package com.example.monitorforno.models;

public class Sessao {

    private String id; // NOVA VARIÁVEL: A "chave" da sessão
    private String data;
    private String horario;
    private String duracao;
    private String estado;

    // Construtor atualizado para receber o ID
    public Sessao(
            String id,
            String data,
            String horario,
            String duracao,
            String estado) {

        this.id = id;
        this.data = data;
        this.horario = horario;
        this.duracao = duracao;
        this.estado = estado;
    }

    public String getId() {
        return id;
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