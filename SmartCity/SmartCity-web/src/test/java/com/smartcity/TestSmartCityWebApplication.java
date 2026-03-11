package com.smartcity;

import org.springframework.boot.SpringApplication;

public class TestSmartCityWebApplication {

	public static void main(String[] args) {
		SpringApplication.from(SmartCityWebApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
