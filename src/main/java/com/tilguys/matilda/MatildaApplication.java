package com.tilguys.matilda;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class MatildaApplication {

    public static void main(String[] args) {
        SpringApplication.run(MatildaApplication.class, args);
    }
}
