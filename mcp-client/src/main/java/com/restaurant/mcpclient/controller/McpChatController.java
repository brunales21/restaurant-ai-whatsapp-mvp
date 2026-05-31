package com.restaurant.mcpclient.controller;

import com.restaurant.mcpclient.dto.McpChatRequest;
import com.restaurant.mcpclient.dto.McpChatResponse;
import com.restaurant.mcpclient.dto.McpToolInfo;
import com.restaurant.mcpclient.service.McpChatService;
import com.restaurant.mcpclient.service.McpToolCatalogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/mcp-chat")
@RequiredArgsConstructor
public class McpChatController {

    private final McpChatService mcpChatService;
    private final McpToolCatalogService mcpToolCatalogService;

    @PostMapping
    public McpChatResponse chat(@Valid @RequestBody McpChatRequest request) {
        return new McpChatResponse(mcpChatService.chat(request.message()));
    }

    @GetMapping("/tools")
    public List<McpToolInfo> tools() {
        return mcpToolCatalogService.listTools();
    }
}
