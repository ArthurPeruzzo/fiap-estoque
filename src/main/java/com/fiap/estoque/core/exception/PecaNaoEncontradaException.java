package com.fiap.estoque.core.exception;

import com.fiap.estoque.shared.exception.BaseException;

public class PecaNaoEncontradaException extends BaseException {

    private static final int STATUS_CODE = 404;
    private static final String MESSAGE = "Peça não encontrada";

    public PecaNaoEncontradaException() {
        super(STATUS_CODE, MESSAGE);
    }
}
