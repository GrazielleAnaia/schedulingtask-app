package com.grazielleanaia.scheduling_api.infrastructure.client;


import com.grazielleanaia.scheduling_api.business.dto.CustomerDTO;
import com.grazielleanaia.scheduling_api.business.dto.CustomerResponseDTO;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;


@HttpExchange
public interface HttpCustomerClient {

    @GetExchange("/by-email")
    CustomerDTO findCustomerByEmail(@RequestParam("email") String email);

    @GetExchange("/{id}")
    CustomerResponseDTO findCustomerById(@PathVariable("id") Long customerId);
}
