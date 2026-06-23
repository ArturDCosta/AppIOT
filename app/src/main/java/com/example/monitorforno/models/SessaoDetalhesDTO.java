package com.example.monitorforno.models;

import java.util.List;

public class SessaoDetalhesDTO {

    // 1. Informações Básicas
    private String id;
    private String data;
    private String horarioInicio;
    private String horarioFim;
    private String duracao;
    private String estadoFinal;

    // 2. Métricas Calculadas
    private Double temperaturaMaxima;
    private Double temperaturaMedia;
    private Integer quantidadeAlertas;
    private Integer quantidadeCriticos;

    // 3. Dados para o Gráfico (Eixo Y e Eixo X)
    private List<Float> temperaturasLista;
    private List<String> horariosLista;

    // 4. Lista de Eventos (Para o RecyclerView)
    private List<EventoSessao> eventos;

    // ==========================================
    // GETTERS (Obrigatórios para o Retrofit ler os dados)
    // ==========================================

    public String getId() {
        return id;
    }

    public String getData() {
        return data;
    }

    public String getHorarioInicio() {
        return horarioInicio;
    }

    public String getHorarioFim() {
        return horarioFim;
    }

    public String getDuracao() {
        return duracao;
    }

    public String getEstadoFinal() {
        return estadoFinal;
    }

    public Double getTemperaturaMaxima() {
        return temperaturaMaxima != null ? temperaturaMaxima : 0.0;
    }

    public Double getTemperaturaMedia() {
        return temperaturaMedia != null ? temperaturaMedia : 0.0;
    }

    public Integer getQuantidadeAlertas() {
        return quantidadeAlertas != null ? quantidadeAlertas : 0;
    }

    public Integer getQuantidadeCriticos() {
        return quantidadeCriticos != null ? quantidadeCriticos : 0;
    }

    public List<Float> getTemperaturasLista() {
        return temperaturasLista;
    }

    public List<String> getHorariosLista() {
        return horariosLista;
    }

    public List<EventoSessao> getEventos() {
        return eventos;
    }
}