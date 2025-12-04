package com.nevzatcirak.sharedsignals.boot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main application entry point.
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.nevzatcirak.sharedsignals")
@EnableJpaRepositories(basePackages = "com.nevzatcirak.sharedsignals.persistence.repository")
@EntityScan(basePackages = "com.nevzatcirak.sharedsignals.persistence.entity")
public class SharedSignalsApplication {
    public static void main(String[] args) {
        SpringApplication.run(SharedSignalsApplication.class, args);
    }
}
