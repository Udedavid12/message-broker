package com.udedavid.message_broker.web.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record CreateTopicRequest(
    @NotBlank @Size(min = 1, max = 255) String name
) {}