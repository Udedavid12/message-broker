package com.udedavid.message_broker;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class MessageApiTest extends BaseApiTest {

    private String createTopic() {
        String name = "msg-topic-" + System.nanoTime();
        rest.postForEntity("/api/v1/topics", Map.of("name", name), Map.class);
        return name;
    }

    @Test
    void publishMessage_returnsCreated() {
        String topic = createTopic();

        ResponseEntity<Map> response = rest.postForEntity(
            "/api/v1/topics/" + topic + "/messages",
            Map.of("payload", Map.of("orderId", "abc123")),
            Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().get("status")).isEqualTo("PENDING");
        assertThat(response.getBody().get("retryCount")).isEqualTo(0);
    }

    @Test
    void publishMessage_unknownTopic_returns404() {
        ResponseEntity<String> response = rest.postForEntity(
            "/api/v1/topics/does-not-exist/messages",
            Map.of("payload", Map.of("test", "value")),
            String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void listMessages_filtersByStatus() {
        String topic = createTopic();
        rest.postForEntity(
            "/api/v1/topics/" + topic + "/messages",
            Map.of("payload", Map.of("a", "1")),
            Map.class
        );

        ResponseEntity<Object[]> pending = rest.getForEntity(
            "/api/v1/topics/" + topic + "/messages?status=PENDING",
            Object[].class
        );

        ResponseEntity<Object[]> dlq = rest.getForEntity(
            "/api/v1/topics/" + topic + "/messages?status=DLQ",
            Object[].class
        );

        assertThat(pending.getBody()).hasSize(1);
        assertThat(dlq.getBody()).isEmpty();
    }

    @Test
    void listMessages_invalidStatus_returns400() {
        String topic = createTopic();

        ResponseEntity<String> response = rest.getForEntity(
            "/api/v1/topics/" + topic + "/messages?status=BOGUS",
            String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}