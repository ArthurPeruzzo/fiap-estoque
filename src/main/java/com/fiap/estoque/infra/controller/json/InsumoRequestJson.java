package com.fiap.estoque.infra.controller.json;

import com.fiap.estoque.core.domain.UnidadeMedida;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(
        name = "InsumoRequestJson",
        description = "Dados necessários para criar um insumo"
)
public record InsumoRequestJson(
        @Schema(
                description = "Nome do insumo",
                example = "Óleo de motor"
        )
        @NotBlank(message = "O nome deve ser preenchido")
        String nome,

        @Schema(
                description = "Descrição do insumo",
                example = "Óleo lubrificante utilizado para reduzir o atrito entre componentes internos do motor"
        )
        @NotBlank(message = "A descrição deve ser preenchida")
        String descricao,

        @Schema(
                description = "Preço do insumo",
                example = "100.00"
        )
        @NotNull(message = "O preço deve ser preenchido")
        @DecimalMin(value = "0.01", message = "O preço deve ser maior que zero")
        BigDecimal preco,

        @Schema(
                description = "Quantidade do insumo",
                example = "100"
        )
        @NotNull(message = "A quantidade em estoque deve ser preenchida")
        @Min(value = 0, message = "A quantidade em estoque não pode ser negativa")
        Integer quantidadeEstoque,

         @Schema(
                 description = "Unidade de medida",
                 example = "UNIDADE"
         )
        @NotNull(message = "A unidade de medida deve ser preenchida")
        UnidadeMedida unidadeMedida
) {
}
