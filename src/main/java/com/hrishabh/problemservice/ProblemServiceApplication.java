package com.hrishabh.problemservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan({
        "com.hrishabh.problemservice.models",
        "com.hrishabh.problemservice.dailychallenge.model"
})
@EnableJpaRepositories({
        "com.hrishabh.problemservice.repository",
        "com.hrishabh.problemservice.dailychallenge.repository"
})
@EnableJpaAuditing
public class ProblemServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProblemServiceApplication.class, args);
    }

}
