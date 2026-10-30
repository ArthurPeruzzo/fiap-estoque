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
    private final ObjectProvider<ServicoAuthenticationFilter> servicoAuthenticationFilter;

    protected static final String ADMINISTRADOR = "ADMINISTRADOR";

    // Permissões do token de serviço (claim "scope"); o filtro as expõe com o prefixo SCOPE_.
    protected static final String ESCOPO_RESERVA = "SCOPE_reserva:escrita";
    protected static final String ESCOPO_CATALOGO = "SCOPE_catalogo:leitura";

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
                        // Internas: token de serviço emitido pelo fiap-mecanica, com o escopo da rota.
                        .requestMatchers("/reserva/**").hasAuthority(ESCOPO_RESERVA)
                        .requestMatchers("/catalogo/**").hasAuthority(ESCOPO_CATALOGO)
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
        servicoAuthenticationFilter.ifAvailable(filter ->
                httpSecurity.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class));
        return httpSecurity.build();
    }
}
