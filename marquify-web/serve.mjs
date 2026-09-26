import { createServer } from 'node:http';
import { readFile } from 'node:fs/promises';
import { extname, join, normalize } from 'node:path';

const root = new URL('.', import.meta.url).pathname;
const types = { '.html': 'text/html; charset=utf-8', '.js': 'text/javascript; charset=utf-8', '.css': 'text/css; charset=utf-8' };
createServer(async (request, response) => {
  const path = normalize(join(root, request.url === '/' ? 'index.html' : request.url.split('?')[0]));
  if (!path.startsWith(root)) return response.writeHead(403).end();
  try { response.writeHead(200, { 'Content-Type': types[extname(path)] || 'application/octet-stream' }); response.end(await readFile(path)); }
  catch { response.writeHead(404).end('Arquivo não encontrado'); }
}).listen(5173, () => console.log('Marquify Web em http://localhost:5173'));
