package com.fiap.estoque.shared.seguranca.infra.config;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Rotas chamadas só por outros serviços (hoje, o fiap-mecanica). Elas se autenticam com token de serviço
 * ({@link ServicoAuthenticationFilter}); as demais usam o JWT de usuário ({@link JwtAuthenticationFilter}).
 */
final class RotasInternas {

    private static final String[] PREFIXOS = {"/reserva/", "/catalogo/"};

    private RotasInternas() {
    }

    static boolean correspondem(HttpServletRequest request) {
        String caminho = request.getServletPath();
        for (String prefixo : PREFIXOS) {
            if (caminho.startsWith(prefixo)) {
                return true;
            }
        }
        return false;
    }
}
