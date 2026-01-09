package com.superdupermart.shopping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class OnlineShoppingApplication {

	public static void main(String[] args) {
		SpringApplication.run(OnlineShoppingApplication.class, args);
	}

}
