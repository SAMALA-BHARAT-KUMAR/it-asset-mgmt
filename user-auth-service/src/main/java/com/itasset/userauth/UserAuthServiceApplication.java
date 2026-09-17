package com.itasset.userauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Day 31: user-auth-service — users/roles/JWT, own DB userdb, registers in Eureka.
@SpringBootApplication
public class UserAuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserAuthServiceApplication.class, args);
    }
}
