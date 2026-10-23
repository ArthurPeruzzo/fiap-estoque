package com.fiap.estoque.shared.exception;


public class ErroAcessoBaseDeDadosException extends BaseException {

    private static final int STATUS_CODE = 500;
    private static final String MESSAGE = "Erro ao acessar base de dados";

    public ErroAcessoBaseDeDadosException() {
        super(STATUS_CODE, MESSAGE);
    }
}
