package com.fiap.estoque.shared.seguranca.infra.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterUnitTest {

    private static final String ISSUER = "mecanica-fiap";
    private static final byte[] SEGREDO = Base64.getUrlDecoder().decode("fIACMIxZHpR5rhnyNTLfwJZOgDtZZVweeS1eRWuT6IZ_U5cRrinFFHJAeSHMpvPKHG_KLM-JTelvD5s3ulA");

    private final Algorithm algorithm = Algorithm.HMAC256(SEGREDO);
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(algorithm, ISSUER);
    private final FilterChain chain = mock(FilterChain.class);

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    private String token(Algorithm alg, String issuer, Instant expiracao) {
        return JWT.create()
                .withIssuer(issuer)
                .withSubject("7")
                .withArrayClaim("roles", new String[]{"ROLE_ADMINISTRADOR"})
                .withExpiresAt(expiracao)
                .sign(alg);
    }

    private MockHttpServletRequest requisicaoComToken(String token) {
        var request = new MockHttpServletRequest("GET", "/peca");
        request.addHeader("Authorization", "Bearer " + token);
        return request;
    }

    @Test
    void shouldAutenticarComRolesDoToken() throws Exception {
        var request = requisicaoComToken(token(algorithm, ISSUER, Instant.now().plus(1, ChronoUnit.HOURS)));
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals("7", auth.getName());
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR")));
        verify(chain).doFilter(request, response);
    }

    @Test
    void shouldSeguirAnonimoQuandoNaoHaToken() throws Exception {
        var request = new MockHttpServletRequest("GET", "/peca");
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(request, response);
    }

    @Test
    void shouldResponder401QuandoAssinaturaInvalida() throws Exception {
        var outroSegredo = Algorithm.HMAC256("outro-segredo");
        var request = requisicaoComToken(token(outroSegredo, ISSUER, Instant.now().plus(1, ChronoUnit.HOURS)));
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertEquals(401, response.getStatus());
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void shouldResponder401QuandoIssuerDiferente() throws Exception {
        var request = requisicaoComToken(token(algorithm, "outro-issuer", Instant.now().plus(1, ChronoUnit.HOURS)));
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertEquals(401, response.getStatus());
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void shouldResponder401QuandoTokenExpirado() throws Exception {
        var request = requisicaoComToken(token(algorithm, ISSUER, Instant.now().minus(1, ChronoUnit.HOURS)));
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertEquals(401, response.getStatus());
        verify(chain, never()).doFilter(any(), any());
    }
}
