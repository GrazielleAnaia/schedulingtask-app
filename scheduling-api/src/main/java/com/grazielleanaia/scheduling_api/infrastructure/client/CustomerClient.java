package com.grazielleanaia.scheduling_api.infrastructure.client;


import com.grazielleanaia.scheduling_api.business.dto.CustomerDTO;
import com.grazielleanaia.scheduling_api.business.dto.CustomerResponseDTO;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

//url = "${customer.url}"

@RefreshScope
@FeignClient(name = "REGISTRATION-API", path = "/api/customers")
public interface CustomerClient {

    @GetMapping("/by-email")
    CustomerDTO findCustomerByEmail(@RequestParam("email") String email);

    @GetMapping("/{id}")
    CustomerResponseDTO findCustomerById(@PathVariable("id") Long customerId);
}
