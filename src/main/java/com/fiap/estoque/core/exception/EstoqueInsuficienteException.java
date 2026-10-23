package com.fiap.estoque.core.exception;

import com.fiap.estoque.shared.exception.BaseException;

public class EstoqueInsuficienteException extends BaseException {
	private static final int STATUS_CODE = 422;
	private static final String MESSAGE = "Estoque insuficiente para realizar a operação";

	public EstoqueInsuficienteException() {
		super(STATUS_CODE, MESSAGE);
	}
}
