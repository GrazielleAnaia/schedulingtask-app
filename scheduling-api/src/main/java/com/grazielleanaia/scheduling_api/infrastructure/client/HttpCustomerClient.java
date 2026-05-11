package com.grazielleanaia.scheduling_api.infrastructure.client;


import com.grazielleanaia.scheduling_api.business.dto.CustomerResponseDTO;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("/api/v1")
public interface HttpCustomerClient {

    @GetExchange("/{customerId}")
    CustomerResponseDTO findCustomerById(@PathVariable("customerId") Long customerId);

    @GetExchange("/customers/me")
    CustomerResponseDTO getMyProfile(@RequestHeader("X-User-Email") String email);
}
