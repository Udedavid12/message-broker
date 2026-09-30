package com.udedavid.message_broker.web;
import com.udedavid.message_broker.service.TopicService;
import com.udedavid.message_broker.service.TopicService.TopicAlreadyExistsException;
import com.udedavid.message_broker.web.dto.CreateTopicRequest;
import com.udedavid.message_broker.web.dto.TopicResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/api/v1/topics")
public class TopicController {
    private final TopicService topicService;
    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }
    @PostMapping
    public ResponseEntity<TopicResponse> createTopic(@Valid @RequestBody CreateTopicRequest request) {
        var topic = topicService.createTopic(request.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(TopicResponse.from(topic));
    }
    @GetMapping
    public List<TopicResponse> listTopics() {
        return topicService.listTopics().stream()
            .map(TopicResponse::from)
            .toList();
    }
    @ExceptionHandler(TopicAlreadyExistsException.class)
    public ResponseEntity<String> handleDuplicate(TopicAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
}