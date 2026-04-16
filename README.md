# ccon-2026-cpt-demo
Demo application for CPT (CCon 2026)

## Camunda Robotics Chat Application

A live customer-support chat website for **Camunda Robotics** — a fictional producer of smart robots and robotic tools. Users can contact a support agent to get help with product issues, upgrades, warranties, and more.

The chat is backed by a **Camunda process** that orchestrates agent responses. Every conversation is identified by a UUID used to correlate messages between the frontend and the running process instance.

---

## Architecture

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

---

## Prerequisites

| Tool       | Version  |
|------------|----------|
| Node.js    | ≥ 20     |
| npm        | ≥ 9      |
| Java       | 21       |
| Maven      | ≥ 3.9    |
| Camunda 8  | SaaS or Self-Managed 8.x |

---

## Building and Running

### Option A — Maven (embedded build)

The `frontend-maven-plugin` installs Node.js locally and builds the TypeScript application automatically during the Maven lifecycle:

```bash
mvn package
```

This runs `npm install` and `npm run build` inside `chat-app/` as part of the `generate-resources` phase.

### Option B — Standalone (npm only)

```bash
cd chat-app

# Install dependencies
npm install

# Build TypeScript to JavaScript
npm run build

# Configure Camunda credentials (copy and edit the example)
cp .env.example .env
# edit .env with your Camunda cluster / self-managed settings

# Start the chat server (default port 3000)
npm start
```

Open [http://localhost:3000](http://localhost:3000) in your browser.

### Development mode (no build step)

```bash
cd chat-app
npm install
cp .env.example .env   # fill in credentials
npm run dev            # runs via ts-node, watches for changes
```

---

## Configuration

Copy `chat-app/.env.example` to `chat-app/.env` and fill in your credentials.

**Camunda SaaS:**
```env
CAMUNDA_AUTH_STRATEGY=OAUTH
CAMUNDA_REST_ADDRESS=https://<region>.zeebe.camunda.io/<cluster-id>
CAMUNDA_CLIENT_ID=<your-client-id>
CAMUNDA_CLIENT_SECRET=<your-client-secret>
CAMUNDA_OAUTH_URL=https://login.cloud.camunda.io/oauth/token
CAMUNDA_TOKEN_AUDIENCE=zeebe.camunda.io
PORT=3000
```

**Self-Managed (no auth):**
```env
CAMUNDA_AUTH_STRATEGY=NONE
CAMUNDA_REST_ADDRESS=http://localhost:8080/v2
PORT=3000
```

---

## BPMN Process

The chat process (`chat-support-process`) loops indefinitely until the process instance is cancelled:

1. **Start** — process instance created by the chat app (variables: `conversationId`, `userName`, `currentMessage`)
2. **Service Task** `send-chat-message` — job worker generates an agent reply and pushes it to the SSE stream
3. **Intermediate Message Catch** `user-message` — waits for the next user message, correlated by `conversationId`
4. **Loop** back to step 2

Deploy the BPMN to your Camunda cluster before starting the chat app. If you run the Spring Boot application, the BPMN files in `src/main/resources/bpmn/` are deployed automatically on startup.

---

## Java Process Application

The Spring Boot application (`ProcessOrderApplication`) deploys all BPMN files on startup (including `chat-support.bpmn`) and contains the existing "Process Order" workers.

```bash
# Run the Spring Boot app (deploys BPMN, starts Java workers)
mvn spring-boot:run
```
