# restaurant-ai-whatsapp-mvp (Twilio Sandbox)

MVP de chatbot para restaurantes con **Java 21 + Spring Boot + Spring AI + PostgreSQL**, conectado a **WhatsApp mediante Twilio Sandbox**.

## Flujo

WhatsApp -> Twilio Sandbox -> `POST /webhooks/twilio` -> `ChatService` -> Spring AI tools -> PostgreSQL -> respuesta -> Twilio API.

## Configuración requerida

Variables de entorno:

- `OPENAI_API_KEY`
- `TWILIO_ACCOUNT_SID`
- `TWILIO_AUTH_TOKEN`
- `TWILIO_WHATSAPP_NUMBER` (por defecto sandbox: `whatsapp:+14155238886`)

## Arranque

```bash
docker compose up --build
```

## Exponer webhook con ngrok

1. Arranca app en `http://localhost:8080`.
2. Ejecuta:

```bash
ngrok http 8080
```

3. Copia URL HTTPS pública (ej: `https://abc123.ngrok-free.app`).

## Conectar Twilio Sandbox

1. En Twilio Console -> Messaging -> Try it out -> Send a WhatsApp message -> Sandbox.
2. Une tu teléfono enviando el código de join al número sandbox.
3. En "When a message comes in" coloca:

```text
https://TU_NGROK/webhooks/twilio
```

4. Método: `HTTP POST`.

## Webhook esperado

- Endpoint: `POST /webhooks/twilio`
- Content-Type: `application/x-www-form-urlencoded`
- Parámetros usados:
  - `From`
  - `Body`

## Prueba rápida

Desde tu WhatsApp (el número unido al sandbox), envía:

- "¿Qué hay hoy de menú?"
- "Reserva una mesa mañana a las 14 para 2 personas, soy Ana, +34600111222"
- "Cancela mi reserva del teléfono +34600111222"

La app responderá automáticamente usando Spring AI + tools actuales.


## Evoluciones incluidas

- Consulta de menú por fecha con tool `getMenuByDate` (ej: mañana, pasado, este jueves, 2026-05-23).
- Memoria básica por número de WhatsApp (contexto por `From`).
- Reservas validadas con fecha real actual/futura y datos demo en mayo de 2026.
