# restaurant-mcp-client

Proyecto Spring Boot separado que actúa como **MCP client** y se conecta a un servidor MCP de restaurante.

## Recomendación de estructura

Para este MVP conviene mantenerlo en el mismo repositorio como carpeta separada (`mcp-client/`) porque:

- servidor y cliente evolucionan juntos durante la demo;
- es fácil probar cambios de contrato MCP en una sola PR;
- si el cliente crece o se despliega por separado, luego se puede extraer a otro repositorio sin mezclar paquetes Java ni builds.

## Qué hace

- Arranca en `8081`.
- Se conecta a un MCP server remoto configurado por variables de entorno.
- Registra las tools descubiertas del MCP server como tools disponibles para `ChatClient`.
- Expone un endpoint local para chatear usando esas tools.
- Expone un endpoint para listar tools descubiertas.

## Variables

- `OPENAI_API_KEY`: API key de OpenAI.
- `OPENAI_MODEL`: modelo de chat, por defecto `gpt-4o-mini`.
- `RESTAURANT_MCP_SERVER_URL`: URL base del MCP server, por defecto `http://localhost:8080`.
- `RESTAURANT_MCP_SERVER_ENDPOINT`: endpoint MCP, por defecto `/mcp`.
- `MCP_CLIENT_PORT`: puerto HTTP del cliente, por defecto `8081`.

## Ejecutar

```bash
mvn spring-boot:run
```


## Docker Compose opcional

Desde la raíz del repositorio puedes levantar el cliente con perfil explícito:

```bash
docker compose --profile mcp-client up --build
```

El perfil evita arrancar el cliente por defecto hasta que tengas un servidor MCP compatible expuesto.

## Probar chat

```bash
curl -X POST http://localhost:8081/mcp-chat \
  -H "Content-Type: application/json" \
  -d '{"message":"¿Qué menú hay mañana?"}'
```

## Ver tools descubiertas

```bash
curl http://localhost:8081/mcp-chat/tools
```

> Nota: el servidor configurado debe exponer un endpoint MCP compatible. Este cliente está preparado para transporte `streamable-http`.
