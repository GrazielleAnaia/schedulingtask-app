package com.grazielleanaia.gateway;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

//Real Redis container and fake downstream service
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "eureka.client.enabled=false",
                "spring.cloud.discovery.enabled=false",
                "spring.cloud.config.enabled=false",
        })


@Testcontainers
public class GatewayRateLimiterIntegrationTest {

    @ServiceConnection
    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379)
            .waitingFor(Wait.forListeningPort());

    private WebTestClient webTestClient;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUpWebTestClient() {
        this.webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .responseTimeout(Duration.ofSeconds(15))
                .build();
    }

    static MockWebServer downstream = new MockWebServer();

    static {
        try {
            downstream.setDispatcher(new Dispatcher() {
                @Override
                public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                    System.out.println("DOWNSTREAM RECEIVED: " + request.getPath());
                    return new MockResponse()
                            .setResponseCode(200)
                            .setHeader("Content-Type", "text/plain")
                            .setHeader("Content-Length", "13")
                            .setHeader("Connection", "close")
                            .setBody("downstream-ok");
                }
            });

            downstream.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterAll
    static void stopDownstream() throws IOException {
        downstream.shutdown();
    }

    @Test
    void shouldReturn429WhenJwtUserExceedsRateLimit() {
        String token = "user-rate-limit-test-" + UUID.randomUUID();

        webTestClient.get()
                .uri("/api/v1/customers/me/tasks")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("downstream-ok");

        webTestClient.get()
                .uri("/api/v1/customers/me/tasks")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }

    @Test
    void shouldRateLimitDifferentJwtUsersSeparately() {

        String userA = "user-separate-a-" + UUID.randomUUID();
        String userB = "user-separate-b-" + UUID.randomUUID();

        webTestClient.get()
                .uri("/api/v1/customers/me/tasks")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userA)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("downstream-ok");

        webTestClient.get()
                .uri("/api/v1/customers/me/tasks")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userA)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS);

        webTestClient.get()
                .uri("/api/v1/customers/me/tasks")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userB)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("downstream-ok");
    }

    @TestConfiguration
    static class JwtTestConfig {
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
        RouterFunction<ServerResponse> testDownstreamRoute() {
            return RouterFunctions.route(
                    RequestPredicates.GET("/test-downstream"),
                    request -> ServerResponse.ok()
                            .contentType(MediaType.TEXT_PLAIN)
                            .bodyValue("downstream-ok"));
        }

        @Bean
        RedisRateLimiter redisRateLimiter() {
            RedisRateLimiter limiter = new RedisRateLimiter(1, 1, 1);
            limiter.setIncludeHeaders(false);
            return limiter;
        }

        @Bean
        RouteLocator testRoutes(
                RouteLocatorBuilder builder,
                RedisRateLimiter redisRateLimiter,
                @Qualifier("userKeyResolver")
                KeyResolver userKeyResolver) {
            return builder.routes()
                    .route("test-scheduling-api", route -> route
                            .path("/api/v1/customers/me/tasks")
                            .filters(filters -> filters.requestRateLimiter(config -> {
                                config.setRateLimiter(redisRateLimiter);
                                config.setKeyResolver(userKeyResolver);
                            }))
                            .uri("forward:/test-downstream"))
                    .build();
        }
    }
}
