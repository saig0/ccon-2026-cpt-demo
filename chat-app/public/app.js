/* ============================================================
   Camunda Robotics Chat — Frontend Application
   Handles both the landing page (index.html) and chat (chat.html)
   ============================================================ */

'use strict';

// ── Shared helpers ─────────────────────────────────────────────
function formatTime(isoString) {
  try {
    return new Date(isoString).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  } catch {
    return '';
  }
}

// ── Landing page logic ─────────────────────────────────────────
const startForm = document.getElementById('startForm');
if (startForm) {
  const startBtn = document.getElementById('startBtn');
  const startError = document.getElementById('startError');

  startForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    startBtn.disabled = true;
    startBtn.querySelector('.btn-text').textContent = 'Connecting…';
    startError.hidden = true;

    const userName = document.getElementById('userName').value.trim();
    const initialMessage = document.getElementById('initialMessage').value.trim();

    try {
      const res = await fetch('/api/chat/start', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ userName, initialMessage }),
      });

      if (!res.ok) {
        const err = await res.json().catch(() => ({ error: 'Unknown error' }));
        throw new Error(err.error || `Server error ${res.status}`);
      }

      const { conversationId } = await res.json();
      window.location.href = `/chat.html?id=${encodeURIComponent(conversationId)}`;
    } catch (err) {
      startError.textContent = `Could not start chat: ${err.message}`;
      startError.hidden = false;
      startBtn.disabled = false;
      startBtn.querySelector('.btn-text').textContent = 'Start Chat';
    }
  });
}

// ── Chat page logic ─────────────────────────────────────────────
const messageForm = document.getElementById('messageForm');
if (messageForm) {
  const params = new URLSearchParams(window.location.search);
  const conversationId = params.get('id');

  if (!conversationId) {
    window.location.href = '/';
  } else {
    initChat(conversationId);
  }
}

async function initChat(conversationId) {
  const messagesInner = document.getElementById('messagesInner');
  const messageInput = document.getElementById('messageInput');
  const sendBtn = document.getElementById('sendBtn');
  const sendError = document.getElementById('sendError');
  const typingIndicator = document.getElementById('typingIndicator');
  const convIdDisplay = document.getElementById('convIdDisplay');

  convIdDisplay.textContent = conversationId;
  convIdDisplay.title = conversationId;

  // Load existing messages (page refresh recovery)
  try {
    const res = await fetch(`/api/chat/${encodeURIComponent(conversationId)}/messages`);
    if (res.ok) {
      const messages = await res.json();
      messages.forEach((msg) => appendMessage(msg));
    }
  } catch {
    // ignore, SSE will bring new messages
  }

  // Enable input after history loaded
  messageInput.disabled = false;
  sendBtn.disabled = false;
  messageInput.focus();

  // Subscribe to real-time updates via SSE
  const evtSource = new EventSource(`/api/chat/${encodeURIComponent(conversationId)}/events`);

  evtSource.onmessage = (event) => {
    const msg = JSON.parse(event.data);
    // Only append agent messages; user messages are appended immediately on send
    if (msg.sender === 'agent') {
      hideTyping();
      appendMessage(msg);
    }
  };

  evtSource.onerror = () => {
    // Connection dropped — SSE auto-reconnects, no action needed
    console.warn('SSE connection interrupted, will reconnect…');
  };

  // Send message handler
  document.getElementById('messageForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const text = messageInput.value.trim();
    if (!text) return;

    sendBtn.disabled = true;
    messageInput.disabled = true;
    sendError.hidden = true;
    messageInput.value = '';

    // Optimistically show user message
    appendMessage({
      id: crypto.randomUUID(),
      conversationId,
      sender: 'user',
      senderName: 'You',
      content: text,
      timestamp: new Date().toISOString(),
    });

    showTyping();

    try {
      const res = await fetch(`/api/chat/${encodeURIComponent(conversationId)}/message`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ message: text }),
      });

      if (!res.ok) {
        const err = await res.json().catch(() => ({ error: 'Unknown error' }));
        throw new Error(err.error || `Server error ${res.status}`);
      }
    } catch (err) {
      hideTyping();
      sendError.textContent = `Could not send message: ${err.message}`;
      sendError.hidden = false;
    } finally {
      sendBtn.disabled = false;
      messageInput.disabled = false;
      messageInput.focus();
    }
  });

  // ── DOM helpers ──────────────────────────────────────────────
  function appendMessage(msg) {
    const wrapper = document.createElement('div');
    wrapper.className = `msg msg-${msg.sender}`;
    wrapper.dataset.id = msg.id;

    const meta = document.createElement('div');
    meta.className = 'msg-meta';
    meta.textContent = formatTime(msg.timestamp);

    const bubble = document.createElement('div');
    bubble.className = 'msg-bubble';
    bubble.textContent = msg.content;

    if (msg.sender === 'agent') {
      const avatar = document.createElement('div');
      avatar.className = 'robot-avatar small';
      avatar.textContent = '🤖';
      wrapper.append(avatar);
    }

    const inner = document.createElement('div');
    inner.style.display = 'flex';
    inner.style.flexDirection = 'column';
    inner.style.gap = '2px';

    if (msg.sender === 'user') {
      inner.append(bubble, meta);
    } else {
      inner.append(meta, bubble);
    }

    wrapper.append(inner);

    // Insert before the typing indicator
    messagesInner.insertBefore(wrapper, typingIndicator);
    scrollToBottom();
  }

  function showTyping() {
    typingIndicator.hidden = false;
    scrollToBottom();
  }

  function hideTyping() {
    typingIndicator.hidden = true;
  }

  function scrollToBottom() {
    const container = document.getElementById('chatMessages');
    container.scrollTop = container.scrollHeight;
  }
}
