package com.udedavid.message_broker.service;

import com.udedavid.message_broker.domain.ConsumerGroup;
import com.udedavid.message_broker.domain.Delivery;
import com.udedavid.message_broker.domain.Message;
import com.udedavid.message_broker.domain.Topic;
import com.udedavid.message_broker.repository.ConsumerGroupRepository;
import com.udedavid.message_broker.repository.DeliveryRepository;
import com.udedavid.message_broker.repository.MessageRepository;
import com.udedavid.message_broker.repository.TopicRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsumerGroupService {

    public static final Duration VISIBILITY_TIMEOUT = Duration.ofMinutes(1);

    private final ConsumerGroupRepository consumerGroupRepository;
    private final TopicRepository topicRepository;
    private final MessageRepository messageRepository;
    private final DeliveryRepository deliveryRepository;

    public ConsumerGroupService(
        ConsumerGroupRepository consumerGroupRepository,
        TopicRepository topicRepository,
        MessageRepository messageRepository,
        DeliveryRepository deliveryRepository
    ) {
        this.consumerGroupRepository = consumerGroupRepository;
        this.topicRepository = topicRepository;
        this.messageRepository = messageRepository;
        this.deliveryRepository = deliveryRepository;
    }

    @Transactional
    public ConsumerGroup register(String topicName, String groupName) {
        Topic topic = topicRepository.findByName(topicName)
            .orElseThrow(() -> new TopicNotFoundException(topicName));

        if (consumerGroupRepository.existsByTopicIdAndName(topic.getId(), groupName)) {
            throw new ConsumerGroupAlreadyExistsException(topicName, groupName);
        }

        return consumerGroupRepository.save(new ConsumerGroup(topic, groupName));
    }

    public List<ConsumerGroup> listGroups(String topicName) {
        Topic topic = topicRepository.findByName(topicName)
            .orElseThrow(() -> new TopicNotFoundException(topicName));
        return consumerGroupRepository.findByTopicId(topic.getId());
    }

    @Transactional
    public List<Delivery> fetchMessages(String topicName, String groupName, int limit) {
        Topic topic = topicRepository.findByName(topicName)
            .orElseThrow(() -> new TopicNotFoundException(topicName));

        ConsumerGroup group = consumerGroupRepository.findByTopicIdAndName(topic.getId(), groupName)
            .orElseThrow(() -> new ConsumerGroupNotFoundException(topicName, groupName));

        List<Message> pending = messageRepository
            .findByTopicIdAndStatusOrderByCreatedAtAsc(topic.getId(), Message.MessageStatus.PENDING);

        if (pending.size() > limit) {
            pending = pending.subList(0, limit);
        }

        Instant expiresAt = Instant.now().plus(VISIBILITY_TIMEOUT);
        List<Delivery> created = new ArrayList<>();
        for (Message message : pending) {
            message.setStatus(Message.MessageStatus.IN_FLIGHT);
            messageRepository.save(message);
            Delivery delivery = new Delivery(message, group, expiresAt);
            created.add(deliveryRepository.save(delivery));
        }

        return created;
    }

    public static class TopicNotFoundException extends RuntimeException {
        public TopicNotFoundException(String name) {
            super("Topic not found: " + name);
        }
    }

    public static class ConsumerGroupNotFoundException extends RuntimeException {
        public ConsumerGroupNotFoundException(String topic, String group) {
            super("Consumer group not found: " + group + " (topic: " + topic + ")");
        }
    }

    public static class ConsumerGroupAlreadyExistsException extends RuntimeException {
        public ConsumerGroupAlreadyExistsException(String topic, String group) {
            super("Consumer group already exists: " + group + " (topic: " + topic + ")");
        }
    }
}