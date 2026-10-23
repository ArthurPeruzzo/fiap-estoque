package com.fiap.estoque.core.gateway;

import com.fiap.estoque.core.domain.Peca;
import com.fiap.estoque.shared.page.Pagina;

import java.util.List;
import java.util.Optional;

public interface PecaGateway {
    void criar(Peca peca);
    Optional<Peca> buscarPorId(Long id);
    void atualizar(Peca peca);
    void deletar(Long id);
    Pagina<Peca> listar(int page, int size);
    List<Peca> listarPorIds(List<Long> pecasIds);
}
