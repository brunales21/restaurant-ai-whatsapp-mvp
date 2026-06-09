package com.restaurant.mcpclient.service;

import com.restaurant.mcpclient.dto.McpToolInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class McpToolCatalogService {

    private final ToolCallbackProvider mcpToolCallbackProvider;

    public List<McpToolInfo> listTools() {
        return Arrays.stream(mcpToolCallbackProvider.getToolCallbacks())
                .map(tool -> new McpToolInfo(
                        tool.getToolDefinition().name(),
                        tool.getToolDefinition().description()))
                .toList();
    }
}
