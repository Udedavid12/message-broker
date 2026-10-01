package com.udedavid.message_broker.repository;

import com.udedavid.message_broker.domain.ConsumerGroup;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsumerGroupRepository extends JpaRepository<ConsumerGroup, UUID> {

    Optional<ConsumerGroup> findByTopicIdAndName(UUID topicId, String name);

    List<ConsumerGroup> findByTopicId(UUID topicId);

    boolean existsByTopicIdAndName(UUID topicId, String name);
}