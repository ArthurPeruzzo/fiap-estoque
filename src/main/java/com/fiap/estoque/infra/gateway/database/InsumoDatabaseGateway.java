package com.fiap.estoque.infra.gateway.database;

import com.fiap.estoque.core.domain.Insumo;
import com.fiap.estoque.core.gateway.InsumoGateway;
import com.fiap.estoque.infra.gateway.entity.InsumoEntity;
import com.fiap.estoque.infra.gateway.repository.InsumoRepository;
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
public class InsumoDatabaseGateway implements InsumoGateway {

	private final InsumoRepository insumoRepository;

	@Override
	public void criar(Insumo insumo) {
		try {
			insumoRepository.save(InsumoEntity.builder()
					.nome(insumo.getNome())
					.descricao(insumo.getDescricao())
					.preco(insumo.getPreco())
					.quantidadeEstoque(insumo.getEstoqueTotal())
					.quantidadeReservada(insumo.getQuantidadeReservada())
					.unidadeMedida(insumo.getUnidadeMedida())
					.build());
		} catch (Exception e) {
			log.error("Erro ao criar insumo", e);
			throw new ErroAcessoBaseDeDadosException();
		}
	}

	@Override
	public Optional<Insumo> buscarPorId(Long id) {
		try {
			return insumoRepository.findById(id)
					.map(this::paraDominio);
		} catch (Exception e) {
			log.error("Erro ao buscar insumo por id: {}", id, e);
			throw new ErroAcessoBaseDeDadosException();
		}
	}

	@Override
	public void atualizar(Insumo insumo) {
		try {
			insumoRepository.save(InsumoEntity.builder()
					.id(insumo.getId())
					.nome(insumo.getNome())
					.descricao(insumo.getDescricao())
					.preco(insumo.getPreco())
					.quantidadeEstoque(insumo.getEstoqueTotal())
					.quantidadeReservada(insumo.getQuantidadeReservada())
					.unidadeMedida(insumo.getUnidadeMedida())
					.build());
		} catch (Exception e) {
			log.error("Erro ao atualizar insumo", e);
			throw new ErroAcessoBaseDeDadosException();
		}
	}

	@Override
	public void deletar(Long id) {
		try {
			insumoRepository.deleteById(id);
		} catch (Exception e) {
			log.error("Erro ao deletar insumo por id: {}", id, e);
			throw new ErroAcessoBaseDeDadosException();
		}
	}

	@Override
	public Pagina<Insumo> listar(int page, int size) {
		try {
			var resultado = insumoRepository.findAll(PageRequest.of(page, size));
			var insumos = resultado.getContent().stream()
					.map(this::paraDominio)
					.toList();
			return new Pagina<>(insumos, resultado.getNumber(), resultado.getSize(), resultado.getTotalElements(), resultado.getTotalPages());
		} catch (Exception e) {
			log.error("Erro ao listar insumos", e);
			throw new ErroAcessoBaseDeDadosException();
		}
	}

	@Override
	public List<Insumo> listarPorIds(List<Long> insumosIds) {
		try {
			return insumoRepository.findAllById(insumosIds)
					.stream()
					.map(this::paraDominio)
					.toList();
		} catch (Exception e) {
			log.error("Erro ao listar insumos por ids: {}", insumosIds, e);
			throw new ErroAcessoBaseDeDadosException();
		}
	}

	private Insumo paraDominio(InsumoEntity e) {
		return Insumo.builder().id(e.getId()).nome(e.getNome()).descricao(e.getDescricao()).preco(e.getPreco()).unidadeMedida(e.getUnidadeMedida()).quantidadeEstoque(e.getQuantidadeEstoque()).quantidadeReservada(e.getQuantidadeReservada()).build();
	}
}
