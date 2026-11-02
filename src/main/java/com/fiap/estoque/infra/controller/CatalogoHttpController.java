package com.fiap.estoque.infra.controller;

import com.fiap.estoque.core.gateway.InsumoGateway;
import com.fiap.estoque.core.gateway.PecaGateway;
import com.fiap.estoque.infra.controller.json.ItemCatalogoResponseJson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * API interna usada pela ordem de serviço (fiap-mecanica) para validar a existência e obter o preço
 * de peças e insumos em lote. Ids que não existem não aparecem na resposta.
 */
@RestController
@RequestMapping("/catalogo")
@Tag(name = "Catálogo")
public class CatalogoHttpController {

    private final CatalogoCleanController cleanController;

    public CatalogoHttpController(PecaGateway pecaGateway, InsumoGateway insumoGateway) {
        this.cleanController = new CatalogoCleanController(pecaGateway, insumoGateway);
    }

    @Operation(summary = "Consultar peças em lote",
            description = "Retorna id e preço das peças informadas. Ids inexistentes são omitidos da resposta.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Ids não informados"),
            @ApiResponse(responseCode = "401", description = "Token de serviço ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Token de serviço sem o escopo catalogo:leitura")
    })
    @GetMapping("/pecas")
    public ResponseEntity<List<ItemCatalogoResponseJson>> consultarPecas(@RequestParam List<Long> ids) {
        return ResponseEntity.ok(cleanController.consultarPecas(ids));
    }

    @Operation(summary = "Consultar insumos em lote",
            description = "Retorna id e preço dos insumos informados. Ids inexistentes são omitidos da resposta.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Ids não informados"),
            @ApiResponse(responseCode = "401", description = "Token de serviço ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Token de serviço sem o escopo catalogo:leitura")
    })
    @GetMapping("/insumos")
    public ResponseEntity<List<ItemCatalogoResponseJson>> consultarInsumos(@RequestParam List<Long> ids) {
        return ResponseEntity.ok(cleanController.consultarInsumos(ids));
    }
}
