package com.sashank.KafkaConsumer.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.sashank.KafkaConsumer.service.ConsumerService;

@RestController
@RequestMapping("/kafka")
public class ConsumerController {
    @Autowired
    private ConsumerService service;

    @GetMapping("/get-course")
    public ResponseEntity<String> getCourse(){
        String response=service.getMessage();
        return new ResponseEntity<String>(response,HttpStatus.OK);
    }
}
