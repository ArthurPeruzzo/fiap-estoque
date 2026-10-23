package com.fiap.estoque.infra.gateway.database;

import com.fiap.estoque.core.domain.Peca;
import com.fiap.estoque.infra.gateway.entity.PecaEntity;
import com.fiap.estoque.infra.gateway.repository.PecaRepository;
import com.fiap.estoque.shared.exception.ErroAcessoBaseDeDadosException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PecaDatabaseGatewayUnitTest {

    @InjectMocks
    private PecaDatabaseGateway gateway;

    @Mock
    private PecaRepository pecaRepository;

    // -------------------------------------------------------------------------
    // criar
    // -------------------------------------------------------------------------

    @Test
    void criar_shouldSaveEntityWithCorrectFields() {
        var peca = new Peca("Filtro de óleo", "Filtro 1.0", new BigDecimal("29.90"), 10);
        var captor = ArgumentCaptor.forClass(PecaEntity.class);

        gateway.criar(peca);

        Mockito.verify(pecaRepository).save(captor.capture());
        var entity = captor.getValue();
        assertEquals("Filtro de óleo", entity.getNome());
        assertEquals("Filtro 1.0", entity.getDescricao());
        assertEquals(new BigDecimal("29.90"), entity.getPreco());
        assertEquals(10, entity.getQuantidadeEstoque());
    }

    @Test
    void criar_shouldThrowErroAcessoBaseDeDadosExceptionWhenRepositoryFails() {
        var peca = new Peca("Filtro de óleo", "Filtro 1.0", new BigDecimal("29.90"), 10);
        Mockito.when(pecaRepository.save(Mockito.any())).thenThrow(new RuntimeException("db error"));

        assertThrows(ErroAcessoBaseDeDadosException.class, () -> gateway.criar(peca));
    }

    // -------------------------------------------------------------------------
    // buscarPorId
    // -------------------------------------------------------------------------

    @Test
    void buscarPorId_shouldReturnMappedPecaWhenFound() {
        var entity = PecaEntity.builder().id(1L).nome("Filtro").descricao("Filtro 1.0").preco(new BigDecimal("29.90")).quantidadeEstoque(10).build();
        Mockito.when(pecaRepository.findById(1L)).thenReturn(Optional.of(entity));

        var result = gateway.buscarPorId(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals("Filtro", result.get().getNome());
        assertEquals(10, result.get().getEstoqueTotal());
    }

    @Test
    void buscarPorId_shouldReturnEmptyWhenNotFound() {
        Mockito.when(pecaRepository.findById(99L)).thenReturn(Optional.empty());

        assertTrue(gateway.buscarPorId(99L).isEmpty());
    }

    @Test
    void buscarPorId_shouldThrowErroAcessoBaseDeDadosExceptionWhenRepositoryFails() {
        Mockito.when(pecaRepository.findById(Mockito.anyLong())).thenThrow(new RuntimeException("db error"));

        assertThrows(ErroAcessoBaseDeDadosException.class, () -> gateway.buscarPorId(1L));
    }

    // -------------------------------------------------------------------------
    // atualizar
    // -------------------------------------------------------------------------

    @Test
    void atualizar_shouldSaveEntityWithId() {
        var peca = Peca.builder().id(1L).nome("Filtro novo").descricao("Desc nova").preco(new BigDecimal("35.00")).quantidadeEstoque(20).build();
        var captor = ArgumentCaptor.forClass(PecaEntity.class);

        gateway.atualizar(peca);

        Mockito.verify(pecaRepository).save(captor.capture());
        var entity = captor.getValue();
        assertEquals(1L, entity.getId());
        assertEquals("Filtro novo", entity.getNome());
        assertEquals("Desc nova", entity.getDescricao());
        assertEquals(new BigDecimal("35.00"), entity.getPreco());
        assertEquals(20, entity.getQuantidadeEstoque());
    }

    @Test
    void atualizar_shouldThrowErroAcessoBaseDeDadosExceptionWhenRepositoryFails() {
        var peca = Peca.builder().id(1L).nome("Filtro").descricao("Desc").preco(new BigDecimal("29.90")).quantidadeEstoque(5).build();
        Mockito.when(pecaRepository.save(Mockito.any())).thenThrow(new RuntimeException("db error"));

        assertThrows(ErroAcessoBaseDeDadosException.class, () -> gateway.atualizar(peca));
    }

    // -------------------------------------------------------------------------
    // deletar
    // -------------------------------------------------------------------------

    @Test
    void deletar_shouldCallDeleteById() {
        gateway.deletar(1L);

        Mockito.verify(pecaRepository).deleteById(1L);
    }

    @Test
    void deletar_shouldThrowErroAcessoBaseDeDadosExceptionWhenRepositoryFails() {
        Mockito.doThrow(new RuntimeException("db error")).when(pecaRepository).deleteById(Mockito.anyLong());

        assertThrows(ErroAcessoBaseDeDadosException.class, () -> gateway.deletar(1L));
    }

    // -------------------------------------------------------------------------
    // listar
    // -------------------------------------------------------------------------

    @Test
    void listar_shouldReturnMappedPagina() {
        var entity = PecaEntity.builder().id(1L).nome("Filtro").descricao("Desc").preco(new BigDecimal("29.90")).quantidadeEstoque(10).build();
        var springPage = new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1);
        Mockito.when(pecaRepository.findAll(PageRequest.of(0, 10))).thenReturn(springPage);

        var resultado = gateway.listar(0, 10);

        assertEquals(1, resultado.content().size());
        assertEquals(1L, resultado.totalElements());
        assertEquals(1L, resultado.content().getFirst().getId());
        assertEquals("Filtro", resultado.content().getFirst().getNome());
    }

    @Test
    void listar_shouldReturnEmptyPaginaWhenNoResults() {
        var springPage = new PageImpl<PecaEntity>(List.of(), PageRequest.of(0, 10), 0);
        Mockito.when(pecaRepository.findAll(PageRequest.of(0, 10))).thenReturn(springPage);

        var resultado = gateway.listar(0, 10);

        assertTrue(resultado.content().isEmpty());
        assertEquals(0L, resultado.totalElements());
    }

    @Test
    void listar_shouldThrowErroAcessoBaseDeDadosExceptionWhenRepositoryFails() {
        Mockito.when(pecaRepository.findAll(Mockito.any(PageRequest.class))).thenThrow(new RuntimeException("db error"));

        assertThrows(ErroAcessoBaseDeDadosException.class, () -> gateway.listar(0, 10));
    }
}
