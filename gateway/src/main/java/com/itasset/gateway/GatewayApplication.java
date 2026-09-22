package com.itasset.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// The API gateway. Day 34: plain Spring Boot app — the eureka-client and config
// starters auto-register it and pull config, no annotations needed (like the other
// services). Routing (spring-cloud-starter-gateway + routes) is added on Day 36.
@SpringBootApplication
public class GatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}
}
