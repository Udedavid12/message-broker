package com.udedavid.message_broker.service;

import com.udedavid.message_broker.domain.Delivery;
import com.udedavid.message_broker.domain.Message;
import com.udedavid.message_broker.domain.Topic;
import com.udedavid.message_broker.repository.DeliveryRepository;
import com.udedavid.message_broker.repository.MessageRepository;
import com.udedavid.message_broker.repository.TopicRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final TopicRepository topicRepository;
    private final DeliveryRepository deliveryRepository;
    private final ObjectMapper objectMapper;

    public MessageService(
        MessageRepository messageRepository,
        TopicRepository topicRepository,
        DeliveryRepository deliveryRepository,
        ObjectMapper objectMapper
    ) {
        this.messageRepository = messageRepository;
        this.topicRepository = topicRepository;
        this.deliveryRepository = deliveryRepository;
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

    @Transactional
    public Message ack(UUID deliveryToken) {
        Delivery delivery = deliveryRepository.findByDeliveryToken(deliveryToken)
            .orElseThrow(() -> new DeliveryNotFoundException(deliveryToken));

        Message message = delivery.getMessage();
        message.setStatus(Message.MessageStatus.ACKED);
        messageRepository.save(message);
        deliveryRepository.delete(delivery);

        return message;
    }

    @Transactional
    public Message nack(UUID deliveryToken) {
        Delivery delivery = deliveryRepository.findByDeliveryToken(deliveryToken)
            .orElseThrow(() -> new DeliveryNotFoundException(deliveryToken));

        Message message = delivery.getMessage();
        message.setStatus(Message.MessageStatus.PENDING);
        message.incrementRetryCount();
        messageRepository.save(message);
        deliveryRepository.delete(delivery);

        return message;
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

    public static class DeliveryNotFoundException extends RuntimeException {
        public DeliveryNotFoundException(UUID token) {
            super("Delivery not found: " + token);
        }
    }
}