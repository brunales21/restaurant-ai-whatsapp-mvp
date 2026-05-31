# restaurant-ai-whatsapp-platform

Plataforma separada en dos proyectos Spring Boot:

```text
mcp-server/  -> servidor MCP puro con tools de restaurante y PostgreSQL
mcp-client/  -> cliente MCP con chatbot OpenAI, Twilio WhatsApp y endpoint REST local
```

## Decisión de estructura

Recomiendo mantener ambos proyectos en este repositorio por ahora, pero en carpetas separadas:

- `mcp-server` no conoce Twilio ni OpenAI chat; solo expone tools MCP y accede a PostgreSQL.
- `mcp-client` contiene la experiencia conversacional: OpenAI, memoria por conversación, webhook Twilio y endpoint REST.
- Docker Compose levanta ambos de forma coordinada para demos.
- Si más adelante servidor y cliente tienen ciclos de vida distintos, se puede extraer cada carpeta a su propio repositorio sin mezclar código.

## Arquitectura final

```text
WhatsApp / REST local
        |
        v
mcp-client (8081)
  - /webhooks/twilio
  - /mcp-chat
  - OpenAI ChatClient
  - descubre tools MCP remotas
        |
        v
mcp-server (8080)
  - /mcp
  - tools: menú y reservas
  - PostgreSQL
        |
        v
PostgreSQL
```

## Levantar todo con Docker Compose

```bash
docker compose up --build
```

Servicios:

- MCP server: `http://localhost:8080`
- MCP endpoint: `http://localhost:8080/mcp`
- MCP client: `http://localhost:8081`
- Chat REST local: `POST http://localhost:8081/mcp-chat`
- Twilio webhook: `POST http://localhost:8081/webhooks/twilio`
- PostgreSQL: `localhost:5432`

## Variables necesarias

Para el cliente:

- `OPENAI_API_KEY`
- `OPENAI_MODEL` (default `gpt-4o-mini`)
- `TWILIO_ACCOUNT_SID`
- `TWILIO_AUTH_TOKEN`
- `TWILIO_WHATSAPP_NUMBER` (default sandbox `whatsapp:+14155238886`)

Para el servidor:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

Docker Compose ya cablea `mcp-client` hacia `mcp-server` con:

- `RESTAURANT_MCP_SERVER_URL=http://mcp-server:8080`
- `RESTAURANT_MCP_SERVER_ENDPOINT=/mcp`

## Probar por REST local

```bash
curl -X POST http://localhost:8081/mcp-chat \
  -H "Content-Type: application/json" \
  -d '{"message":"¿Qué menú hay mañana?","phone":"34640064806"}'
```

## Probar tools descubiertas

```bash
curl http://localhost:8081/mcp-chat/tools
```

## Twilio Sandbox

Configura en Twilio Sandbox el webhook de mensajes entrantes apuntando a:

```text
https://TU_URL_PUBLICA/webhooks/twilio
```

En local necesitas exponer el puerto `8081` con ngrok/cloudflared o desplegar el `mcp-client` en una URL pública HTTPS.
