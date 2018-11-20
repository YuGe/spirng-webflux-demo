package me.yuge.springwebflux.core.configuration.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfiguration {
    private final HttpBasicBearerAuthenticationEntryPoint authenticationEntryPoint;
    private final SecurityContextRepository securityContextRepository;

    @Autowired
    public SecurityConfiguration(HttpBasicBearerAuthenticationEntryPoint authenticationEntryPoint, SecurityContextRepository securityContextRepository) {
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.securityContextRepository = securityContextRepository;
    }

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        // Disable default spec
        http.httpBasic().disable();
        http.formLogin().disable();
        http.logout().disable();
        http.csrf().disable();

        http.exceptionHandling().authenticationEntryPoint(authenticationEntryPoint);
        http.securityContextRepository(securityContextRepository);

        http.authorizeExchange().anyExchange().permitAll();

        return http.build();
    }
}
