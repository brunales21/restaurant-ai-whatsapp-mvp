package com.restaurant.mcpclient.dto;

import jakarta.validation.constraints.NotBlank;

public record McpChatRequest(
        @NotBlank String message,
        String phone
) {
}
