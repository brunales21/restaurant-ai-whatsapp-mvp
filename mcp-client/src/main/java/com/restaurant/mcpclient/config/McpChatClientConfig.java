package com.restaurant.mcpclient.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpChatClientConfig {

    @Bean
    ChatClient chatClient(ChatClient.Builder builder, ToolCallbackProvider mcpToolCallbackProvider) {
        return builder
                .defaultSystem("Eres un cliente MCP para un restaurante. Usa las tools MCP remotas cuando el usuario pregunte por menús o reservas.")
                .defaultToolCallbacks(mcpToolCallbackProvider)
                .build();
    }
}
