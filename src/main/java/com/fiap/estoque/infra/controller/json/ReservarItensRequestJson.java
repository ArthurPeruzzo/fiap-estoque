package com.fiap.estoque.infra.controller.json;

import com.fiap.estoque.core.domain.TipoItemReserva;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ReservarItensRequestJson(
        @NotEmpty(message = "Os itens devem ser informados")
        List<@Valid @NotNull(message = "O item não pode ser nulo") ItemReservaRequestJson> itens
) {

    public record ItemReservaRequestJson(
            @NotNull(message = "O tipo do item deve ser preenchido")
            TipoItemReserva tipoItem,

            @NotNull(message = "O id do item deve ser preenchido")
            Long itemId,

            @NotNull(message = "A quantidade total deve ser preenchida")
            @Min(value = 0, message = "A quantidade total não pode ser negativa")
            Integer quantidadeTotal
    ) {
    }
}
