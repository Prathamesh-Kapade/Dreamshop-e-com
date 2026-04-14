package com.demo.dreamshops;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class DreamShopsApplication {

	public static void main(String[] args) {
        SpringApplication.run(DreamShopsApplication.class, args);
	}
}


