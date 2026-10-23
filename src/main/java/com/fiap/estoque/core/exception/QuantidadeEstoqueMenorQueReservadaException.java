package com.fiap.estoque.core.exception;

import com.fiap.estoque.shared.exception.BaseException;

public class QuantidadeEstoqueMenorQueReservadaException extends BaseException {
	private static final int STATUS_CODE = 422;
	private static final String MESSAGE = "A quantidade em estoque não pode ser menor que a quantidade reservada para ordens de serviço";

	public QuantidadeEstoqueMenorQueReservadaException() {
		super(STATUS_CODE, MESSAGE);
	}
}
