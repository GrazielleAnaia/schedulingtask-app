package com.grazielleanaia.scheduling_api.infrastructure.client;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
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

    private final Logger logger = LoggerFactory.getLogger(CustomerServiceClientConfig.class);

    @RefreshScope
    @Bean
    public HttpCustomerClient httpCustomerClientInterface(@Qualifier("restClientBuilderLb") RestClient.Builder restClientBuilder) {
        RestClient restClient = restClientBuilder
                .baseUrl("http://REGISTRATION-API") //points to registration-api
                .defaultStatusHandler(HttpStatusCode::isError,
                        ((request, response) -> {
                            logger.error("Error status is: " + response.getStatusCode());
                            throw new RuntimeException("Error is " + response.getStatusCode());
                        }))
                .build();
        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        HttpCustomerClient httpCustomerClient = factory.createClient(HttpCustomerClient.class);
        return httpCustomerClient;
    }
}
