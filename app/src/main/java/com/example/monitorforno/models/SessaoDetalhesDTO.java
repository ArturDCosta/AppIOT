package com.example.monitorforno.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SessaoDetalhesDTO {

    private String id;

    @SerializedName("inicioSessao")
    private String horarioInicio;

    @SerializedName("fimSessao")
    private String horarioFim;

    @SerializedName("duracaoSegundos")
    private Long duracaoSegundos;

    @SerializedName("estadoFornoFinal")
    private String estadoFinal;

    @SerializedName("temperaturas")
    private List<TemperaturaDTO> temperaturas;

    @SerializedName("eventos")
    private List<EventoSessao> eventos;

    // Métricas opcionais (se você for calcular no app ou se a API mandar no futuro)
    private Double temperaturaMaxima;
    private Double temperaturaMedia;

    // Getters
    public String getId() { return id; }
    public String getHorarioInicio() { return horarioInicio; }
    public String getHorarioFim() { return horarioFim; }
    public Long getDuracaoSegundos() { return duracaoSegundos; }
    public String getEstadoFinal() { return estadoFinal; }
    public List<TemperaturaDTO> getTemperaturas() { return temperaturas; }
    public List<EventoSessao> getEventos() { return eventos; }

    public Double getTemperaturaMaxima() { return temperaturaMaxima != null ? temperaturaMaxima : 0.0; }
    public Double getTemperaturaMedia() { return temperaturaMedia != null ? temperaturaMedia : 0.0; }
}