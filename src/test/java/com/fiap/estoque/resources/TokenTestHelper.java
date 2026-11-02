package com.fiap.estoque.resources;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestComponent;

import java.time.Instant;
import java.util.Base64;
import java.time.temporal.ChronoUnit;

/**
 * Gera JWTs válidos para os testes de integração, no mesmo formato emitido pela API principal
 * (subject = id do usuário, claim "roles" com valores como ROLE_ADMINISTRADOR).
 */
@TestComponent
public class TokenTestHelper {

    @Autowired
    private Algorithm algorithm;

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${servico.jwt.secret}")
    private String servicoSecret;

    @Value("${servico.jwt.issuer}")
    private String servicoIssuer;

    @Value("${servico.jwt.audience}")
    private String servicoAudience;

    public String gerar(String... roles) {
        return JWT.create()
                .withIssuer(issuer)
                .withSubject("1")
                .withArrayClaim("roles", roles)
                .withExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .sign(algorithm);
    }

    public String administrador() {
        return gerar("ROLE_ADMINISTRADOR");
    }

    /** Token de serviço válido, no formato emitido pelo fiap-mecanica (claim "scope" separada por espaço). */
    public String servico(String... escopos) {
        return servico(servicoAudience, Instant.now().plus(90, ChronoUnit.SECONDS), servicoSecret, escopos);
    }

    public String servico(String audiencia, Instant expiraEm, String segredoBase64Url, String... escopos) {
        return JWT.create()
                .withIssuer(servicoIssuer)
                .withSubject("svc:fiap-mecanica")
                .withAudience(audiencia)
                .withClaim("scope", String.join(" ", escopos))
                .withIssuedAt(Instant.now())
                .withExpiresAt(expiraEm)
                .sign(Algorithm.HMAC256(Base64.getUrlDecoder().decode(segredoBase64Url)));
    }

    public String servicoReserva() {
        return servico("reserva:escrita");
    }

    public String servicoCatalogo() {
        return servico("catalogo:leitura");
    }

    public String segredoDeServico() {
        return servicoSecret;
    }

    public String audienciaDeServico() {
        return servicoAudience;
    }
}
