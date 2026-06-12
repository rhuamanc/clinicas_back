package com.magm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FarmaciaApplication {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FarmaciaApplication.class);

    public static void main(String[] args) {
        log.info("Iniciando FarmaciaApplication");
        SpringApplication.run(FarmaciaApplication.class, args);
    }
}
