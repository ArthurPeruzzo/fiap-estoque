package com.fiap.estoque.infra.controller;

import com.fiap.estoque.core.dto.AtualizarInsumoDto;
import com.fiap.estoque.core.dto.CriarInsumoDto;
import com.fiap.estoque.core.dto.ListarInsumosDto;
import com.fiap.estoque.core.gateway.InsumoGateway;
import com.fiap.estoque.infra.controller.json.InsumoRequestJson;
import com.fiap.estoque.infra.controller.json.InsumoResponseJson;
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
@RequestMapping("/insumo")
@Tag(name = "Insumo")
public class InsumoHttpController {

    private final InsumoCleanController cleanController;

    public InsumoHttpController(InsumoGateway insumoGateway) {
        this.cleanController = new InsumoCleanController(insumoGateway);
    }

    @Operation(summary = "Criar um insumo", description = "Cria um novo insumo no estoque")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Insumo criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @PostMapping
    public ResponseEntity<Void> criar(@RequestBody @Valid InsumoRequestJson request) {
        cleanController.criar(new CriarInsumoDto(request.nome(), request.descricao(), request.preco(), request.quantidadeEstoque(), request.unidadeMedida()));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Listar insumos", description = "Retorna a lista paginada de insumos cadastrados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @GetMapping
    public ResponseEntity<PageResponse<InsumoResponseJson>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(PageResponse.from(cleanController.listar(new ListarInsumosDto(page, size))));
    }

    @Operation(summary = "Atualizar um insumo", description = "Atualiza os dados de um insumo existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Insumo atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Insumo não encontrado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Void> atualizar(@PathVariable Long id, @RequestBody @Valid InsumoRequestJson request) {
        cleanController.atualizar(new AtualizarInsumoDto(id, request.nome(), request.descricao(), request.preco(), request.unidadeMedida(), request.quantidadeEstoque()));
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Deletar um insumo", description = "Remove um insumo pelo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Insumo removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Insumo não encontrado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        cleanController.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
