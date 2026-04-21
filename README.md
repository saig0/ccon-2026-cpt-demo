# ccon-2026-cpt-demo

Demo application for CPT (CCon 2026)

## Scenario

A live customer-support chat website for **Camunda Robotics** — a fictional producer of smart robots and robotic tools.
Users can contact a support agent to get help with product issues, upgrades, warranties, and more.

> TODO: add process diagram

## Content

> TODO: link to code and process resources

## Manual testing

### Install Camunda 8 Run

- [Download Camunda 8 Run](https://developers.camunda.com/install-camunda-8/)
- Add the following Connector secrets to the `.env` file of Camunda 8 Run:

```env
SECRET_AWS_BEDROCK_ACCESS_KEY=YOUR_ACCESS_KEY
SECRET_AWS_BEDROCK_SECRET_KEY=YOUR_SECRET_KEY
```

- Start Camunda 8 Run

### Run the Chat Application

Run the following commands in your terminal to start the chat application:

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

### Run the Spring Boot process application

### Inspect the process in Camunda 8

Open [http://localhost:8080/operate](http://localhost:8080/operate) to inspect the process instances in Operate (login:
`demo/demo`).

## Resources

- [Camunda Process Test: documentation](https://docs.camunda.io/docs/apis-tools/testing/getting-started/)
