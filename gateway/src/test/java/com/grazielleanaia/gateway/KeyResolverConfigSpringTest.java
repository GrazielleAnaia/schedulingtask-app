package com.grazielleanaia.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = KeyResolverConfig.class)

public class KeyResolverConfigSpringTest {

    @Autowired
    @Qualifier("userKeyResolver")
    private KeyResolver userKeyResolver;

    @Test
    void shouldLoadUserKeyResolverBean() {
        assertThat(userKeyResolver).isNotNull();
    }
}
