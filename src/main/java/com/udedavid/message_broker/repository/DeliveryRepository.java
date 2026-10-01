package com.udedavid.message_broker.repository;

import com.udedavid.message_broker.domain.Delivery;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {

    Optional<Delivery> findByDeliveryToken(UUID deliveryToken);

    List<Delivery> findByExpiresAtBefore(Instant now);

    void deleteByMessageId(UUID messageId);
}