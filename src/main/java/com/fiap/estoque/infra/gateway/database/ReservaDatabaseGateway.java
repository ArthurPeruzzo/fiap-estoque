package com.fiap.estoque.infra.gateway.database;

import com.fiap.estoque.core.domain.Reserva;
import com.fiap.estoque.core.domain.StatusReserva;
import com.fiap.estoque.core.domain.TipoItemReserva;
import com.fiap.estoque.core.gateway.ReservaGateway;
import com.fiap.estoque.infra.gateway.entity.ReservaEntity;
import com.fiap.estoque.infra.gateway.repository.ReservaRepository;
import com.fiap.estoque.shared.exception.ErroAcessoBaseDeDadosException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class ReservaDatabaseGateway implements ReservaGateway {

	private final ReservaRepository reservaRepository;

	@Override
	public Optional<Reserva> buscar(Long ordemServicoId, TipoItemReserva tipoItem, Long itemId) {
		try {
			return reservaRepository.findByOrdemServicoIdAndTipoItemAndItemId(ordemServicoId, tipoItem, itemId)
					.map(this::toDomain);
		} catch (Exception e) {
			log.error("Erro ao buscar reserva da ordem {} item {} {}", ordemServicoId, tipoItem, itemId, e);
			throw new ErroAcessoBaseDeDadosException();
		}
	}

	@Override
	public List<Reserva> listarAtivasPorOrdemDeServico(Long ordemServicoId) {
		try {
			return reservaRepository.findByOrdemServicoIdAndStatus(ordemServicoId, StatusReserva.ATIVA)
					.stream()
					.map(this::toDomain)
					.toList();
		} catch (Exception e) {
			log.error("Erro ao listar reservas ativas da ordem {}", ordemServicoId, e);
			throw new ErroAcessoBaseDeDadosException();
		}
	}

	@Override
	public void salvar(Reserva reserva) {
		try {
			reservaRepository.save(ReservaEntity.builder()
					.id(reserva.getId())
					.ordemServicoId(reserva.getOrdemServicoId())
					.tipoItem(reserva.getTipoItem())
					.itemId(reserva.getItemId())
					.quantidade(reserva.getQuantidade())
					.status(reserva.getStatus())
					.build());
		} catch (Exception e) {
			log.error("Erro ao salvar reserva da ordem {}", reserva.getOrdemServicoId(), e);
			throw new ErroAcessoBaseDeDadosException();
		}
	}

	private Reserva toDomain(ReservaEntity e) {
		return Reserva.builder().id(e.getId()).ordemServicoId(e.getOrdemServicoId()).tipoItem(e.getTipoItem()).itemId(e.getItemId()).quantidade(e.getQuantidade()).status(e.getStatus()).build();
	}
}
