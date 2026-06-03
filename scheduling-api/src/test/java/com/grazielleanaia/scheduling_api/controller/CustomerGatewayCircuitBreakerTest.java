package com.grazielleanaia.scheduling_api.controller;

import com.grazielleanaia.scheduling_api.infrastructure.client.CustomerClient;
import com.grazielleanaia.scheduling_api.infrastructure.client.HttpCustomerClient;
import com.grazielleanaia.scheduling_api.infrastructure.exception.CustomerServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {
        "client.type=http",
        "MONGO_URI=mongodb://localhost:27017/scheduling_test",
        "MONGO_DATABASE=scheduling_test",
        "resilience4j.circuitbreaker.instances.customerService.registerHealthIndicator=true",
        "resilience4j.circuitbreaker.instances.customerService.slidingWindowSize=10",
        "resilience4j.circuitbreaker.instances.customerService.minimumNumberOfCalls=5",
        "resilience4j.circuitbreaker.instances.customerService.failureRateThreshold=50",
        "resilience4j.circuitbreaker.instances.customerService.waitDurationInOpenState=10s",
        "resilience4j.circuitbreaker.instances.customerService.permittedNumberOfCallsInHalfOpenState=3",
        "resilience4j.circuitbreaker.instances.customerService.automaticTransitionFromOpenToHalfOpenEnabled=true",
        "resilience4j.circuitbreaker.instances.customerService.slidingWindowType=COUNT_BASED"
})

@ActiveProfiles("test")

public class CustomerGatewayCircuitBreakerTest {
    @Autowired
    private CustomerGateway customerGateway;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @MockitoBean
    private CustomerClient feignClient;

    @MockitoBean
    private HttpCustomerClient httpClient;

    @BeforeEach
    void resetCircuitBreaker() {
        reset(httpClient, feignClient);
        circuitBreakerRegistry.circuitBreaker("customerService").reset();
    }

    @Test
    void shouldOpenCircuitBreakerWhenCustomerServiceFails() {
        when(httpClient.getMyProfile("test@email.com"))
                .thenThrow(new RuntimeException("registration-api is down"));

        for (int i = 0; i < 5; i++) {
            assertThrows(CustomerServiceUnavailableException.class, () ->
                    customerGateway.findCustomerByEmail("test@email.com"));
        }

        CircuitBreaker circuitBreaker =
                circuitBreakerRegistry.circuitBreaker("customerService");

        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        verify(httpClient, times(5)).getMyProfile("test@email.com");
    }

    @Test
    void shouldFailFastWhenCircuitIsOpen() {
        when(httpClient.getMyProfile("test@email.com"))
                .thenThrow(new RuntimeException("registration-api is down"));

        for (int i = 0; i < 5; i++) {
            assertThrows(CustomerServiceUnavailableException.class, () ->
                    customerGateway.findCustomerByEmail("test@email.com")
            );
        }

        CircuitBreaker circuitBreaker =
                circuitBreakerRegistry.circuitBreaker("customerService");

        assertThat(circuitBreaker.getState())
                .isEqualTo(CircuitBreaker.State.OPEN);

        assertThrows(CustomerServiceUnavailableException.class, () ->
                customerGateway.findCustomerByEmail("test@email.com"));

        verify(httpClient, times(5)).getMyProfile("test@email.com");
    }
}
