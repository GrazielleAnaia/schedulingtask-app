package com.grazielleanaia.gateway;

//Verifies scheduling-api and registration-api routes load from yml file

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false"

})

public class GatewayRouteConfigTest {

    @Autowired
    private RouteDefinitionLocator routeDefinitionLocator;

    @Test
    void shouldLoadSchedulingAndRegistrationRoutes() {
        List<RouteDefinition> routes = routeDefinitionLocator.getRouteDefinitions()
                .collectList()
                .block();
        assertThat(routes).isNotNull();
        assertThat(routes).extracting(RouteDefinition::getId)
                .contains("scheduling-api", "registration-api");
    }

    @Test
    void schedulingApiRouteShouldHaveExpectedUriPredicatesAndFilters() {
        RouteDefinition schedulingRoute = findRoute("scheduling-api");
        assertThat(schedulingRoute.getUri().toString()).isEqualTo("lb://SCHEDULING-API");
        assertThat(schedulingRoute.getPredicates())
                .anySatisfy(predicate -> {
                    assertThat(predicate.getName()).isEqualTo("Path");
                    assertThat(predicate.getArgs().values())
                            .anyMatch(value -> value.contains("/api/v1/customers/me/tasks"));
                });

        assertThat(schedulingRoute.getFilters()).extracting(filter -> filter.getName())
                .contains("RequestRateLimiter", "CircuitBreaker");
    }

    @Test
    void registrationApiRouteShouldHaveExpectedUriPredicatesAndFilters() {
        RouteDefinition registrationRoute = findRoute("registration-api");
        assertThat(registrationRoute.getUri().toString()).isEqualTo("lb://REGISTRATION-API");
        assertThat(registrationRoute.getPredicates())
                .anySatisfy(predicate -> {
                    assertThat(predicate.getName()).isEqualTo("Path");
                    assertThat(predicate.getArgs().values()).anyMatch(value -> value.contains("/api/v1/customers"));
                });
        assertThat(registrationRoute.getFilters())
                .extracting(filter -> filter.getName()).contains("CircuitBreaker");
    }

    private RouteDefinition findRoute(String routeId) {
        List<RouteDefinition> routes = routeDefinitionLocator.getRouteDefinitions()
                .collectList()
                .block();
        return routes.stream()
                .filter(route -> route.getId().equals(routeId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No route found with id: " + routeId));
    }

}

/*

anySatisfy:
- for objects where you want multiple assertions inside

anyMatch:
- for values where a true/false predicate is enough

extracting:
- transforms objects into one property before asserting
*/