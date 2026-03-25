package com.grazielleanaia.scheduling_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class SchedulingApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(SchedulingApiApplication.class, args);
	}

}
