import express, { type Request, type Response } from 'express';
import cors from 'cors';
import { v4 as uuidv4 } from 'uuid';
import path from 'path';
import { fileURLToPath } from 'url';
import {
  createConversation,
  getConversation,
  addMessage,
  subscribe,
  unsubscribe,
} from './store.js';
import { zbc } from './camunda.js';

const __dirname = path.dirname(fileURLToPath(import.meta.url));

const app = express();
app.use(express.json());
app.use(cors());
app.use(express.static(path.join(__dirname, '..', 'public')));

/** POST /api/chat/start — start a new chat conversation */
app.post('/api/chat/start', async (req: Request, res: Response) => {
  const { userName, initialMessage } = req.body as {
    userName?: string;
    initialMessage?: string;
  };

  if (!userName?.trim() || !initialMessage?.trim()) {
    res.status(400).json({ error: 'userName and initialMessage are required' });
    return;
  }

  const conversationId = uuidv4();
  createConversation(conversationId, userName);

  addMessage(conversationId, {
    sender: 'user',
    senderName: userName,
    content: initialMessage,
  });

  await zbc.createProcessInstance({
    bpmnProcessId: 'chat-support-process',
    variables: {
      conversationId,
      userName,
      currentMessage: initialMessage,
    },
  });

  console.log(`[Server] Started conversation ${conversationId} for user "${userName}"`);
  res.json({ conversationId });
});

/** POST /api/chat/:conversationId/message — send a user message */
app.post('/api/chat/:conversationId/message', async (req: Request, res: Response) => {
  const conversationId = req.params['conversationId'] as string;
  const { message } = req.body as { message?: string };

  const conv = getConversation(conversationId);
  if (!conv) {
    res.status(404).json({ error: 'Conversation not found' });
    return;
  }

  if (!message?.trim()) {
    res.status(400).json({ error: 'message is required' });
    return;
  }

  addMessage(conversationId, {
    sender: 'user',
    senderName: conv.userName,
    content: message,
  });

  await zbc.publishMessage({
    name: 'user-message',
    correlationKey: conversationId,
    variables: { currentMessage: message },
  });

  console.log(`[Server] User message published for conversation ${conversationId}`);
  res.json({ success: true });
});

/** GET /api/chat/:conversationId/messages — fetch message history */
app.get('/api/chat/:conversationId/messages', (req: Request, res: Response) => {
  const conversationId = req.params['conversationId'] as string;
  const conv = getConversation(conversationId);

  if (!conv) {
    res.status(404).json({ error: 'Conversation not found' });
    return;
  }

  res.json(conv.messages);
});

/** GET /api/chat/:conversationId/events — SSE stream for real-time updates */
app.get('/api/chat/:conversationId/events', (req: Request, res: Response) => {
  const conversationId = req.params['conversationId'] as string;
  const conv = getConversation(conversationId);

  if (!conv) {
    res.status(404).json({ error: 'Conversation not found' });
    return;
  }

  res.setHeader('Content-Type', 'text/event-stream');
  res.setHeader('Cache-Control', 'no-cache');
  res.setHeader('Connection', 'keep-alive');
  res.flushHeaders();

  subscribe(conversationId, res);

  const keepalive = setInterval(() => {
    res.write(': keepalive\n\n');
  }, 25000);

  req.on('close', () => {
    clearInterval(keepalive);
    unsubscribe(conversationId, res);
  });
});

export function startServer(port: number = 3000): void {
  app.listen(port, () => {
    console.log(`🤖 Camunda Robotics Chat server running at http://localhost:${port}`);
  });
}
