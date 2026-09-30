package com.udedavid.message_broker.repository;
import com.udedavid.message_broker.domain.Topic;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
public interface TopicRepository extends JpaRepository<Topic, UUID> {
    Optional<Topic> findByName(String name);
    boolean existsByName(String name);
}