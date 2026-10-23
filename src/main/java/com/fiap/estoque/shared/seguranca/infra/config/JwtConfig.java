package com.fiap.estoque.shared.seguranca.infra.config;

import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Base64;

/**
 * Mesmo segredo e issuer da API principal (fiap-mecanica): o token emitido lá é validado aqui,
 * sem consultar usuário em banco.
 */
@Configuration
public class JwtConfig {

    @Value("${jwt.secret}")
    private String secretKey;

    @Bean
    public Algorithm jwtAlgorithm() {
        return Algorithm.HMAC256(Base64.getUrlDecoder().decode(secretKey));
    }
}
