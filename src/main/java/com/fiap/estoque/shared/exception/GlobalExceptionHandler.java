package com.fiap.estoque.shared.exception;

import com.fiap.estoque.shared.exception.dto.ExceptionDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int INTERNAL_SERVER_ERROR = 500;
    private static final int BAD_REQUEST = 400;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionDto> exceptionHandler(Exception e) {
        log.error("Erro inesperado não tratado", e);
        return ResponseEntity
                .status(INTERNAL_SERVER_ERROR).body(
                        new ExceptionDto(INTERNAL_SERVER_ERROR, LocalDateTime.now().format(FORMATTER), "Ocorreu um erro inesperado: " + e.getMessage())
                );
    }

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ExceptionDto> baseExceptionHandler(BaseException e) {
        // 5xx é falha de infra/inesperada (ex.: ErroAcessoBaseDeDadosException) — merece stack
        // trace completo. 4xx é regra de negócio em fluxo esperado (não encontrado, conflito,
        // credenciais inválidas) — WARN com só a mensagem evita poluir o log com stack trace de
        // algo que não é bug.
        if (e.getStatusCode() >= INTERNAL_SERVER_ERROR) {
            log.error(e.getMessage(), e);
        } else {
            log.warn(e.getMessage());
        }
        return ResponseEntity
                .status(e.getStatusCode()).body(
                        new ExceptionDto(e.getStatusCode(), LocalDateTime.now().format(FORMATTER), e.getMessage())
                );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ExceptionDto> illegalArgumentExceptionHandler(IllegalArgumentException e){
        log.warn(e.getMessage());
        return ResponseEntity.status(BAD_REQUEST).body(
                new ExceptionDto(BAD_REQUEST, LocalDateTime.now().format(FORMATTER), e.getMessage())
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ExceptionDto> dataIntegrityViolationExceptionHandler(DataIntegrityViolationException e){
        log.warn(e.getMessage());
        return ResponseEntity.status(BAD_REQUEST).body(
                new ExceptionDto(BAD_REQUEST, LocalDateTime.now().format(FORMATTER), e.getMessage())
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ExceptionDto> httpRequestMethodNotSupportedExceptionHandler(HttpRequestMethodNotSupportedException e){
        log.warn("Método {} não suportado", e.getMethod());
        return ResponseEntity.status(BAD_REQUEST).body(
                new ExceptionDto(BAD_REQUEST, LocalDateTime.now().format(FORMATTER), "Método " + e.getMethod() + " não suportado")
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ExceptionDto> missingServletRequestParameterExceptionHandler(MissingServletRequestParameterException e){
        log.warn("Parâmetro obrigatório ausente: {}", e.getParameterName());
        return ResponseEntity.status(BAD_REQUEST).body(
                new ExceptionDto(BAD_REQUEST, LocalDateTime.now().format(FORMATTER), "O parâmetro '" + e.getParameterName() + "' deve ser informado")
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ExceptionDto> httpMessageNotReadableExceptionHandler(HttpMessageNotReadableException e){
        log.warn("Corpo da requisição ilegível: {}", e.getMessage());
        return ResponseEntity.status(BAD_REQUEST).body(
                new ExceptionDto(BAD_REQUEST, LocalDateTime.now().format(FORMATTER), "Corpo da requisição inválido ou malformado")
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getGlobalErrors().forEach(error ->
                errors.put(error.getObjectName(), error.getDefaultMessage())
        );

        if (errors.isEmpty()) {
            ex.getBindingResult().getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
        }
        log.warn("Validação falhou: {}", errors);
        return ResponseEntity.status(BAD_REQUEST).body(errors);
    }

}
