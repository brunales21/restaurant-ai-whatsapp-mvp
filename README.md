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


## Configuración MCP importante

Si levantas la plataforma con Docker Compose, **no uses ngrok para `RESTAURANT_MCP_SERVER_URL`**.

Correcto dentro de Compose:

```bash
RESTAURANT_MCP_SERVER_URL=http://mcp-server:8080
RESTAURANT_MCP_SERVER_ENDPOINT=/mcp
```

Incorrecto para la conexión interna MCP:

```bash
RESTAURANT_MCP_SERVER_URL=https://TU_NGROK
```

Ngrok solo debe exponer el cliente para Twilio:

```bash
ngrok http 8081
# Twilio -> https://TU_NGROK/webhooks/twilio
```

Si apuntas el cliente MCP a un ngrok que realmente está exponiendo `mcp-client` (8081), Spring AI intentará llamar `https://TU_NGROK/mcp` y recibirá `404 path=/mcp`, porque `/mcp` existe en `mcp-server`, no en `mcp-client`.

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



## Optimización de memoria y tokens

El `mcp-client` mantiene solo una ventana reciente de contexto por conversación:

- máximo 6 interacciones recientes;
- máximo aproximado de 2.000 caracteres de contexto;
- máximo 500 conversaciones recientes en memoria del proceso;
- logs con número de interacciones y caracteres enviados como contexto.

Antes, el contexto conversacional crecía indefinidamente y se reenviaba al modelo en cada turno, aumentando coste y latencia. Ahora se descartan interacciones antiguas y, cuando el usuario necesita información real de reservas, el modelo puede usar `getReservationsByPhone(phone)` para consultar la base de datos en vez de depender de memoria acumulada.

## Seguridad al cancelar reservas

La cancelación se autentica por teléfono:

- `mcp-client` extrae el teléfono real desde Twilio `From` y se lo pasa al LLM como teléfono autorizado.
- Para cancelar, el LLM debe llamar `cancelReservation` con `requesterPhone` igual a ese teléfono.
- `mcp-server` solo cancela si la reserva activa pertenece a ese teléfono normalizado; si el ID pertenece a otro cliente, devuelve un rechazo y no cambia la reserva.

Esto evita que un usuario cancele reservas de otro solo con conocer el ID.

## Twilio Sandbox

Configura en Twilio Sandbox el webhook de mensajes entrantes apuntando a:

```text
https://TU_URL_PUBLICA/webhooks/twilio
```

En local necesitas exponer el puerto `8081` con ngrok/cloudflared o desplegar el `mcp-client` en una URL pública HTTPS.
