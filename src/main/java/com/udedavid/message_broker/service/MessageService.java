package com.udedavid.message_broker.service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import com.udedavid.message_broker.domain.Message;
import com.udedavid.message_broker.domain.Topic;
import com.udedavid.message_broker.repository.MessageRepository;
import com.udedavid.message_broker.repository.TopicRepository;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class MessageService {
    private final MessageRepository messageRepository;
    private final TopicRepository topicRepository;
    private final ObjectMapper objectMapper;
    public MessageService(
        MessageRepository messageRepository,
        TopicRepository topicRepository,
        ObjectMapper objectMapper
    ) {
        this.messageRepository = messageRepository;
        this.topicRepository = topicRepository;
        this.objectMapper = objectMapper;
    }
    @Transactional
    public Message publish(String topicName, Map<String, Object> payload) {
        Topic topic = topicRepository.findByName(topicName)
            .orElseThrow(() -> new TopicNotFoundException(topicName));
        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(payload);
        } catch (JacksonException e) {
            throw new InvalidPayloadException("Could not serialize payload");
        }
        Message message = new Message(topic, payloadJson);
        return messageRepository.save(message);
    }
    public List<Message> listMessages(String topicName) {
        Topic topic = topicRepository.findByName(topicName)
            .orElseThrow(() -> new TopicNotFoundException(topicName));
        return messageRepository.findByTopicIdOrderByCreatedAtAsc(topic.getId());
    }
    public static class TopicNotFoundException extends RuntimeException {
        public TopicNotFoundException(String name) {
            super("Topic not found: " + name);
        }
    }
    public static class InvalidPayloadException extends RuntimeException {
        public InvalidPayloadException(String message) {
            super(message);
        }
    }
}