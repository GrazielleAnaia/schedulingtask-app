package com.grazielleanaia.scheduling_api.health;


import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")


public class HealthController {

    private final CustomHealthIndicator indicator;

    public HealthController(CustomHealthIndicator indicator) {
        this.indicator = indicator;
    }

    //@Profile("dev")
    @PostMapping("/down")
    public void down() {
        indicator.setHealthEnum(HealthEnum.DOWN);
    }

    @PostMapping("/up")
    public void up() {
        indicator.setHealthEnum(HealthEnum.UP);
    }

    //@Profile("dev")
    @PostMapping("/out-of-service")
    public void outOfService() {
        indicator.setHealthEnum(HealthEnum.OUT_OF_SERVICE);
    }
}
