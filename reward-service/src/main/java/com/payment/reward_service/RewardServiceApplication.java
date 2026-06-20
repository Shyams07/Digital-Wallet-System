package com.payment.reward_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Collections;

@SpringBootApplication
public class RewardServiceApplication {
	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(RewardServiceApplication.class);
		app.setDefaultProperties(Collections.singletonMap("server.port", "8085"));
		app.run(args);
	}
}
