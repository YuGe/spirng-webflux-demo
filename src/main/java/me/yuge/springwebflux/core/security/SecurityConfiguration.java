package me.yuge.springwebflux.core.security;

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

    @Autowired
    public SecurityConfiguration(AuthenticationConverter authenticationConverter) {
        this.authenticationConverter = authenticationConverter;
    }

//    @Bean
//    public Boolean oauth2StatelessSecurityContext() {
//        return Boolean.FALSE;
//    }

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        // Disabled default security
        http.httpBasic().disable();
        http.formLogin().disable();
        http.logout().disable();
        http.csrf().disable();
        http.exceptionHandling();

        // Set custom exception handler
        http.exceptionHandling().authenticationEntryPoint(new AuthenticationEntryPoint());

        // Add custom authentication converter
        AuthenticationWebFilter authenticationFilter = new AuthenticationWebFilter(new AuthenticationManager());
        authenticationFilter.setAuthenticationConverter(authenticationConverter);
        http.addFilterAt(authenticationFilter, SecurityWebFiltersOrder.CSRF);

        http.authorizeExchange().anyExchange().permitAll();

        return http.build();
    }

    private class AuthenticationManager implements ReactiveAuthenticationManager {
        @Override
        public Mono<Authentication> authenticate(Authentication authentication) {
            return Mono.just(authentication);
        }
    }
}
