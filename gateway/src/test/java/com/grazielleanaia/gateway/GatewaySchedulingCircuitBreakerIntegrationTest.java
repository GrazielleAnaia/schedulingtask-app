package com.grazielleanaia.gateway;


import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "eureka.client.enabled=false",
                "spring.cloud.discovery.enabled=false",
                "spring.cloud.config.enabled=false",
                "resilience4j.circuitbreaker.instances.schedulingApiCircuitBreaker.slidingWindowSize=2",
                "resilience4j.circuitbreaker.instances.schedulingApiCircuitBreaker.minimumNumberOfCalls=1",
                "resilience4j.circuitbreaker.instances.schedulingApiCircuitBreaker.failureRateThreshold=50",
                "resilience4j.circuitbreaker.instances.schedulingApiCircuitBreaker.waitDurationInOpenState=5s",
                "resilience4j.circuitbreaker.instances.schedulingApiCircuitBreaker.permittedNumberOfCallsInHalfOpenState=1",
                "resilience4j.circuitbreaker.instances.schedulingApiCircuitBreaker.automaticTransitionFromOpenToHalfOpenEnabled=true",
                "resilience4j.circuitbreaker.instances.schedulingApiCircuitBreaker.slidingWindowType=COUNT_BASED"
        })

public class GatewaySchedulingCircuitBreakerIntegrationTest {

    @LocalServerPort
    private int port;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUpWebTestClient() {
        this.webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    void shouldReturnSchedulingFallbackWhenSchedulingApiFails() {
        String token = "test-user-" + UUID.randomUUID();
        webTestClient.get()
                .uri("/api/v1/customers/me/tasks")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Scheduling service is temporarily unavailable");
    }

    @TestConfiguration
    static class TestConfig {

        @Primary
        @Bean
        ReactiveJwtDecoder reactiveJwtDecoder() {
            return token -> {
                Jwt jwt = Jwt.withTokenValue(token)
                        .header("alg", "none")
                        .claim("sub", token)
                        .build();
                return Mono.just(jwt);
            };
        }

        @Bean
        RouteLocator circuitBreakerRouterTest(RouteLocatorBuilder builder) {
            return builder.routes()
                    .route("test-scheduling-api-circuit-breaker",
                            route -> route.path("/api/v1/customers/me/tasks")
                                    .filters(filters -> filters.circuitBreaker(config -> {
                                        config.setName("schedulingApiCircuitBreaker");
                                        config.setFallbackUri("forward:/fallback/scheduling-api");
                                    }))
                                    .uri("forward:/broken-scheduling-api"))
                    .build();
        }

        @Bean
        RouterFunction<ServerResponse> brokenSchedulingApiRoute() {
            return RouterFunctions.route(
                    RequestPredicates.GET("/broken-scheduling-api"),
                    request ->
                            ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(Map.of("message", "Scheduling API is temporarily unavailable"))
            );
        }
    }
}
