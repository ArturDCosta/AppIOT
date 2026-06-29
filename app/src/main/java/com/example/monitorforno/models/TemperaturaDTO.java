package com.example.monitorforno.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public class TemperaturaDTO {
    private String id;
    private Double temperaturaAtual;
    private Double temperaturaUltima;

    // O JsonElement permite aceitar o formato que vier da API (String ou Array) sem dar erro de conversão
    private JsonElement registradoEm;

    public String getId() { return id; }
    public Double getTemperaturaAtual() { return temperaturaAtual; }
    public Double getTemperaturaUltima() { return temperaturaUltima; }

    // Método inteligente para devolver a hora já formatada para o gráfico
    public String getHorarioFormatado() {
        if (registradoEm == null || registradoEm.isJsonNull()) {
            return "--:--:--";
        }

        try {
            // 1. Se a API enviar como String (ex: "2026-06-29T19:19:50")
            if (registradoEm.isJsonPrimitive()) {
                String dataString = registradoEm.getAsString();
                if (dataString.contains("T")) {
                    String horaComMilissegundos = dataString.split("T")[1];
                    if (horaComMilissegundos.length() >= 8) {
                        return horaComMilissegundos.substring(0, 8);
                    }
                    return horaComMilissegundos;
                }
                return dataString;
            }
            // 2. Se a API enviar como Array sem formatação (ex: [2026, 6, 29, 19, 19, 50])
            else if (registradoEm.isJsonArray()) {
                JsonArray array = registradoEm.getAsJsonArray();
                if (array.size() >= 6) { // Assegura que tem horas, minutos e segundos
                    int hora = array.get(3).getAsInt();
                    int minuto = array.get(4).getAsInt();
                    int segundo = array.get(5).getAsInt();
                    // Formata para manter dois dígitos (ex: 09:05:02)
                    return String.format("%02d:%02d:%02d", hora, minuto, segundo);
                } else if (array.size() >= 5) {
                    int hora = array.get(3).getAsInt();
                    int minuto = array.get(4).getAsInt();
                    return String.format("%02d:%02d:00", hora, minuto);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "--:--:--";
    }
}