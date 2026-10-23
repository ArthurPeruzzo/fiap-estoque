package com.fiap.estoque.core.gateway;

import com.fiap.estoque.core.domain.Reserva;
import com.fiap.estoque.core.domain.TipoItemReserva;

import java.util.List;
import java.util.Optional;

public interface ReservaGateway {
    Optional<Reserva> buscar(Long ordemServicoId, TipoItemReserva tipoItem, Long itemId);
    List<Reserva> listarAtivasPorOrdemDeServico(Long ordemServicoId);
    void salvar(Reserva reserva);
}
