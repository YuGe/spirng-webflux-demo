package me.yuge.springwebflux.core.configuration;

import me.yuge.springwebflux.core.configuration.security.AuthenticationConverter;
import me.yuge.springwebflux.core.configuration.security.AuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import reactor.core.publisher.Mono;

@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfiguration {

    private final AuthenticationConverter authenticationConverter;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    @Autowired
    public SecurityConfiguration(AuthenticationConverter authenticationConverter, AuthenticationEntryPoint authenticationEntryPoint) {
        this.authenticationConverter = authenticationConverter;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        // Disabled default security
        http.httpBasic().disable();
        http.formLogin().disable();
        http.logout().disable();
        http.csrf().disable();

        // Set custom exception handler
        http.exceptionHandling().authenticationEntryPoint(authenticationEntryPoint);

        // Add custom authentication converter
        AuthenticationWebFilter authenticationFilter = getAuthenticationWebFilter();
        authenticationFilter.setAuthenticationConverter(authenticationConverter);
        http.addFilterAt(authenticationFilter, SecurityWebFiltersOrder.CSRF);

        http.authorizeExchange().anyExchange().permitAll();

        return http.build();
    }

    private AuthenticationWebFilter getAuthenticationWebFilter() {
        return new AuthenticationWebFilter(new AuthenticationManager());
    }

    private class AuthenticationManager implements ReactiveAuthenticationManager {
        @Override
        public Mono<Authentication> authenticate(Authentication authentication) {
            return Mono.just(authentication);
        }
    }
}
