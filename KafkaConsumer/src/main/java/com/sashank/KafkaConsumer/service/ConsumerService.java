package com.sashank.KafkaConsumer.service;

import com.sashank.KafkaConsumer.model.Course;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ConsumerService {
    private String message;
    @KafkaListener(topics="my-topic",groupId = "my-topic-group")
    public String getMessage(Course course){
        message=course+" got the data from kafka topic";
        return "Course Message Sent to Kafka Server";
    }

    public String getMessage() {
        return message;
    }
}
