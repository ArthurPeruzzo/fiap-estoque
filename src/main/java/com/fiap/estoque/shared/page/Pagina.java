package com.fiap.estoque.shared.page;

import java.util.List;
import java.util.function.Function;

public record Pagina<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public <R> Pagina<R> map(Function<T, R> mapper) {
        return new Pagina<>(content.stream().map(mapper).toList(), page, size, totalElements, totalPages);
    }
}
