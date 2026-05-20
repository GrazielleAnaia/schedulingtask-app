package com.grazielleanaia.gateway;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ServerWebExchange;
import reactor.test.StepVerifier;

import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.get;


@ExtendWith(MockitoExtension.class)

public class KeyResolverConfigTest {

    private final KeyResolverConfig config = new KeyResolverConfig();

    @Test
    void userKeyResolverShouldReturnJwtSubject() {
        KeyResolver resolver = config.userKeyResolver();

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "user-123")
                .build();
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt);

        MockServerHttpRequest request =
                MockServerHttpRequest.get("/api/v1/customers/me/tasks")
                        .build();
        MockServerWebExchange exchange =
                MockServerWebExchange.builder(request)
                        .principal(authentication)
                        .build();
        StepVerifier.create(resolver.resolve(exchange))
                .expectNext("user-123")
                .verifyComplete();
    }

    @Test
    void userKeyResolverShouldReturnAnonymousWhenNoPrincipalExists() {
        KeyResolver resolver = config.userKeyResolver();
        ServerWebExchange exchange = MockServerWebExchange.from(
                get("/api/v1/customers/me/tasks")
                        .build());
        StepVerifier.create(resolver.resolve(exchange))
                .expectNext("anonymous")
                .verifyComplete();
    }
}
