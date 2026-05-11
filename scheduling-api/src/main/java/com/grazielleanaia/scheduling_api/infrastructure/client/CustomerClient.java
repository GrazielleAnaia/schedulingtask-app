package com.grazielleanaia.scheduling_api.infrastructure.client;


import com.grazielleanaia.scheduling_api.business.dto.CustomerResponseDTO;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

//url = "${customer.url}"

@RefreshScope
@FeignClient(name = "REGISTRATION-API", path = "/api/v1")
public interface CustomerClient {

    @GetMapping("/{customerId}")
    CustomerResponseDTO findCustomerById(@PathVariable("customerId") Long customerId);

    @GetMapping("/customers/me")
    CustomerResponseDTO getMyProfile(@RequestHeader("X-User-Email") String email);
}
