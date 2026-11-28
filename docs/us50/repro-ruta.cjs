const http = require('node:http');
const fs = require('node:fs/promises');
const path = require('node:path');
const root = path.resolve(process.argv[2]);
const server = http.createServer(async (request, response) => {
  try {
    response.end(await fs.readFile(path.join(root, request.url)));
  } catch {
    response.writeHead(404).end();
  }
});
server.listen(0, '127.0.0.1', async () => {
  try {
    const base = 'http://127.0.0.1:' + server.address().port;
    async function get(url) {
      const result = await fetch(base + url);
      console.log('GET', url, result.status);
      return result.text();
    }
    const index = await get('/admin/index.html');
    const route = index.match(/href="([^"]*productos-mas-vendidos\.html)"/)[1];
    const report = await get(route);
    const existing = report || await get('/admin/producto-mas-vendido.html');
    for (const [, asset] of existing.matchAll(/(?:src|href)="([^"]*productos-mas-vendidos\.(?:js|css))"/g)) {
      await get(asset);
    }
  } finally {
    server.close();
  }
});
