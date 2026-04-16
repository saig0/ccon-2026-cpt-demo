import type { Response } from 'express';

export interface ChatMessage {
  id: string;
  conversationId: string;
  sender: 'user' | 'agent';
  senderName: string;
  content: string;
  timestamp: string;
}

export interface Conversation {
  id: string;
  userName: string;
  messages: ChatMessage[];
  subscribers: Response[];
}

const conversations = new Map<string, Conversation>();

export function createConversation(id: string, userName: string): Conversation {
  const conv: Conversation = {
    id,
    userName,
    messages: [],
    subscribers: [],
  };
  conversations.set(id, conv);
  return conv;
}

export function getConversation(id: string): Conversation | undefined {
  return conversations.get(id);
}

export function addMessage(
  conversationId: string,
  message: Omit<ChatMessage, 'id' | 'conversationId' | 'timestamp'>
): ChatMessage | null {
  const conv = conversations.get(conversationId);
  if (!conv) return null;

  const msg: ChatMessage = {
    id: crypto.randomUUID(),
    conversationId,
    timestamp: new Date().toISOString(),
    ...message,
  };

  conv.messages.push(msg);
  notifySubscribers(conv, msg);
  return msg;
}

function notifySubscribers(conv: Conversation, message: ChatMessage): void {
  const data = `data: ${JSON.stringify(message)}\n\n`;
  conv.subscribers = conv.subscribers.filter((res) => {
    try {
      res.write(data);
      return true;
    } catch {
      return false;
    }
  });
}

export function subscribe(conversationId: string, res: Response): void {
  const conv = conversations.get(conversationId);
  if (conv) {
    conv.subscribers.push(res);
  }
}

export function unsubscribe(conversationId: string, res: Response): void {
  const conv = conversations.get(conversationId);
  if (conv) {
    conv.subscribers = conv.subscribers.filter((s) => s !== res);
  }
}
