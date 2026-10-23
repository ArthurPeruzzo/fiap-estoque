package com.fiap.estoque.shared.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ExceptionsUnitTest {

    // -------------------------------------------------------------------------
    // BaseException
    // -------------------------------------------------------------------------

    @Test
    void baseException_shouldStoreStatusAndMessage() {
        BaseException exception = new BaseException(400, "mensagem de teste") {};

        assertEquals(400, exception.getStatusCode());
        assertEquals("mensagem de teste", exception.getMessage());
    }

    @Test
    void baseException_shouldExtendRuntimeException() {
        BaseException exception = new BaseException(400, "mensagem de teste") {};

        assertInstanceOf(RuntimeException.class, exception);
    }

    // -------------------------------------------------------------------------
    // ErroAcessoBaseDeDadosException
    // -------------------------------------------------------------------------

    @Test
    void erroAcessoBaseDeDadosException_shouldExtendedBaseExceptionAndReturnInternalServerErrorAndCorrectMessage() {
        ErroAcessoBaseDeDadosException exception = new ErroAcessoBaseDeDadosException();

        assertInstanceOf(BaseException.class, exception);
        assertEquals(500, exception.getStatusCode());
        assertEquals("Erro ao acessar base de dados", exception.getMessage());
    }
}
