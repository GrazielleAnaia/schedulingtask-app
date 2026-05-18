package com.grazielleanaia.scheduling_api.controller;


import com.grazielleanaia.scheduling_api.business.dto.CustomerResponseDTO;
import com.grazielleanaia.scheduling_api.infrastructure.client.CustomerClient;
import com.grazielleanaia.scheduling_api.infrastructure.client.HttpCustomerClient;
import com.grazielleanaia.scheduling_api.infrastructure.exception.CustomerServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

@RefreshScope
@Service
public class CustomerGateway {

    private final CustomerClient feignClient;
    private final HttpCustomerClient httpClient;
    private final Logger logger = LoggerFactory.getLogger(CustomerGateway.class);

    @Value("${client.type:feign}")
    private String clientType;

    public CustomerGateway(CustomerClient feignClient, HttpCustomerClient httpClient) {
        logger.info(">>> CustomerGateway CREATED <<<");
        this.feignClient = feignClient;
        this.httpClient = httpClient;
    }

//    public CustomerResponseDTO findCustomerById(Long id) {
//        if ("http".equalsIgnoreCase(clientType)) {
//            logger.info("Client type is http: {}", httpClient.getClass().getName());
//            return httpClient.findCustomerById(id);
//        }
//        logger.info("Client type is feign: {}", feignClient.getClass().getName());
//        return feignClient.findCustomerById(id);
//    }

    // This protects scheduling-api -> registration-api
    @CircuitBreaker(name = "customerService", fallbackMethod = "findCustomerByEmailFallback")
    public CustomerResponseDTO findCustomerByEmail(String email) {
        if ("http".equalsIgnoreCase(clientType)) {
            logger.info("Client type is http: {}", httpClient.getClass().getName());
            return httpClient.getMyProfile(email);
        }
        logger.info("Client type is feign: {}", feignClient.getClass().getName());
        return feignClient.getMyProfile(email);
    }

    private CustomerResponseDTO findCustomerByEmailFallback(String email, Throwable ex) {
        logger.warn("Circuit breaker fallback triggered for email: {}", email, ex);
        throw new CustomerServiceUnavailableException("Customer service is unavailable. " +
                "Could not verify customer email: " + email, ex);
    }
}
