# Kafka + Spring Boot Roadmap

Notes from building `kafkaProducer` (port 8081) and `KafkaConsumer` (port 8080) with Spring Boot 4 and Spring Kafka 4.

## Flow

```
curl POST /kafka/add-course -> Producer (8081) -> Kafka topic my-topic (localhost:9092) -> Consumer (8080) @KafkaListener
curl GET  /kafka/get-course <- Consumer (8080) returns the last message it received
```

## Key learnings

### 1. Package layout decides what Spring finds
- `@SpringBootApplication` only scans its own package and sub-packages.
- `controller`, `service` and `model` must live under the main class's package (for example `com.sashank.kafkaProducer.controller`).
- Symptom when wrong: `404 NoResourceFoundException: No static resource kafka/add-course`. No handler matched, so Spring tried static files.

### 2. Serializer must match the value type
- `KafkaTemplate<String, Course>` needs a JSON serializer. `StringSerializer` fails on a `Course` object.
- Spring Boot 4 uses Jackson 3 (`tools.jackson`). The old `JsonSerializer` and `JsonDeserializer` need Jackson 2 and throw `NoClassDefFoundError: com/fasterxml/jackson/databind/ObjectMapper`.
- Use the Jackson 3 classes:
  - Producer: `org.springframework.kafka.support.serializer.JacksonJsonSerializer`
  - Consumer: `org.springframework.kafka.support.serializer.JacksonJsonDeserializer`

### 3. Docker: publish the broker port
- `docker ps` showing `9092/tcp` means the port is exposed but not published, so the Mac can't reach it.
- It must show `0.0.0.0:9092->9092/tcp`. Run with `-p 9092:9092`.
- Symptom when wrong: `TimeoutException: Topic my-topic not present in metadata after 60000 ms`.
- Removing a container without a volume deletes its topics and groups.

### 4. Consumer config
- The producer's type header names the producer's class (`com.sashank.kafkaProducer.model.Course`), which does not exist in the consumer.
  - Set `spring.json.use.type.headers=false`.
  - Set `spring.json.value.default.type` to the consumer's own `Course`.
  - Set `spring.json.trusted.packages` to the consumer's model package.
- `auto-offset-reset=earliest` only applies to a group with no committed offset. A group that has already committed an offset resumes from there.
- A new `groupId` re-reads the topic from the start.

### 5. Other gotchas
- Don't put `@RequestBody` on a GET handler.
- Data kept in a field (`private String message`) is lost on restart or devtools reload. Use a list or a database for anything you need to keep.
- Without `toString()`, printing an object gives `Course@23b8bd99`.
- Topics are auto-created with 1 partition unless you create them yourself.
- Consumer groups appear only after a consumer first joins.

## Tutorial commands

### Docker
```bash
docker run -d --name kafka -p 9092:9092 apache/kafka:latest   # start broker, port published
docker ps --format '{{.Names}} {{.Ports}}'                    # check 0.0.0.0:9092->9092
docker rm -f kafka                                            # remove it (topics are lost)
```

### Topics (prefix with `docker exec kafka`)
```bash
/opt/kafka/bin/kafka-topics.sh --create --topic my-topic --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
/opt/kafka/bin/kafka-topics.sh --list --bootstrap-server localhost:9092
/opt/kafka/bin/kafka-topics.sh --describe --topic my-topic --bootstrap-server localhost:9092
/opt/kafka/bin/kafka-topics.sh --alter --topic my-topic --partitions 3 --bootstrap-server localhost:9092   # can only increase
```

### Console producer and consumer
```bash
/opt/kafka/bin/kafka-console-producer.sh --topic my-topic --bootstrap-server localhost:9092
/opt/kafka/bin/kafka-console-consumer.sh --topic my-topic --bootstrap-server localhost:9092 --from-beginning
/opt/kafka/bin/kafka-console-consumer.sh --topic my-topic --bootstrap-server localhost:9092 --group my-group
```

### Consumer groups
```bash
/opt/kafka/bin/kafka-consumer-groups.sh --list --bootstrap-server localhost:9092
/opt/kafka/bin/kafka-consumer-groups.sh --describe --group my-topic-group --bootstrap-server localhost:9092   # CURRENT-OFFSET, LOG-END-OFFSET, LAG
```

### Test the apps
```bash
curl -X POST localhost:8081/kafka/add-course -H 'Content-Type: application/json' \
  -d '{"courseId":"java1","title":"java SB","trainer":"navin","price":2550}'
curl localhost:8080/kafka/get-course
```

## Debugging checklist
1. Does the endpoint return 404? Check package scanning (learning 1).
2. Is the producer stuck for 60 seconds? Check the Docker port and `bootstrap-servers` (learning 3).
3. Does the producer fail on construction? Check the Jackson 3 serializer class (learning 2).
4. Is the consumer silent? Run `kafka-consumer-groups.sh --describe`. If LAG is 0 and offsets moved, the message was consumed, so check the consumer's logs and in-memory state (learnings 4 and 5).
5. Compare messages with the console consumer using `--from-beginning`.

## Next steps
- Store consumed messages in a list or a database.
- Create the topic with 3 partitions and use the course ID as the message key.
- Run two consumers in one group and watch partitions split between them.
- Add a `DefaultErrorHandler` with a dead-letter topic.
- Add `docker-compose.yml` with a volume for Kafka data.
