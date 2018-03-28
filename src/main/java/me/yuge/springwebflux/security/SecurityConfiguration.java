package me.yuge.springwebflux.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import reactor.core.publisher.Mono;

@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfiguration {

    private final ServerAuthenticationConverter serverAuthenticationConverter;

    @Autowired
    public SecurityConfiguration(ServerAuthenticationConverter serverAuthenticationConverter) {
        this.serverAuthenticationConverter = serverAuthenticationConverter;
    }

    @Bean
    UserDetailsService userDetailsService() {
        return null;
    }

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity httpSecurity) {
        // Disabled default security
        httpSecurity.httpBasic().disable();
        httpSecurity.formLogin().disable();
        httpSecurity.logout().disable();
        httpSecurity.csrf().disable();

        // Add custom authentication converter
        AuthenticationWebFilter authenticationFilter = new AuthenticationWebFilter(new AuthenticationManager());
        authenticationFilter.setAuthenticationConverter(serverAuthenticationConverter);
        httpSecurity.addFilterAt(authenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION);

        // Disabled authentication for `POST /user` routes.
        httpSecurity.authorizeExchange().pathMatchers(HttpMethod.POST, "/user").permitAll();
        httpSecurity.authorizeExchange().anyExchange().authenticated();

        return httpSecurity.build();
    }

    public class AuthenticationManager implements ReactiveAuthenticationManager {
        @Override
        public Mono<Authentication> authenticate(Authentication authentication) {
            return Mono.just(authentication);
        }
    }
}
