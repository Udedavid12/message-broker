package com.udedavid.message_broker.web;

import com.udedavid.message_broker.service.ConsumerGroupService;
import com.udedavid.message_broker.service.ConsumerGroupService.ConsumerGroupAlreadyExistsException;
import com.udedavid.message_broker.service.ConsumerGroupService.ConsumerGroupNotFoundException;
import com.udedavid.message_broker.service.ConsumerGroupService.TopicNotFoundException;
import com.udedavid.message_broker.web.dto.ConsumerGroupResponse;
import com.udedavid.message_broker.web.dto.CreateConsumerGroupRequest;
import com.udedavid.message_broker.web.dto.DeliveryResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/topics/{topicName}/consumer-groups")
public class ConsumerGroupController {

    private final ConsumerGroupService consumerGroupService;

    public ConsumerGroupController(ConsumerGroupService consumerGroupService) {
        this.consumerGroupService = consumerGroupService;
    }

    @PostMapping
    public ResponseEntity<ConsumerGroupResponse> register(
        @PathVariable String topicName,
        @Valid @RequestBody CreateConsumerGroupRequest request
    ) {
        var group = consumerGroupService.register(topicName, request.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(ConsumerGroupResponse.from(group));
    }

    @GetMapping
    public List<ConsumerGroupResponse> list(@PathVariable String topicName) {
        return consumerGroupService.listGroups(topicName).stream()
            .map(ConsumerGroupResponse::from)
            .toList();
    }

    @GetMapping("/{groupName}/messages")
    public List<DeliveryResponse> fetch(
        @PathVariable String topicName,
        @PathVariable String groupName,
        @RequestParam(defaultValue = "10") int limit
    ) {
        return consumerGroupService.fetchMessages(topicName, groupName, limit).stream()
            .map(DeliveryResponse::from)
            .toList();
    }

    @ExceptionHandler(TopicNotFoundException.class)
    public ResponseEntity<String> handleTopicNotFound(TopicNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(ConsumerGroupNotFoundException.class)
    public ResponseEntity<String> handleGroupNotFound(ConsumerGroupNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(ConsumerGroupAlreadyExistsException.class)
    public ResponseEntity<String> handleGroupExists(ConsumerGroupAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
}