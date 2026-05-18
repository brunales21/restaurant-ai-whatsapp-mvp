# restaurant-ai-whatsapp-mvp

MVP funcional de chatbot para restaurantes con **Java 21 + Spring Boot 4 + Spring AI + PostgreSQL**, conectado con **WhatsApp Cloud API** y con **tool calling real**.

## Qué demuestra este MVP

- Responder por WhatsApp usando lenguaje natural.
- Consultar menú del día en PostgreSQL.
- Crear y cancelar reservas en PostgreSQL.
- Decidir automáticamente qué tool ejecutar según la intención del usuario.

## Arquitectura (simple y demo-friendly)

- `controller`: endpoints REST locales + webhook de WhatsApp.
- `service`: orquestación de chat, menú y reservas.
- `repository`: acceso JPA a PostgreSQL.
- `entity`: tablas `daily_menus` y `reservations`.
- `ai/tools`: tools expuestas al LLM con `@Tool`.
- `client`: cliente HTTP para responder a WhatsApp Cloud API.
- `configuration`: configuración de `ChatClient`.

## Flujo completo

1. Usuario escribe en WhatsApp.
2. Meta llama al webhook `POST /webhooks/whatsapp`.
3. El sistema extrae el texto y teléfono.
4. `ChatService` manda el mensaje al LLM con Spring AI.
5. El LLM decide si llamar `getTodayMenu`, `createReservation` o `cancelReservation`.
6. Tool ejecuta lógica real en PostgreSQL.
7. El LLM redacta respuesta natural final.
8. Se envía respuesta al usuario mediante WhatsApp Cloud API.

## Tools incluidas (solo MVP)

- `getTodayMenu`
- `createReservation`
- `cancelReservation`

## Estructura del proyecto

```text
src/main/java/com/restaurant/mvp
├── ai/tools/RestaurantTools.java
├── client/WhatsappClient.java
├── config/AiConfig.java
├── controller/ChatController.java
├── controller/WhatsappWebhookController.java
├── dto/
├── entity/
├── repository/
└── service/
```

## Base de datos

Tablas mínimas:

- `daily_menus`
- `reservations`

Scripts:

- `src/main/resources/schema.sql`
- `src/main/resources/data.sql`

## Configuración

Variables principales:

- `OPENAI_API_KEY`
- `OPENAI_MODEL` (default `gpt-4o-mini`)
- `WHATSAPP_PHONE_NUMBER_ID`
- `WHATSAPP_ACCESS_TOKEN`
- `WHATSAPP_VERIFY_TOKEN`

## Arranque rápido con Docker Compose

```bash
docker compose up --build
```

Servicios:

- App: `http://localhost:8080`
- Swagger: `http://localhost:8080/swagger-ui.html`
- PostgreSQL: `localhost:5432`

## Probar local sin WhatsApp

### Endpoint de chat

`POST /chat`

```json
{
  "message": "¿Qué hay hoy de menú?"
}
```

Ejemplo con curl:

```bash
curl -X POST http://localhost:8080/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"Reserva una mesa mañana a las 14 para 3 personas, soy Laura y mi teléfono es +34600123456"}'
```

## Configurar webhook de WhatsApp Cloud API

1. En Meta Developers, configura la URL de callback:
   - `GET/POST https://TU_DOMINIO/webhooks/whatsapp`
2. Configura verify token igual a `WHATSAPP_VERIFY_TOKEN`.
3. Suscribe evento de mensajes.
4. Añade teléfono de prueba en sandbox.

### Verificación webhook

- Endpoint: `GET /webhooks/whatsapp`
- Valida `hub.verify_token` y devuelve `hub.challenge`.

## Ejemplos reales de conversación

- Usuario: "¿Qué hay hoy de menú?"
  - LLM -> `getTodayMenu()` -> Respuesta natural.
- Usuario: "Reserva una mesa mañana a las 14 para 2 personas. Soy Ana, +34600111222"
  - LLM -> `createReservation(...)` -> Confirmación con ID.
- Usuario: "Cancela mi reserva del teléfono +34600111222"
  - LLM -> `cancelReservation(null, "+34600111222")` -> Cancelación confirmada.

## OpenAPI

- UI: `/swagger-ui.html`
- Incluye endpoint `/chat` y webhook `/webhooks/whatsapp`.

## Evolución futura (sin romper simplicidad)

- Añadir autenticación del webhook por firma.
- Añadir reglas de disponibilidad horaria.
- Añadir endpoint de backoffice para gestionar menús.
