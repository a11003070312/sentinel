package com.pineguard.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class AmqpConfig {

    @Bean
    public TopicExchange alarmExchange() {
        return new TopicExchange("sentinel.alarm", true, false);
    }

    @Bean
    public Queue alarmQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-max-priority", 10);
        return new Queue("sentinel.alarm.triggered", true, false, false, args);
    }

    @Bean
    public Binding alarmBinding(Queue alarmQueue, TopicExchange alarmExchange) {
        return BindingBuilder.bind(alarmQueue).to(alarmExchange).with("alarm.triggered");
    }
}
