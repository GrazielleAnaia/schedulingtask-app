package com.grazielleanaia.gateway;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class GatewayFallbackController {

    @GetMapping("/fallback/scheduling-api")
    public ResponseEntity<Map<String, String>> schedulingApiFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("message", "Scheduling service is temporarily unavailable"));
    }

    @GetMapping("/fallback/registration-api")
    public ResponseEntity<Map<String, String>> registrationApiFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("message", "Registration service is temporarily unavailable"));
    }
}
