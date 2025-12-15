package com.hrishabh.problemservice;


import com.intuit.karate.junit5.Karate;

class KarateRunner {

    @Karate.Test
    Karate runTests() {
        return Karate.run("classpath:karate/questions.feature").relativeTo(getClass());
    }
}

