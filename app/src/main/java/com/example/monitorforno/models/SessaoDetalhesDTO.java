package com.example.monitorforno.models;

import java.util.List;

public class SessaoDetalhesDTO {
    private String id;
    private String inicioSessao;
    private String fimSessao;
    private String estadoFornoFinal;
    private String estadoSistema;
    private Long duracaoSegundos;
    private List<EventoDTO> eventos;
    private List<TemperaturaDTO> temperaturas;

    public String getId() { return id; }
    public String getInicioSessao() { return inicioSessao; }
    public String getFimSessao() { return fimSessao; }
    public String getEstadoFornoFinal() { return estadoFornoFinal; }
    public String getEstadoSistema() { return estadoSistema; }
    public Long getDuracaoSegundos() { return duracaoSegundos; }
    public List<EventoDTO> getEventos() { return eventos; }
    public List<TemperaturaDTO> getTemperaturas() { return temperaturas; }
}
