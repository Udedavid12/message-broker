package com.udedavid.message_broker.service;

import com.udedavid.message_broker.domain.Delivery;
import com.udedavid.message_broker.domain.Message;
import com.udedavid.message_broker.repository.DeliveryRepository;
import com.udedavid.message_broker.repository.MessageRepository;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class VisibilityTimeoutJob {

    private static final Logger log = LoggerFactory.getLogger(VisibilityTimeoutJob.class);

    private final DeliveryRepository deliveryRepository;
    private final MessageRepository messageRepository;

    public VisibilityTimeoutJob(
        DeliveryRepository deliveryRepository,
        MessageRepository messageRepository
    ) {
        this.deliveryRepository = deliveryRepository;
        this.messageRepository = messageRepository;
    }

    @Scheduled(fixedDelay = 30_000)
    @Transactional
    public void reclaimExpiredDeliveries() {
        Instant now = Instant.now();
        List<Delivery> expired = deliveryRepository.findByExpiresAtBefore(now);

        if (expired.isEmpty()) {
            return;
        }

        log.info("Found {} expired deliveries to reclaim", expired.size());

        for (Delivery delivery : expired) {
            Message message = delivery.getMessage();
            message.incrementRetryCount();

            if (message.getRetryCount() >= message.getMaxRetries()) {
                message.setStatus(Message.MessageStatus.DLQ);
                log.info("Message {} exceeded retries, moved to DLQ", message.getId());
            } else {
                message.setStatus(Message.MessageStatus.PENDING);
                log.info("Message {} returned to PENDING (retry {})",
                    message.getId(), message.getRetryCount());
            }

            messageRepository.save(message);
            deliveryRepository.delete(delivery);
        }
    }
}