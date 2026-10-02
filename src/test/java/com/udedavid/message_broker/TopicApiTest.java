package com.udedavid.message_broker;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class TopicApiTest extends BaseApiTest {

    @Test
    void createTopic_returnsCreated() {
        ResponseEntity<Map> response = rest.postForEntity(
            "/api/v1/topics",
            Map.of("name", "test-topic-" + System.nanoTime()),
            Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).containsKey("id");
        assertThat(response.getBody()).containsKey("createdAt");
    }

    @Test
    void createTopic_duplicate_returnsConflict() {
        String name = "dupe-topic-" + System.nanoTime();

        rest.postForEntity("/api/v1/topics", Map.of("name", name), Map.class);

        ResponseEntity<String> response = rest.postForEntity(
            "/api/v1/topics",
            Map.of("name", name),
            String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void createTopic_missingName_returns400() {
        ResponseEntity<String> response = rest.postForEntity(
            "/api/v1/topics",
            Map.of(),
            String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void listTopics_returnsArray() {
        rest.postForEntity(
            "/api/v1/topics",
            Map.of("name", "list-topic-" + System.nanoTime()),
            Map.class
        );

        ResponseEntity<Object[]> response = rest.getForEntity(
            "/api/v1/topics",
            Object[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
    }
}