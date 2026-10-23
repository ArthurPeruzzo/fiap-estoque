package com.fiap.estoque.infra.controller;

import com.fiap.estoque.core.dto.AtualizarPecaDto;
import com.fiap.estoque.core.dto.CriarPecaDto;
import com.fiap.estoque.core.dto.ListarPecasDto;
import com.fiap.estoque.core.gateway.PecaGateway;
import com.fiap.estoque.infra.controller.json.PecaRequestJson;
import com.fiap.estoque.infra.controller.json.PecaResponseJson;
import com.fiap.estoque.shared.page.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/peca")
@Tag(name = "Peça")
public class PecaHttpController {

    private final PecaCleanController cleanController;

    public PecaHttpController(PecaGateway pecaGateway) {
        this.cleanController = new PecaCleanController(pecaGateway);
    }

    @Operation(summary = "Criar uma peça", description = "Cria uma nova peça no estoque")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Peça criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @PostMapping
    public ResponseEntity<Void> criar(@RequestBody @Valid PecaRequestJson request) {
        cleanController.criar(new CriarPecaDto(request.nome(), request.descricao(), request.preco(), request.quantidadeEstoque()));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Listar peças", description = "Retorna a lista paginada de peças cadastradas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @GetMapping
    public ResponseEntity<PageResponse<PecaResponseJson>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(PageResponse.from(cleanController.listar(new ListarPecasDto(page, size))));
    }

    @Operation(summary = "Atualizar uma peça", description = "Atualiza os dados de uma peça existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Peça atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Peça não encontrada"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Void> atualizar(@PathVariable Long id, @RequestBody @Valid PecaRequestJson request) {
        cleanController.atualizar(new AtualizarPecaDto(id, request.nome(), request.descricao(), request.preco(), request.quantidadeEstoque()));
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Deletar uma peça", description = "Remove uma peça pelo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Peça removida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Peça não encontrada"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        cleanController.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
