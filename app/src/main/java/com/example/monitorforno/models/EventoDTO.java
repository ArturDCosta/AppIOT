package com.example.monitorforno.models;

public class EventoDTO {
    private String id;
    private String tipo; // Ex: "ALERTA_ENTRADA", "CRITICO_ENTRADA"
    private String horario;
    private String data; // Opcional, mas bom ter
    private String fornoId;

    // Construtor vazio para o Retrofit
    public EventoDTO() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getHorario() { return horario; }
    public void setHorario(String horario) { this.horario = horario; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public String getFornoId() { return fornoId; }
    public void setFornoId(String fornoId) { this.fornoId = fornoId; }
}
