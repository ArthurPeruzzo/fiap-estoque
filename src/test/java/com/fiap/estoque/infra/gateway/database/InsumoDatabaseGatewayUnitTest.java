package com.fiap.estoque.infra.gateway.database;

import com.fiap.estoque.core.domain.Insumo;
import com.fiap.estoque.core.domain.UnidadeMedida;
import com.fiap.estoque.infra.gateway.entity.InsumoEntity;
import com.fiap.estoque.infra.gateway.repository.InsumoRepository;
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
class InsumoDatabaseGatewayUnitTest {

    @InjectMocks
    private InsumoDatabaseGateway gateway;

    @Mock
    private InsumoRepository insumoRepository;

    // -------------------------------------------------------------------------
    // criar
    // -------------------------------------------------------------------------

    @Test
    void criar_shouldSaveEntityWithCorrectFields() {
        var insumo = new Insumo("Óleo de motor", "Óleo 5W30", new BigDecimal("45.90"), UnidadeMedida.LITRO, 10);
        var captor = ArgumentCaptor.forClass(InsumoEntity.class);

        gateway.criar(insumo);

        Mockito.verify(insumoRepository).save(captor.capture());
        var entity = captor.getValue();
        assertEquals("Óleo de motor", entity.getNome());
        assertEquals("Óleo 5W30", entity.getDescricao());
        assertEquals(new BigDecimal("45.90"), entity.getPreco());
        assertEquals(10, entity.getQuantidadeEstoque());
        assertEquals(UnidadeMedida.LITRO, entity.getUnidadeMedida());
    }

    @Test
    void criar_shouldThrowErroAcessoBaseDeDadosExceptionWhenRepositoryFails() {
        var insumo = new Insumo("Óleo de motor", "Óleo 5W30", new BigDecimal("45.90"), UnidadeMedida.LITRO, 10);
        Mockito.when(insumoRepository.save(Mockito.any())).thenThrow(new RuntimeException("db error"));

        assertThrows(ErroAcessoBaseDeDadosException.class, () -> gateway.criar(insumo));
    }

    // -------------------------------------------------------------------------
    // buscarPorId
    // -------------------------------------------------------------------------

    @Test
    void buscarPorId_shouldReturnMappedInsumoWhenFound() {
        var entity = InsumoEntity.builder().id(1L).nome("Óleo").descricao("5W30").preco(new BigDecimal("45.90")).quantidadeEstoque(10).unidadeMedida(UnidadeMedida.LITRO).build();
        Mockito.when(insumoRepository.findById(1L)).thenReturn(Optional.of(entity));

        var result = gateway.buscarPorId(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals("Óleo", result.get().getNome());
        assertEquals(UnidadeMedida.LITRO, result.get().getUnidadeMedida());
    }

    @Test
    void buscarPorId_shouldReturnEmptyWhenNotFound() {
        Mockito.when(insumoRepository.findById(99L)).thenReturn(Optional.empty());

        assertTrue(gateway.buscarPorId(99L).isEmpty());
    }

    @Test
    void buscarPorId_shouldThrowErroAcessoBaseDeDadosExceptionWhenRepositoryFails() {
        Mockito.when(insumoRepository.findById(Mockito.anyLong())).thenThrow(new RuntimeException("db error"));

        assertThrows(ErroAcessoBaseDeDadosException.class, () -> gateway.buscarPorId(1L));
    }

    // -------------------------------------------------------------------------
    // atualizar
    // -------------------------------------------------------------------------

    @Test
    void atualizar_shouldSaveEntityWithId() {
        var insumo = Insumo.builder().id(1L).nome("Óleo novo").descricao("Desc nova").preco(new BigDecimal("50.00")).unidadeMedida(UnidadeMedida.ML).quantidadeEstoque(20).build();
        var captor = ArgumentCaptor.forClass(InsumoEntity.class);

        gateway.atualizar(insumo);

        Mockito.verify(insumoRepository).save(captor.capture());
        var entity = captor.getValue();
        assertEquals(1L, entity.getId());
        assertEquals("Óleo novo", entity.getNome());
        assertEquals(UnidadeMedida.ML, entity.getUnidadeMedida());
        assertEquals(20, entity.getQuantidadeEstoque());
    }

    @Test
    void atualizar_shouldThrowErroAcessoBaseDeDadosExceptionWhenRepositoryFails() {
        var insumo = Insumo.builder().id(1L).nome("Óleo").descricao("Desc").preco(new BigDecimal("45.90")).unidadeMedida(UnidadeMedida.LITRO).quantidadeEstoque(5).build();
        Mockito.when(insumoRepository.save(Mockito.any())).thenThrow(new RuntimeException("db error"));

        assertThrows(ErroAcessoBaseDeDadosException.class, () -> gateway.atualizar(insumo));
    }

    // -------------------------------------------------------------------------
    // deletar
    // -------------------------------------------------------------------------

    @Test
    void deletar_shouldCallDeleteById() {
        gateway.deletar(1L);

        Mockito.verify(insumoRepository).deleteById(1L);
    }

    @Test
    void deletar_shouldThrowErroAcessoBaseDeDadosExceptionWhenRepositoryFails() {
        Mockito.doThrow(new RuntimeException("db error")).when(insumoRepository).deleteById(Mockito.anyLong());

        assertThrows(ErroAcessoBaseDeDadosException.class, () -> gateway.deletar(1L));
    }

    // -------------------------------------------------------------------------
    // listar
    // -------------------------------------------------------------------------

    @Test
    void listar_shouldReturnMappedPagina() {
        var entity = InsumoEntity.builder().id(1L).nome("Óleo").descricao("5W30").preco(new BigDecimal("45.90")).quantidadeEstoque(10).unidadeMedida(UnidadeMedida.LITRO).build();
        var springPage = new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1);
        Mockito.when(insumoRepository.findAll(PageRequest.of(0, 10))).thenReturn(springPage);

        var resultado = gateway.listar(0, 10);

        assertEquals(1, resultado.content().size());
        assertEquals(1L, resultado.totalElements());
        assertEquals("Óleo", resultado.content().getFirst().getNome());
        assertEquals(UnidadeMedida.LITRO, resultado.content().getFirst().getUnidadeMedida());
    }

    @Test
    void listar_shouldReturnEmptyPaginaWhenNoResults() {
        var springPage = new PageImpl<InsumoEntity>(List.of(), PageRequest.of(0, 10), 0);
        Mockito.when(insumoRepository.findAll(PageRequest.of(0, 10))).thenReturn(springPage);

        var resultado = gateway.listar(0, 10);

        assertTrue(resultado.content().isEmpty());
        assertEquals(0L, resultado.totalElements());
    }

    @Test
    void listar_shouldThrowErroAcessoBaseDeDadosExceptionWhenRepositoryFails() {
        Mockito.when(insumoRepository.findAll(Mockito.any(PageRequest.class))).thenThrow(new RuntimeException("db error"));

        assertThrows(ErroAcessoBaseDeDadosException.class, () -> gateway.listar(0, 10));
    }
}
