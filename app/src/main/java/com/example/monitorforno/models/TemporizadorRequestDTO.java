package com.example.monitorforno.models;

public class TemporizadorRequestDTO {
    private String horarioInicio;
    private String horarioFim;

    public TemporizadorRequestDTO(String horarioInicio, String horarioFim) {
        this.horarioInicio = horarioInicio;
        this.horarioFim = horarioFim;
    }

    public String getHorarioInicio() { return horarioInicio; }
    public void setHorarioInicio(String horarioInicio) { this.horarioInicio = horarioInicio; }

    public String getHorarioFim() { return horarioFim; }
    public void setHorarioFim(String horarioFim) { this.horarioFim = horarioFim; }
}