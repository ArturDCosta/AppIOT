package com.example.monitorforno.models;

public class TemporizadorResponseDTO {
    private String id;
    private String criadoEm;
    private String horarioFim;
    private boolean executado;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCriadoEm() { return criadoEm; }
    public void setCriadoEm(String criadoEm) { this.criadoEm = criadoEm; }

    public String getHorarioFim() { return horarioFim; }
    public void setHorarioFim(String horarioFim) { this.horarioFim = horarioFim; }

    public boolean isExecutado() { return executado; }
    public void setExecutado(boolean executado) { this.executado = executado; }
}