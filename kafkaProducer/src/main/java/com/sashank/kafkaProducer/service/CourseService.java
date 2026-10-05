package com.sashank.kafkaProducer.service;

import com.sashank.kafkaProducer.model.Course;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class CourseService {
    @Autowired
    private KafkaTemplate<String, Course> kafkaTemplate;
    public String sendMessage(Course course){
        kafkaTemplate.send("my-topic","course",course);
        return "Course Message Sent to Kafka Server";
    }
}
