package com.hrishabh.problemservice.dailychallenge.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class PotdClockConfig {

    @Bean
    public Clock potdClock(PotdSchedulerProperties properties) {
        return Clock.system(java.time.ZoneId.of(properties.getZoneId()));
    }
}
