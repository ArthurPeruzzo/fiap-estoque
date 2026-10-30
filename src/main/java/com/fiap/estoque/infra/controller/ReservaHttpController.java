package com.fiap.estoque.infra.controller;

import com.fiap.estoque.core.dto.ItemReservaDto;
import com.fiap.estoque.core.gateway.ItemEstoqueGateway;
import com.fiap.estoque.core.gateway.ReservaGateway;
import com.fiap.estoque.core.gateway.TransacaoGateway;
import com.fiap.estoque.infra.controller.json.ReservarItensRequestJson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API interna usada pela ordem de serviço (fiap-mecanica). Cada operação roda em uma única
 * transação (controlada pelos casos de uso): ou todos os itens são reservados, ou nenhum.
 */
@RestController
@RequestMapping("/reserva/ordem-servico/{ordemServicoId}")
@Tag(name = "Reserva")
public class ReservaHttpController {

    private final ReservaCleanController cleanController;

    public ReservaHttpController(ItemEstoqueGateway itemEstoqueGateway, ReservaGateway reservaGateway,
                                 TransacaoGateway transacaoGateway) {
        this.cleanController = new ReservaCleanController(itemEstoqueGateway, reservaGateway, transacaoGateway);
    }

    @Operation(summary = "Reservar itens em lote",
            description = "Define a quantidade total reservada de cada item (peça ou insumo) para a ordem de serviço. "
                    + "Idempotente e atômico: se algum item não puder ser reservado, nenhum é alterado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Itens reservados com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Token de serviço ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Token de serviço sem o escopo reserva:escrita"),
            @ApiResponse(responseCode = "404", description = "Peça ou insumo não encontrado"),
            @ApiResponse(responseCode = "422", description = "Estoque insuficiente para algum dos itens")
    })
    @PutMapping("/itens")
    public ResponseEntity<Void> reservar(@PathVariable Long ordemServicoId,
                                         @RequestBody @Valid ReservarItensRequestJson request) {
        var itens = request.itens().stream()
                .map(i -> new ItemReservaDto(i.tipoItem(), i.itemId(), i.quantidadeTotal()))
                .toList();
        cleanController.reservar(ordemServicoId, itens);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Confirmar reservas", description = "Converte as reservas ativas da ordem de serviço em baixa definitiva do estoque")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Reservas confirmadas com sucesso")
    })
    @PostMapping("/confirmar")
    public ResponseEntity<Void> confirmar(@PathVariable Long ordemServicoId) {
        cleanController.confirmar(ordemServicoId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Liberar reservas", description = "Devolve ao saldo disponível tudo que estava reservado para a ordem de serviço")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Reservas liberadas com sucesso")
    })
    @PostMapping("/liberar")
    public ResponseEntity<Void> liberar(@PathVariable Long ordemServicoId) {
        cleanController.liberar(ordemServicoId);
        return ResponseEntity.noContent().build();
    }
}
