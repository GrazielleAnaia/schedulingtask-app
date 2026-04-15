package com.grazielleanaia.scheduling_api.infrastructure.client;


import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class CustomerServiceClientConfig {

    //@Value("${customer.url}") String baseUrl

    @RefreshScope
    @Bean
    public HttpCustomerClient httpCustomerClientInterface(RestClient.Builder restClientBuilder) {
        RestClient restClient = restClientBuilder
                .baseUrl("http://REGISTRATION-API") //points to registration-api
                .defaultStatusHandler(HttpStatusCode::isError,
                        ((request, response) -> {
                            throw new RuntimeException("Error is " + response.getStatusCode());
                        }))
                .build();
        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        HttpCustomerClient httpCustomerClient = factory.createClient(HttpCustomerClient.class);
        return httpCustomerClient;
    }
}
