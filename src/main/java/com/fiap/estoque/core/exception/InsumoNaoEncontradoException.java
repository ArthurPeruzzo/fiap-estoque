package com.fiap.estoque.core.exception;

import com.fiap.estoque.shared.exception.BaseException;

public class InsumoNaoEncontradoException extends BaseException {

    private static final int STATUS_CODE = 404;
    private static final String MESSAGE = "Insumo não encontrado";

    public InsumoNaoEncontradoException() {
        super(STATUS_CODE, MESSAGE);
    }
}
