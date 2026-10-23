package com.fiap.estoque.infra.controller.json;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(
        name = "PecaRequestJson",
        description = "Dados necessários para criar uma peca"
)
public record PecaRequestJson(
        @Schema(
                description = "Nome da peca",
                example = "Biela"
        )
        @NotBlank(message = "O nome deve ser preenchido")
        String nome,

        @Schema(
                description = "Descrição da peca",
                example = "Transmite movimento do pistão ao virabrequim."
        )
        @NotBlank(message = "A descrição deve ser preenchida")
        String descricao,

        @Schema(
                description = "Preço da peca",
                example = "100.00"
        )
        @NotNull(message = "O preço deve ser preenchido")
        @DecimalMin(value = "0.01", message = "O preço deve ser maior que zero")
        BigDecimal preco,

        @Schema(
                description = "Quantidade da peca",
                example = "100"
        )
        @NotNull(message = "A quantidade em estoque deve ser preenchida")
        @Min(value = 0, message = "A quantidade em estoque não pode ser negativa")
        Integer quantidadeEstoque
) {
}
