package com.udedavid.message_broker.web.dto;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
public record PublishMessageRequest(
    @NotNull Map<String, Object> payload
) {}