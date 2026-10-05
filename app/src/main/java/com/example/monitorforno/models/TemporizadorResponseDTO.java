package com.example.monitorforno.models;

public class TemporizadorResponseDTO {
    private String id;
    private String horarioInicio;
    private String horarioFim;
    private Long duracaoSegundos;
    private boolean executado;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getHorarioInicio() { return horarioInicio; }
    public void setHorarioInicio(String horarioInicio) { this.horarioInicio = horarioInicio; }

    public String getHorarioFim() { return horarioFim; }
    public void setHorarioFim(String horarioFim) { this.horarioFim = horarioFim; }

    public Long getDuracaoSegundos() { return duracaoSegundos; }
    public void setDuracaoSegundos(Long duracaoSegundos) { this.duracaoSegundos = duracaoSegundos; }

    public boolean isExecutado() { return executado; }
    public void setExecutado(boolean executado) { this.executado = executado; }
}