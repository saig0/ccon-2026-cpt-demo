import {client} from './camunda.js';
import {addMessage} from './store.js';

interface ChatJobVariables {
    conversationId: string;
    userName: string;
    message: string;
}

interface AgentResponseEntry {
    keywords: string[];
    response: string;
}

const agentResponses: AgentResponseEntry[] = [
    {
        keywords: ['hello', 'hi', 'hey', 'greetings', 'good morning', 'good afternoon'],
        response:
            "Hello! I'm RoboSupport 3000, your Camunda Robotics assistant. How can I help you today? 🤖",
    },
    {
        keywords: ['upgrade', 'update', 'version', 'new model', 'latest'],
        response:
            'Great news! We have several exciting upgrades available for your Camunda Robotics products. Our latest models feature enhanced AI processing and improved joint flexibility. Would you like to hear more about our upgrade packages? 🚀',
    },
    {
        keywords: ['problem', 'issue', 'error', 'bug', 'broken', 'not working', 'fail', 'trouble'],
        response:
            "I'm sorry to hear you're experiencing issues. Let me connect you with our technical support team. In the meantime, have you tried turning the robot off and on again? (It works surprisingly often!) 🔧",
    },
    {
        keywords: ['price', 'cost', 'pricing', 'buy', 'purchase', 'order'],
        response:
            'Our pricing varies by model and configuration. The starter CR-200 begins at 9,999 credits. For enterprise solutions with full automation suites, we offer custom pricing. Would you like to schedule a demo? 💰',
    },
    {
        keywords: ['warranty', 'guarantee', 'repair', 'service', 'maintenance'],
        response:
            'All Camunda Robotics products come with a 2-year comprehensive warranty covering parts and labor. We also offer extended service contracts with 24/7 support. 🛡️',
    },
    {
        keywords: ['product', 'model', 'spec', 'specification', 'feature', 'robot'],
        response:
            'Our product lineup includes the CR-200 (home assistant), CR-500 (industrial worker), and CR-Pro (advanced AI companion). Each is built with precision engineering and powered by the Camunda automation platform! 🏭',
    },
    {
        keywords: ['thanks', 'thank you', 'great', 'awesome', 'perfect', 'excellent', 'helpful'],
        response:
            "You're very welcome! It's always a pleasure to assist fellow robotics enthusiasts. Is there anything else I can help you with? 😊",
    },
    {
        keywords: ['bye', 'goodbye', 'farewell', 'exit', 'done', 'that is all'],
        response:
            "Thank you for contacting Camunda Robotics Support! It was great chatting with you. Don't hesitate to reach out if you need anything else. Stay robotic! 🤖👋",
    },
];

function generateAgentResponse(message: string): string {
    const lowerMessage = message.toLowerCase();
    for (const {keywords, response} of agentResponses) {
        if (keywords.some((kw) => lowerMessage.includes(kw))) {
            return response;
        }
    }
    return "Thank you for your message! I'm analysing your request... 🧠 A human support specialist will review your case if further assistance is needed. Is there anything specific about our robotic products I can help with?";
}

export function startWorker(): void {
    console.log('Starting Camunda job worker for "send-chat-message"...');

    client.createJobWorker({
        jobType: 'send-chat-message',
        jobHandler: async (job) => {
            const {conversationId, userName, message} =
                job.variables as unknown as ChatJobVariables;

            addMessage(conversationId, {
                sender: 'agent',
                senderName: 'RoboSupport 3000',
                content: message,
            });

            console.log(`[Worker] Send responded to ${conversationId}: "${message}"`);

            return job.complete({lastResponse: message});
        },
    });

    console.log('Job worker started — listening for "send-chat-message" tasks.');
}
