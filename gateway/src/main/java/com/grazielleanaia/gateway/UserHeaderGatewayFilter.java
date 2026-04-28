package com.grazielleanaia.gateway;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@Component

public class UserHeaderGatewayFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return exchange.getPrincipal()
                .cast(Authentication.class)
                .flatMap(authentication -> {
                    Object principal = authentication.getPrincipal();
                    if (principal instanceof Jwt jwt) {
                        String email = jwt.getClaimAsString("email");
                        String username = jwt.getClaimAsString("preferred_username");

                        ServerHttpRequest.Builder requestBuilder = exchange.getRequest().mutate();

                        if (email != null) {
                            requestBuilder.header("X-User-Email", email);
                        }
                        if (username != null) {
                            requestBuilder.header("X-Username", username);
                        }

                        return chain.filter(exchange.mutate().request(requestBuilder.build()).build());
                    }
                    return chain.filter(exchange);
                })
                .switchIfEmpty(chain.filter(exchange));
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
