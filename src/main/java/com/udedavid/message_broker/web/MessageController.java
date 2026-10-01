package com.udedavid.message_broker.web;

import com.udedavid.message_broker.service.MessageService;
import com.udedavid.message_broker.service.MessageService.InvalidStatusException;
import com.udedavid.message_broker.service.MessageService.TopicNotFoundException;
import com.udedavid.message_broker.web.dto.MessageResponse;
import com.udedavid.message_broker.web.dto.PublishMessageRequest;
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
@RequestMapping("/api/v1/topics/{topicName}/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    public ResponseEntity<MessageResponse> publish(
        @PathVariable String topicName,
        @Valid @RequestBody PublishMessageRequest request
    ) {
        var message = messageService.publish(topicName, request.payload());
        return ResponseEntity.status(HttpStatus.CREATED).body(MessageResponse.from(message));
    }

    @GetMapping
    public List<MessageResponse> list(
        @PathVariable String topicName,
        @RequestParam(required = false) String status
    ) {
        return messageService.listMessages(topicName, status).stream()
            .map(MessageResponse::from)
            .toList();
    }

    @ExceptionHandler(TopicNotFoundException.class)
    public ResponseEntity<String> handleTopicNotFound(TopicNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidStatusException.class)
    public ResponseEntity<String> handleInvalidStatus(InvalidStatusException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}