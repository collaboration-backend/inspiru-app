package com.stc.inspireu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableCaching
@EnableJpaAuditing
public class InspireuAppServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(InspireuAppServerApplication.class, args);
	}

}
