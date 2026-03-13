package com.smartCity.Web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;
@Component("com.*")
@SpringBootApplication
public class MySmartCityApplication {

	public static void main(String[] args) {
		SpringApplication.run(MySmartCityApplication.class, args);
		System.out.println("Hi Subhash Im Started With you");
	}

}
