package com.example.monitorforno.models;

import com.google.gson.annotations.SerializedName;

public class EventoDTO {
    private String id;
    private String tipo;

    // Diz ao Gson para mapear a chave "criadoEm" da API para esta variável
    @SerializedName("criadoEm")
    private String criadoEm;

    // Se a API não envia fornoId, isso ficará nulo. Se não for usar na tela, pode remover.
    private String fornoId;

    public EventoDTO() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getCriadoEm() { return criadoEm; }
    public void setCriadoEm(String criadoEm) { this.criadoEm = criadoEm; }

    public String getFornoId() { return fornoId; }
    public void setFornoId(String fornoId) { this.fornoId = fornoId; }
}