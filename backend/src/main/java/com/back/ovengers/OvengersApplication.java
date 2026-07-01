package com.back.ovengers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@ConfigurationPropertiesScan
@EnableScheduling
@EnableAsync
public class OvengersApplication {

    public static void main(String[] args) {
        SpringApplication.run(OvengersApplication.class, args);
    }

}
