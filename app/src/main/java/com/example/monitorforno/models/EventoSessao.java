package com.example.monitorforno.models;

import com.google.gson.annotations.SerializedName;

public class EventoSessao {

    // Diz ao Gson: "O JSON vai mandar 'tipo', mas guarde na variável 'descricao'"
    @SerializedName("tipo")
    private String descricao;

    // Diz ao Gson: "O JSON vai mandar 'criadoEm', mas guarde na variável 'horario'"
    @SerializedName("criadoEm")
    private String horario;

    public EventoSessao(String descricao, String horario) {
        this.descricao = descricao;
        this.horario = horario;
    }

    public String getDescricao() {
        return descricao;
    }

    // Adicionado o Getter do horário para você poder usar no Adapter e mostrar na tela!
    public String getHorario() {
        return horario;
    }
}