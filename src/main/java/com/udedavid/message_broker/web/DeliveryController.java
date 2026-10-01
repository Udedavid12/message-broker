package com.udedavid.message_broker.web;

import com.udedavid.message_broker.service.MessageService;
import com.udedavid.message_broker.service.MessageService.DeliveryNotFoundException;
import com.udedavid.message_broker.web.dto.MessageResponse;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/deliveries")
public class DeliveryController {

    private final MessageService messageService;

    public DeliveryController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping("/{deliveryToken}/ack")
    public ResponseEntity<MessageResponse> ack(@PathVariable UUID deliveryToken) {
        var message = messageService.ack(deliveryToken);
        return ResponseEntity.ok(MessageResponse.from(message));
    }

    @PostMapping("/{deliveryToken}/nack")
    public ResponseEntity<MessageResponse> nack(@PathVariable UUID deliveryToken) {
        var message = messageService.nack(deliveryToken);
        return ResponseEntity.ok(MessageResponse.from(message));
    }

    @ExceptionHandler(DeliveryNotFoundException.class)
    public ResponseEntity<String> handleDeliveryNotFound(DeliveryNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}