package com.fiap.estoque.infra.gateway.database;

import com.fiap.estoque.core.domain.Peca;
import com.fiap.estoque.core.gateway.PecaGateway;
import com.fiap.estoque.infra.gateway.entity.PecaEntity;
import com.fiap.estoque.infra.gateway.repository.PecaRepository;
import com.fiap.estoque.shared.exception.ErroAcessoBaseDeDadosException;
import com.fiap.estoque.shared.page.Pagina;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class PecaDatabaseGateway implements PecaGateway {

	private final PecaRepository pecaRepository;

	@Override
	public void criar(Peca peca) {
		try {
			pecaRepository.save(PecaEntity.builder()
					.nome(peca.getNome())
					.descricao(peca.getDescricao())
					.preco(peca.getPreco())
					.quantidadeEstoque(peca.getEstoqueTotal())
					.quantidadeReservada(peca.getQuantidadeReservada())
					.build());
		} catch (Exception e) {
			log.error("Erro ao criar peca", e);
			throw new ErroAcessoBaseDeDadosException();
		}
	}

	@Override
	public Optional<Peca> buscarPorId(Long id) {
		try {
			return pecaRepository.findById(id)
					.map(this::paraDominio);
		} catch (Exception e) {
			log.error("Erro ao buscar peca por id: {}", id, e);
			throw new ErroAcessoBaseDeDadosException();
		}
	}

	@Override
	public void atualizar(Peca peca) {
		try {
			pecaRepository.save(PecaEntity.builder()
					.id(peca.getId())
					.nome(peca.getNome())
					.descricao(peca.getDescricao())
					.preco(peca.getPreco())
					.quantidadeEstoque(peca.getEstoqueTotal())
					.quantidadeReservada(peca.getQuantidadeReservada())
					.build());
		} catch (Exception e) {
			log.error("Erro ao atualizar peca", e);
			throw new ErroAcessoBaseDeDadosException();
		}
	}

	@Override
	public void deletar(Long id) {
		try {
			pecaRepository.deleteById(id);
		} catch (Exception e) {
			log.error("Erro ao deletar peca por id: {}", id, e);
			throw new ErroAcessoBaseDeDadosException();
		}
	}

	@Override
	public Pagina<Peca> listar(int page, int size) {
		try {
			var resultado = pecaRepository.findAll(PageRequest.of(page, size));
			var pecas = resultado.getContent().stream()
					.map(this::paraDominio)
					.toList();
			return new Pagina<>(pecas, resultado.getNumber(), resultado.getSize(), resultado.getTotalElements(), resultado.getTotalPages());
		} catch (Exception e) {
			log.error("Erro ao listar pecas", e);
			throw new ErroAcessoBaseDeDadosException();
		}
	}

	@Override
	public List<Peca> listarPorIds(List<Long> pecasIds) {
		try {
			return pecaRepository.findAllById(pecasIds)
					.stream()
					.map(this::paraDominio)
					.toList();
		} catch (Exception e) {
			log.error("Erro ao listar pecas por ids: {}", pecasIds, e);
			throw new ErroAcessoBaseDeDadosException();
		}
	}

	private Peca paraDominio(PecaEntity e) {
		return Peca.builder().id(e.getId()).nome(e.getNome()).descricao(e.getDescricao()).preco(e.getPreco()).quantidadeEstoque(e.getQuantidadeEstoque()).quantidadeReservada(e.getQuantidadeReservada()).build();
	}
}
