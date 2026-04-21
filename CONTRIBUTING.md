# Contribution guide

## Prerequisites

| Tool      | Version                  |
|-----------|--------------------------|
| Node.js   | ≥ 20                     |
| npm       | ≥ 9                      |
| Java      | 21                       |
| Maven     | ≥ 3.9                    |
| Camunda 8 | SaaS or Self-Managed 8.x |

## Chat Application

### Architecture

```
Browser (HTML/CSS/JS)
  │
  │  HTTP REST  /  Server-Sent Events
  ▼
Express server (Node.js / TypeScript)
  │
  │  Camunda TypeScript SDK  (@camunda8/sdk)
  ▼
Camunda 8 (SaaS or Self-Managed)
  │
  │  Job Worker: "send-chat-message"
  ▼
Back to Express → SSE → Browser
```

* **Frontend** — `chat-app/public/` (plain HTML + CSS + vanilla JS, no build step needed)
* **Backend** — `chat-app/src/` TypeScript, compiled to `chat-app/dist/`
* **BPMN** — `src/main/resources/bpmn/chat-support.bpmn`

### Configuration

The app is configured in the file `chat-app/.env.example`.

```env
CAMUNDA_AUTH_STRATEGY=NONE
CAMUNDA_REST_ADDRESS=http://localhost:8080/v2
PORT=3000
```

### Run in development mode

```bash
cd chat-app
npm install
cp .env.example .env   # fill in credentials
npm run dev            # runs via ts-node, watches for changes
```

## Spring Boot process application

> TODO describe the architecture, configuration, and how to run the Spring Boot process application

### Build and run

```bash
# Run the Spring Boot app (deploys BPMN, starts Java workers)
mvn spring-boot:run
```