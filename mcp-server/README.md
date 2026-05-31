# restaurant-mcp-server

Servidor MCP puro para el restaurante. No contiene lógica de Twilio ni lógica conversacional con OpenAI.

## Responsabilidad

- Exponer tools MCP para el dominio restaurante.
- Persistir menús y reservas en PostgreSQL.
- Mantener la lógica de negocio cerca de la base de datos.

## Tools

- `getTodayMenu`
- `getMenuByDate`
- `createReservation`
- `cancelReservation`

## Configuración

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `SERVER_PORT` (default `8080`)

## Endpoint MCP

- `POST/GET /mcp` usando transporte MCP Streamable HTTP.

## Ejecutar local

```bash
mvn spring-boot:run
```
