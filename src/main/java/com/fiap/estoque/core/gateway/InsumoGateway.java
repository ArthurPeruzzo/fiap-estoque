package com.fiap.estoque.core.gateway;

import com.fiap.estoque.core.domain.Insumo;
import com.fiap.estoque.shared.page.Pagina;

import java.util.List;
import java.util.Optional;

public interface InsumoGateway {
    void criar(Insumo insumo);
    Optional<Insumo> buscarPorId(Long id);
    void atualizar(Insumo insumo);
    void deletar(Long id);
    Pagina<Insumo> listar(int page, int size);
    List<Insumo> listarPorIds(List<Long> insumosIds);
}
