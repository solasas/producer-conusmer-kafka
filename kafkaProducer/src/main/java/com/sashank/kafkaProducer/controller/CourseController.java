package com.sashank.kafkaProducer.controller;

import com.sashank.kafkaProducer.model.Course;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.sashank.kafkaProducer.service.CourseService;

@RestController
@RequestMapping("/kafka")
public class CourseController {
    @Autowired
    private CourseService service;
    @PostMapping("/add-course")
    public ResponseEntity<String> addCourse(@RequestBody Course course){
        String response=service.sendMessage(course);
        return new ResponseEntity<String>(response,HttpStatus.OK);
    }
}
