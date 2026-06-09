package com.restaurant.mcpclient.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class McpConnectionStartupLogger {

    @Value("${spring.ai.mcp.client.streamable-http.connections.restaurant-server.url}")
    private String mcpServerUrl;

    @Value("${spring.ai.mcp.client.streamable-http.connections.restaurant-server.endpoint:/mcp}")
    private String mcpServerEndpoint;

    @PostConstruct
    void logMcpConnection() {
        log.info("MCP client configurado para conectar con {}{}", mcpServerUrl, mcpServerEndpoint);

        if (mcpServerUrl.contains("ngrok")) {
            log.warn("RESTAURANT_MCP_SERVER_URL apunta a ngrok ({}). " +
                    "Esto suele ser incorrecto cuando mcp-client y mcp-server corren juntos en Docker Compose. " +
                    "Usa http://mcp-server:8080 dentro de Compose; reserva ngrok para exponer /webhooks/twilio del mcp-client.",
                    mcpServerUrl);
        }

        if (mcpServerUrl.endsWith(mcpServerEndpoint)) {
            log.warn("La URL base del MCP server ya termina con el endpoint {}. " +
                    "Spring AI concatena url + endpoint, así que podrías estar llamando a {}{}.",
                    mcpServerEndpoint, mcpServerUrl, mcpServerEndpoint);
        }
    }
}
