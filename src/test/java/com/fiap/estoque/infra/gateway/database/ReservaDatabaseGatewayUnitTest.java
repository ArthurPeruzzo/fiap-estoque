package com.fiap.estoque.infra.gateway.database;

import com.fiap.estoque.core.domain.Reserva;
import com.fiap.estoque.core.domain.StatusReserva;
import com.fiap.estoque.core.domain.TipoItemReserva;
import com.fiap.estoque.infra.gateway.entity.ReservaEntity;
import com.fiap.estoque.infra.gateway.repository.ReservaRepository;
import com.fiap.estoque.shared.exception.ErroAcessoBaseDeDadosException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ReservaDatabaseGatewayUnitTest {

    @InjectMocks
    private ReservaDatabaseGateway gateway;

    @Mock
    private ReservaRepository reservaRepository;

    private ReservaEntity entity() {
        return ReservaEntity.builder().id(7L).ordemServicoId(1L).tipoItem(TipoItemReserva.PECA)
                .itemId(5L).quantidade(3).status(StatusReserva.ATIVA).build();
    }

    @Test
    void buscar_shouldMapEntityToDomain() {
        Mockito.when(reservaRepository.findByOrdemServicoIdAndTipoItemAndItemId(1L, TipoItemReserva.PECA, 5L))
                .thenReturn(Optional.of(entity()));

        var reserva = gateway.buscar(1L, TipoItemReserva.PECA, 5L).orElseThrow();

        assertEquals(7L, reserva.getId());
        assertEquals(3, reserva.getQuantidade());
        assertEquals(StatusReserva.ATIVA, reserva.getStatus());
    }

    @Test
    void listarAtivas_shouldFiltrarPorStatusAtiva() {
        Mockito.when(reservaRepository.findByOrdemServicoIdAndStatus(1L, StatusReserva.ATIVA)).thenReturn(List.of(entity()));

        assertEquals(1, gateway.listarAtivasPorOrdemDeServico(1L).size());
    }

    @Test
    void salvar_shouldPersistAllFields() {
        var reserva = Reserva.builder().id(7L).ordemServicoId(1L).tipoItem(TipoItemReserva.INSUMO).itemId(30L).quantidade(4).status(StatusReserva.CONFIRMADA).build();
        var captor = ArgumentCaptor.forClass(ReservaEntity.class);

        gateway.salvar(reserva);

        Mockito.verify(reservaRepository).save(captor.capture());
        var e = captor.getValue();
        assertEquals(7L, e.getId());
        assertEquals(TipoItemReserva.INSUMO, e.getTipoItem());
        assertEquals(30L, e.getItemId());
        assertEquals(4, e.getQuantidade());
        assertEquals(StatusReserva.CONFIRMADA, e.getStatus());
    }

    @Test
    void shouldThrowErroAcessoBaseDeDadosWhenRepositoryFails() {
        Mockito.when(reservaRepository.findByOrdemServicoIdAndTipoItemAndItemId(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenThrow(new RuntimeException("db"));
        Mockito.when(reservaRepository.findByOrdemServicoIdAndStatus(Mockito.any(), Mockito.any())).thenThrow(new RuntimeException("db"));
        Mockito.when(reservaRepository.save(Mockito.any())).thenThrow(new RuntimeException("db"));

        assertThrows(ErroAcessoBaseDeDadosException.class, () -> gateway.buscar(1L, TipoItemReserva.PECA, 5L));
        assertThrows(ErroAcessoBaseDeDadosException.class, () -> gateway.listarAtivasPorOrdemDeServico(1L));
        assertThrows(ErroAcessoBaseDeDadosException.class, () -> gateway.salvar(new Reserva(1L, TipoItemReserva.PECA, 5L)));
    }
}
