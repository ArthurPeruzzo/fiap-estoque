package com.fiap.estoque.shared.page;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    public static <T> PageResponse<T> from(Pagina<T> pagina) {
        return new PageResponse<>(
                pagina.content(),
                pagina.page(),
                pagina.size(),
                pagina.totalElements(),
                pagina.totalPages()
        );
    }
}

