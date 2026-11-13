package com.example.monitorforno.utils;

import android.util.Log;

import org.json.JSONObject;

import retrofit2.Response;

/**
 * Transforma uma resposta de erro da API em uma mensagem que o usuário entende.
 *
 * Ordem de prioridade:
 *  1. A mensagem que o backend mandou no corpo do erro (ex.: {"message": "..."}),
 *     porque ela explica o motivo exato.
 *  2. Uma frase amigável escolhida pelo código HTTP (400, 401, 404, 409, 422, 500...).
 *
 * O código HTTP nunca aparece para o usuário; ele vai só para o Logcat (tag "ERRO_API").
 */
public final class ErroApi {

    private ErroApi() { } // classe utilitária: não precisa ser instanciada

    /**
     * @param response resposta com erro do Retrofit
     * @param acao     o que o usuário tentou fazer, ex.: "salvar o temporizador"
     */
    public static String mensagem(Response<?> response, String acao) {
        int codigo = response.code();

        // errorBody() só pode ser lido UMA vez, então lemos aqui e guardamos.
        String corpo = "";
        try {
            if (response.errorBody() != null) {
                corpo = response.errorBody().string();
            }
        } catch (Exception e) {
            Log.e("ERRO_API", "Falha ao ler o corpo do erro", e);
        }

        Log.e("ERRO_API", "HTTP " + codigo + " ao " + acao + ": " + corpo);

        String doBackend = extrairMensagem(corpo);
        if (doBackend != null) {
            return doBackend;
        }
        return "Não foi possível " + acao + ". " + motivoPorCodigo(codigo);
    }

    /** Mensagem para quando a requisição nem chegou ao servidor (onFailure). */
    public static String falhaDeConexao(String acao) {
        return "Não foi possível " + acao + ". Verifique sua conexão com a internet.";
    }

    // Procura um texto legível no JSON de erro (nomes comuns do Spring Boot).
    private static String extrairMensagem(String corpo) {
        if (corpo == null || corpo.trim().isEmpty()) return null;

        String texto = corpo.trim();
        if (texto.startsWith("{")) {
            try {
                JSONObject json = new JSONObject(texto);
                for (String campo : new String[]{"mensagem", "message", "erro", "detail"}) {
                    String valor = json.optString(campo, "").trim();
                    // Ignora mensagens técnicas genéricas que não ajudam o usuário.
                    if (!valor.isEmpty() && !valor.equalsIgnoreCase("No message available")) {
                        return valor;
                    }
                }
            } catch (Exception ignored) { }
            return null; // JSON sem mensagem útil (ex.: só "status" e "error")
        }

        // Texto puro e curto: provavelmente uma mensagem escrita pelo backend.
        // HTML ou textos enormes (stack trace) não são mostrados.
        if (!texto.startsWith("<") && texto.length() <= 200) {
            return texto;
        }
        return null;
    }

    private static String motivoPorCodigo(int codigo) {
        switch (codigo) {
            case 400: return "Confira os dados informados.";
            case 401: return "Sua sessão expirou. Faça login novamente.";
            case 402: return "Este recurso não está disponível para sua conta.";
            case 403: return "Você não tem permissão para fazer isso.";
            case 404: return "O item não foi encontrado.";
            case 408: return "O servidor demorou para responder. Tente novamente.";
            case 409: return "Já existe um registro igual a este.";
            case 422: return "Os dados enviados não são válidos. Confira as datas e os valores.";
            case 429: return "Muitas tentativas seguidas. Aguarde um pouco e tente de novo.";
            default:
                if (codigo >= 500) return "O servidor está com problemas. Tente novamente em instantes.";
                return "Tente novamente.";
        }
    }
}