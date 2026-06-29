package com.example.monitorforno.models;

public class TemporizadorRequestDTO {
    private String horarioFim;

    public TemporizadorRequestDTO(String horarioFim) {
        this.horarioFim = horarioFim;
    }

    public String getHorarioFim() {
        return horarioFim;
    }

    public void setHorarioFim(String horarioFim) {
        this.horarioFim = horarioFim;
    }
}