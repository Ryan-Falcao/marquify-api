package com.marquify.beta.infra.security;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public final class SecurityErrors {
    private SecurityErrors() {}

    public static void unauthorized(HttpServletResponse response) throws IOException {
        response.setHeader("WWW-Authenticate", "Bearer");
        write(response, 401, "NAO_AUTENTICADO", "Autenticação necessária ou inválida");
    }

    public static void forbidden(HttpServletResponse response) throws IOException {
        write(response, 403, "ACESSO_NEGADO", "Acesso negado");
    }

    private static void write(HttpServletResponse response, int status, String code, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"status\":" + status + ",\"codigo\":\"" + code
                + "\",\"mensagem\":\"" + message + "\"}");
    }
}
