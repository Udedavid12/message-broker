package com.udedavid.message_broker.service;
import com.udedavid.message_broker.domain.Topic;
import com.udedavid.message_broker.repository.TopicRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class TopicService {
    private final TopicRepository topicRepository;
    public TopicService(TopicRepository topicRepository) {
        this.topicRepository = topicRepository;
    }
    @Transactional
    public Topic createTopic(String name) {
        if (topicRepository.existsByName(name)) {
            throw new TopicAlreadyExistsException(name);
        }
        return topicRepository.save(new Topic(name));
    }
    public List<Topic> listTopics() {
        return topicRepository.findAll();
    }
    public static class TopicAlreadyExistsException extends RuntimeException {
        public TopicAlreadyExistsException(String name) {
            super("Topic already exists: " + name);
        }
    }
}