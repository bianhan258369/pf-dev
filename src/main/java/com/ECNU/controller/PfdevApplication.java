package com.ECNU.controller;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.ECNU.service","com.ECNU.controller"})
public class PfdevApplication {

	public static void main(String[] args) {
		SpringApplication.run(PfdevApplication.class, args);
	}

}
