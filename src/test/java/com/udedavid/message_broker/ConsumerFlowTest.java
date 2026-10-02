package com.udedavid.message_broker;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class ConsumerFlowTest extends BaseApiTest {

    private String topic;
    private String group;

    private void setup() {
        topic = "flow-topic-" + System.nanoTime();
        group = "flow-group-" + System.nanoTime();
        rest.postForEntity("/api/v1/topics", Map.of("name", topic), Map.class);
        rest.postForEntity(
            "/api/v1/topics/" + topic + "/consumer-groups",
            Map.of("name", group),
            Map.class
        );
    }

    private Map publishMessage(String payloadValue) {
        return rest.postForEntity(
            "/api/v1/topics/" + topic + "/messages",
            Map.of("payload", Map.of("value", payloadValue)),
            Map.class
        ).getBody();
    }

    private Map[] fetchMessages() {
        return rest.getForEntity(
            "/api/v1/topics/" + topic + "/consumer-groups/" + group + "/messages?limit=10",
            Map[].class
        ).getBody();
    }

    @Test
    void fullLifecycle_ack() {
        setup();
        publishMessage("ack-test");

        Map[] deliveries = fetchMessages();
        assertThat(deliveries).hasSize(1);

        String token = (String) deliveries[0].get("deliveryToken");

        ResponseEntity<Map> ackResponse = rest.postForEntity(
            "/api/v1/deliveries/" + token + "/ack",
            null,
            Map.class
        );

        assertThat(ackResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(ackResponse.getBody().get("status")).isEqualTo("ACKED");
    }

    @Test
    void fullLifecycle_nack_thenRedeliver() {
        setup();
        publishMessage("nack-test");

        Map[] firstDelivery = fetchMessages();
        String token1 = (String) firstDelivery[0].get("deliveryToken");

        ResponseEntity<Map> nackResponse = rest.postForEntity(
            "/api/v1/deliveries/" + token1 + "/nack",
            null,
            Map.class
        );

        assertThat(nackResponse.getBody().get("status")).isEqualTo("PENDING");
        assertThat(nackResponse.getBody().get("retryCount")).isEqualTo(1);

        Map[] secondDelivery = fetchMessages();
        assertThat(secondDelivery).hasSize(1);
        assertThat(secondDelivery[0].get("deliveryToken")).isNotEqualTo(token1);
    }

    @Test
    void nackThrice_movesToDlq() {
        setup();
        publishMessage("dlq-test");

        for (int i = 1; i <= 3; i++) {
            Map[] deliveries = fetchMessages();
            assertThat(deliveries).as("delivery #" + i).hasSize(1);

            String token = (String) deliveries[0].get("deliveryToken");

            ResponseEntity<Map> response = rest.postForEntity(
                "/api/v1/deliveries/" + token + "/nack",
                null,
                Map.class
            );

            if (i < 3) {
                assertThat(response.getBody().get("status")).isEqualTo("PENDING");
                assertThat(response.getBody().get("retryCount")).isEqualTo(i);
            } else {
                assertThat(response.getBody().get("status")).isEqualTo("DLQ");
                assertThat(response.getBody().get("retryCount")).isEqualTo(3);
            }
        }

        Map[] afterDlq = fetchMessages();
        assertThat(afterDlq).isEmpty();
    }

    @Test
    void ackWithInvalidToken_returns404() {
        setup();

        ResponseEntity<String> response = rest.postForEntity(
            "/api/v1/deliveries/00000000-0000-0000-0000-000000000000/ack",
            null,
            String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}