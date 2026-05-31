package com.restaurant.mcpserver.config;

import com.restaurant.mcpserver.ai.tools.RestaurantTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpToolConfig {

    @Bean
    ToolCallbackProvider restaurantToolCallbackProvider(RestaurantTools restaurantTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(restaurantTools)
                .build();
    }
}
