import { startServer } from './server.js';
import { startWorker } from './worker.js';

const PORT = parseInt(process.env.PORT ?? '3000', 10);

startWorker();
startServer(PORT);
