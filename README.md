# producer-consumer-kafka

A small Kafka learning project with two Spring Boot apps. The producer takes a `Course` over REST and publishes it to a Kafka topic. The consumer listens on that topic and exposes the last message it received.

## Flow

```
POST /kafka/add-course -> kafkaProducer (8081) -> topic my-topic (localhost:9092) -> KafkaConsumer (8080)
GET  /kafka/get-course <- KafkaConsumer (8080) returns the last consumed message
```

## Projects

| Module | Port | Role |
|---|---|---|
| `kafkaProducer` | 8081 | REST endpoint that sends a `Course` to `my-topic` as JSON (key `course`) |
| `KafkaConsumer` | 8080 | `@KafkaListener` on `my-topic` (group `my-topic-group`) that keeps the last message in memory |

Both use Java 21, Spring Boot 4.1 and Spring Kafka 4, with the Jackson 3 JSON (de)serializers.

## Prerequisites
- Java 21
- Docker, for a local Kafka broker

## Run it

1. Start a broker with port 9092 published to the host:
   ```bash
   docker run -d --name kafka -p 9092:9092 apache/kafka:latest
   ```
2. Start both apps, each in its own terminal:
   ```bash
   cd kafkaProducer && ./mvnw spring-boot:run
   cd KafkaConsumer && ./mvnw spring-boot:run
   ```
3. Send a course and read it back:
   ```bash
   curl -X POST localhost:8081/kafka/add-course -H 'Content-Type: application/json' \
     -d '{"courseId":"java1","title":"java SB","trainer":"navin","price":2550}'
   curl localhost:8080/kafka/get-course
   ```

The topic is auto-created on first use. See `kafkaProducer/roadmap.md` to create it with several partitions.

## Notes
`kafkaProducer/roadmap.md` has the lessons learned (package scanning, Jackson 3 serializers, Docker port publishing, consumer type headers), Kafka CLI commands, a debugging checklist and next steps.
