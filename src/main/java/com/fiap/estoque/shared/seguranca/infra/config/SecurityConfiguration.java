package com.fiap.estoque.shared.seguranca.infra.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.beans.factory.ObjectProvider;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final ObjectProvider<JwtAuthenticationFilter> jwtAuthenticationFilter;

    protected static final String ADMINISTRADOR = "ADMINISTRADOR";

    static final String[] ENDPOINTS_SEM_AUTENTICACAO = {
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/favicon.ico",
            "/actuator/health/**"
    };

    private static final String[] ENDPOINTS_ADMINISTRADOR = {
            "/peca/**",
            "/insumo/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(ENDPOINTS_SEM_AUTENTICACAO).permitAll()
                        .requestMatchers(ENDPOINTS_ADMINISTRADOR).hasRole(ADMINISTRADOR)
                        .anyRequest().denyAll()
                )
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\":\"Não autenticado\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\":\"Acesso negado\"}");
                        })
                );
        jwtAuthenticationFilter.ifAvailable(filter ->
                httpSecurity.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class));
        return httpSecurity.build();
    }
}
