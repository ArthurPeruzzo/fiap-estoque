package com.fiap.estoque.resources;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestComponent;

import java.time.Instant;
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
}
