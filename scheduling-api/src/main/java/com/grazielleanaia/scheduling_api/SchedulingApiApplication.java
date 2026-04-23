package com.grazielleanaia.scheduling_api;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.util.Arrays;

@SpringBootApplication
@EnableFeignClients
public class SchedulingApiApplication {

    @Autowired
    Environment env;

    public static void main(String[] args) {
        SpringApplication.run(SchedulingApiApplication.class, args);
    }

    @PostConstruct
    public void checkConfig() {
        System.out.println("eureka.instance.hostname = " + env.getProperty("eureka.instance.hostname"));
        System.out.println("eureka.instance.ip-address = " + env.getProperty("eureka.instance.ip-address"));
        System.out.println("spring.application.name = " + env.getProperty("spring.application.name"));
    }

    @PostConstruct
    public void debug() throws Exception {
        System.out.println("ENV HOSTNAME = " + System.getenv("HOSTNAME"));
        System.out.println("InetAddress.getLocalHost() = " + InetAddress.getLocalHost().getHostName());
        System.out.println("InetAddress.getLocalHost().getHostAddress() = " + InetAddress.getLocalHost().getHostAddress());
        System.out.println("ACTIVE PROFILES: " + Arrays.toString(env.getActiveProfiles()));
        System.out.println("Eureka enabled = " + env.getProperty("eureka.client.enabled"));

    }
}
