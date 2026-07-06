package com.example.monitorforno.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

public class TemperaturaDTO {
    private String id;

    @SerializedName("temperaturaAtual")
    private Double temperaturaAtual;

    @SerializedName("temperaturaUltima")
    private Double temperaturaUltima;

    @SerializedName("registradoEm")
    private JsonElement registradoEm;

    public Double getTemperaturaAtual() {
        return temperaturaAtual != null ? temperaturaAtual : 0.0;
    }

    // Extrai a hora (ex: "14:30:15") independentemente se o Spring Boot mandar como String ou Array
    public String getHorarioFormatado() {
        if (registradoEm == null || registradoEm.isJsonNull()) return "--:--";

        try {
            if (registradoEm.isJsonPrimitive()) {
                String dataString = registradoEm.getAsString();
                if (dataString.contains("T")) {
                    String hora = dataString.split("T")[1];
                    return hora.length() >= 8 ? hora.substring(0, 8) : hora;
                }
                return dataString;
            } else if (registradoEm.isJsonArray()) {
                JsonArray array = registradoEm.getAsJsonArray();
                if (array.size() >= 6) {
                    return String.format("%02d:%02d:%02d", array.get(3).getAsInt(), array.get(4).getAsInt(), array.get(5).getAsInt());
                } else if (array.size() >= 5) {
                    return String.format("%02d:%02d", array.get(3).getAsInt(), array.get(4).getAsInt());
                }
            }
        } catch (Exception ignored) {}
        return "--:--";
    }
}