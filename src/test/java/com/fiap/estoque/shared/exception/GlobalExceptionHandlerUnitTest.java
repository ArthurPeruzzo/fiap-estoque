package com.fiap.estoque.shared.exception;

import com.fiap.estoque.shared.exception.dto.ExceptionDto;
import com.fiap.estoque.core.exception.PecaNaoEncontradaException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerUnitTest {

    private static final String TIMESTAMP_PATTERN = "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}";

    @InjectMocks
    private GlobalExceptionHandler handler;

    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidException;

    @Mock
    private BindingResult bindingResult;

    @Test
    void exceptionHandler_shouldReturnInternalServerError() {
        Exception exception = new Exception("erro genérico");

        ResponseEntity<ExceptionDto> response = handler.exceptionHandler(exception);

        assertEquals(500, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().status());
        assertEquals("Ocorreu um erro inesperado: erro genérico", response.getBody().message());
        assertTrue(response.getBody().timestamp().matches(TIMESTAMP_PATTERN));
    }

    @Test
    void baseExceptionHandler_shouldReturnStatusAndMessageFromException() {
        PecaNaoEncontradaException exception = new PecaNaoEncontradaException();

        ResponseEntity<ExceptionDto> response = handler.baseExceptionHandler(exception);

        assertEquals(404, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().status());
        assertEquals("Peça não encontrada", response.getBody().message());
        assertTrue(response.getBody().timestamp().matches(TIMESTAMP_PATTERN));
    }

    @Test
    void illegalArgumentExceptionHandler_shouldReturnBadRequest() {
        IllegalArgumentException exception = new IllegalArgumentException("argumento inválido");

        ResponseEntity<ExceptionDto> response = handler.illegalArgumentExceptionHandler(exception);

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertEquals("argumento inválido", response.getBody().message());
        assertTrue(response.getBody().timestamp().matches(TIMESTAMP_PATTERN));
    }

    @Test
    void dataIntegrityViolationExceptionHandler_shouldReturnBadRequest() {
        DataIntegrityViolationException exception = new DataIntegrityViolationException("violação de integridade");

        ResponseEntity<ExceptionDto> response = handler.dataIntegrityViolationExceptionHandler(exception);

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertEquals("violação de integridade", response.getBody().message());
        assertTrue(response.getBody().timestamp().matches(TIMESTAMP_PATTERN));
    }

    @Test
    void httpRequestMethodNotSupportedExceptionHandler_shouldReturnBadRequestWithMethodName() {
        HttpRequestMethodNotSupportedException exception = new HttpRequestMethodNotSupportedException("DELETE");

        ResponseEntity<ExceptionDto> response = handler.httpRequestMethodNotSupportedExceptionHandler(exception);

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertEquals("Método DELETE não suportado", response.getBody().message());
        assertTrue(response.getBody().timestamp().matches(TIMESTAMP_PATTERN));
    }

    @Test
    void httpMessageNotReadableExceptionHandler_shouldReturnBadRequestWithoutLeakingParserDetails() {
        HttpMessageNotReadableException exception =
                new HttpMessageNotReadableException("JSON parse error: valor interno", Mockito.mock(HttpInputMessage.class));

        ResponseEntity<ExceptionDto> response = handler.httpMessageNotReadableExceptionHandler(exception);

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertEquals("Corpo da requisição inválido ou malformado", response.getBody().message());
        assertTrue(response.getBody().timestamp().matches(TIMESTAMP_PATTERN));
    }

    @Test
    void methodArgumentNotValidExceptionHandler_shouldReturnMapOfFieldErrors() {
        FieldError fieldError = new FieldError("object", "email", "não pode ser nulo");

        Mockito.when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        Mockito.when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<Map<String, String>> response = handler.methodArgumentNotValidExceptionHandler(methodArgumentNotValidException);

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("não pode ser nulo", response.getBody().get("email"));
    }
}
