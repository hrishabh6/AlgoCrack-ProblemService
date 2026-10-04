package com.hrishabh.problemservice;

import com.hrishabh.problemservice.complexity.config.ComplexityProfileProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableConfigurationProperties(ComplexityProfileProperties.class)
@EntityScan({
        "com.hrishabh.problemservice.models",
        "com.hrishabh.problemservice.dailychallenge.model",
        "com.hrishabh.problemservice.complexity.model"
})
@EnableJpaRepositories({
        "com.hrishabh.problemservice.repository",
        "com.hrishabh.problemservice.dailychallenge.repository",
        "com.hrishabh.problemservice.complexity.repository"
})
@EnableJpaAuditing
public class ProblemServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProblemServiceApplication.class, args);
    }

}
