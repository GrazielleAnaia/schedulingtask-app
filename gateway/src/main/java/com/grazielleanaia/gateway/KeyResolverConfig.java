package com.grazielleanaia.gateway;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

@Configuration
public class KeyResolverConfig {

    //defines the driver limits by IP
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(
                exchange.getRequest()
                        .getRemoteAddress()
                        .getAddress()
                        .getHostAddress());
    }

    //limits by JWT authenticated user for Spring Cloud Gateway
    //app uses OAuth2 JWT resource server security
    @Primary
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> exchange.getPrincipal()
                .cast(org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken.class)
                .map(authentication -> authentication.getToken().getSubject())
                .switchIfEmpty(Mono.just("anonymous"));
    }
}
