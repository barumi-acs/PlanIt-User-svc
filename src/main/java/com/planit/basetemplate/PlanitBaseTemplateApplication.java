package com.planit.basetemplate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaAuditing
@SpringBootApplication
@ComponentScan(basePackages = {"com.planit.basetemplate", "com.planit.userservice"})
@EntityScan(basePackages = {"com.planit.basetemplate", "com.planit.userservice"})
@EnableJpaRepositories(basePackages = {"com.planit.basetemplate", "com.planit.userservice"})
public class PlanitBaseTemplateApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlanitBaseTemplateApplication.class, args);
    }
}
