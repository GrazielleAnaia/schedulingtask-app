package com.grazielleanaia.scheduling_api.controller;


import com.grazielleanaia.scheduling_api.business.dto.CustomerResponseDTO;
import com.grazielleanaia.scheduling_api.infrastructure.client.CustomerClient;
import com.grazielleanaia.scheduling_api.infrastructure.client.HttpCustomerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

@RefreshScope
@Service
public class CustomerGateway {

    private final CustomerClient feignClient; //static
    private final HttpCustomerClient httpClient; //RestClient (dynamic baseUrl)
    private final Logger logger = LoggerFactory.getLogger(CustomerGateway.class);

    @Value("${client.type:feign}")
    private String clientType;

    public CustomerGateway(CustomerClient feignClient, HttpCustomerClient httpClient) {
        logger.info(">>> CustomerGateway CREATED <<<");
        this.feignClient = feignClient;
        this.httpClient = httpClient;
    }

    public CustomerResponseDTO findCustomerById(Long id) {
        if ("http".equalsIgnoreCase(clientType)) {
            logger.info("Client type is http: {}", httpClient.getClass().getName());
            return httpClient.findCustomerById(id);
        }
        logger.info("Client type is feign: {}", feignClient.getClass().getName());
        return feignClient.findCustomerById(id);
    }
}
