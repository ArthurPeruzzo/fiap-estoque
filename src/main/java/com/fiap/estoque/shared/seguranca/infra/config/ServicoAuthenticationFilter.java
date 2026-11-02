package com.fiap.estoque.shared.seguranca.infra.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

/**
 * Autenticação serviço a serviço das rotas internas ({@link RotasInternas}). O token de serviço é assinado
 * com um segredo próprio (diferente do JWT de usuário), precisa ter o issuer e a audiência deste serviço e
 * carrega as permissões na claim "scope" (separadas por espaço), que viram autoridades "SCOPE_xxx".
 * Sem token, a requisição segue anônima e as regras do SecurityConfiguration respondem 401.
 */
@Component
@Slf4j
@Profile({"!controller-test"})
public class ServicoAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER = "Bearer ";
    private static final String SCOPE = "scope";

    private final JWTVerifier verifier;

    public ServicoAuthenticationFilter(@Value("${servico.jwt.secret}") String secret,
                                       @Value("${servico.jwt.issuer}") String issuer,
                                       @Value("${servico.jwt.audience}") String audience,
                                       @Value("${servico.jwt.leeway:10s}") Duration leeway) {
        Algorithm algorithm = Algorithm.HMAC256(Base64.getUrlDecoder().decode(secret));
        this.verifier = JWT.require(algorithm)
                .withIssuer(issuer)
                .withAudience(audience)
                .acceptLeeway(leeway.toSeconds())
                .build();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !RotasInternas.correspondem(request);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");

        if (authorization != null && authorization.startsWith(BEARER)) {
            try {
                DecodedJWT jwt = verifier.verify(authorization.substring(BEARER.length()));
                List<GrantedAuthority> authorities = escopos(jwt).stream()
                        .map(escopo -> new SimpleGrantedAuthority("SCOPE_" + escopo))
                        .map(GrantedAuthority.class::cast)
                        .toList();
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(jwt.getSubject(), null, authorities));
            } catch (JWTVerificationException e) {
                log.warn("Falha ao verificar token de serviço: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Token inválido ou expirado\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private List<String> escopos(DecodedJWT jwt) {
        String escopos = jwt.getClaim(SCOPE).asString();
        if (escopos == null || escopos.isBlank()) {
            return List.of();
        }
        return Arrays.asList(escopos.trim().split("\\s+"));
    }
}
