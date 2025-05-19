package com.notebridge.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SbNotebridgeApplication {

	public static void main(String[] args) {
		SpringApplication.run(SbNotebridgeApplication.class, args);
	}

}
