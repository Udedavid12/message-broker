package com.udedavid.message_broker;

import org.springframework.boot.SpringApplication;

public class TestMessageBrokerApplication {

    public static void main(String[] args) {
        SpringApplication
            .from(MessageBrokerApplication::main)
            .with(TestcontainersConfiguration.class)
            .run(args);
    }
}