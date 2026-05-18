package com.grazielleanaia.gateway;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Mono;

public class KeyResolverConfig {


    @Bean
    public KeyResolver keyResolver() {
        return exchange -> Mono.just(
                exchange.getRequest()
                .getRemoteAddress()
                .getAddress()
                .getHostAddress());
    }
}
