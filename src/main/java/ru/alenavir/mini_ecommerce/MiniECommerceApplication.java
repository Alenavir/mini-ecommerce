package ru.alenavir.mini_ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication
@EnableCaching
public class MiniECommerceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiniECommerceApplication.class, args);
    }

}
