package com.udedavid.message_broker.repository;
import com.udedavid.message_broker.domain.Message;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
public interface MessageRepository extends JpaRepository<Message, UUID> {
    List<Message> findByTopicIdOrderByCreatedAtAsc(UUID topicId);
    long countByTopicId(UUID topicId);
}