package com.fiap.estoque.infra.controller.json;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Item do catálogo (peça ou insumo) com o preço vigente")
public record ItemCatalogoResponseJson(
        @Schema(description = "ID do item", example = "1")
        Long id,
        @Schema(description = "Preço unitário", example = "89.90")
        BigDecimal preco
) {
}
