package com.grazielleanaia.scheduling_api.health;


import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;


@Component
public class CustomHealthIndicator implements HealthIndicator {

    private HealthEnum healthEnum = HealthEnum.UP;

    @Override
    public Health health() {
        return switch (healthEnum) {
            case UP -> Health.up().build();
            case DOWN -> Health.down().build();
            case OUT_OF_SERVICE -> Health.outOfService().build();
        };
    }

    public void setHealthEnum(HealthEnum healthEnum) {
        this.healthEnum = healthEnum;
    }
}

