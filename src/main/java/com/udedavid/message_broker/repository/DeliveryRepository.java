package com.udedavid.message_broker.repository;

import com.udedavid.message_broker.domain.Delivery;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {

    Optional<Delivery> findByDeliveryToken(UUID deliveryToken);

    void deleteByMessageId(UUID messageId);
}