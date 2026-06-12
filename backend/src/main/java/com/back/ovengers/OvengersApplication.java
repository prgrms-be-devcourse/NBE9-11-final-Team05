package com.back.ovengers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class OvengersApplication {

    public static void main(String[] args) {
        SpringApplication.run(OvengersApplication.class, args);
    }

}
