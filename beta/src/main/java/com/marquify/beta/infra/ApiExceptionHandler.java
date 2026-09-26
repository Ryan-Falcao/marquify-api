package com.marquify.beta.infra;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {
    public record ApiError(int status, String codigo, String mensagem) {}

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> forbidden() {
        return error(403, "ACESSO_NEGADO", "Acesso negado");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> unauthorized() {
        return ResponseEntity.status(401).header("WWW-Authenticate", "Bearer")
                .body(new ApiError(401, "NAO_AUTENTICADO", "Autenticação necessária ou inválida"));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ApiError> invalid() {
        return error(400, "REQUISICAO_INVALIDA", "Verifique os campos da requisição");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> conflict() {
        return error(409, "CONFLITO", "A operação conflita com os dados existentes");
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> status(ResponseStatusException exception) {
        int status = exception.getStatusCode().value();
        String code = switch (status) {
            case 404 -> "NAO_ENCONTRADO";
            case 409 -> "CONFLITO";
            default -> "REQUISICAO_INVALIDA";
        };
        return error(status, code, exception.getReason() == null ? "Operação inválida" : exception.getReason());
    }

    private ResponseEntity<ApiError> error(int status, String code, String message) {
        return ResponseEntity.status(status).body(new ApiError(status, code, message));
    }
}
