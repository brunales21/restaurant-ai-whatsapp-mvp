# restaurant-mcp-client

Cliente MCP y chatbot del restaurante. Aquí vive toda la lógica conversacional y de canal:

- OpenAI `ChatClient`
- conexión a MCP server remoto
- memoria por conversación
- webhook Twilio WhatsApp
- endpoint REST local `/mcp-chat`

## Responsabilidad

El cliente recibe mensajes de WhatsApp o REST, decide con el LLM qué necesita hacer y llama tools MCP remotas expuestas por `mcp-server`.

## Variables

- `OPENAI_API_KEY`: API key de OpenAI.
- `OPENAI_MODEL`: modelo de chat, por defecto `gpt-4o-mini`.
- `RESTAURANT_MCP_SERVER_URL`: URL base del MCP server.
- `RESTAURANT_MCP_SERVER_ENDPOINT`: endpoint MCP, por defecto `/mcp`.
- `MCP_CLIENT_PORT`: puerto HTTP del cliente, por defecto `8081`.
- `TWILIO_ACCOUNT_SID`
- `TWILIO_AUTH_TOKEN`
- `TWILIO_WHATSAPP_NUMBER`


## Evitar error 404 en `/mcp`

Cuando cliente y servidor corren con Docker Compose, configura el servidor MCP con la URL interna:

```bash
RESTAURANT_MCP_SERVER_URL=http://mcp-server:8080
RESTAURANT_MCP_SERVER_ENDPOINT=/mcp
```

No uses la URL pública de ngrok para `RESTAURANT_MCP_SERVER_URL` salvo que esa URL esté exponiendo realmente el `mcp-server`.
Normalmente ngrok debe apuntar al cliente (`8081`) para Twilio, no al servidor MCP.

## Endpoints

- `POST /mcp-chat`: chat local usando tools MCP remotas.
- `GET /mcp-chat/tools`: lista tools descubiertas.
- `POST /webhooks/twilio`: webhook Twilio `application/x-www-form-urlencoded`.

## Ejecutar local

```bash
mvn spring-boot:run
```

## Ejecutar con Docker Compose

Desde la raíz del repositorio:

```bash
docker compose up --build
```

## Probar chat local

```bash
curl -X POST http://localhost:8081/mcp-chat \
  -H "Content-Type: application/json" \
  -d '{"message":"Reserva mañana a las 19 para 4 personas, soy Charly","phone":"34640064806"}'
```

## Twilio

Twilio enviará `From` y `Body` al webhook. El cliente extrae el teléfono real desde `From`, lo pasa en el prompt y el LLM debe usarlo al invocar la tool remota `createReservation`.
