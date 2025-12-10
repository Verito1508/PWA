/* sw.js: caché básico PWA */
const VERSION = 'v1';
const STATIC_CACHE = `static-${VERSION}`;
const STATIC_ASSETS = [
  '/',                     // index.html
  '/index.html',
  '/manifest.webmanifest',

  '/icons/icon-192.png',
  '/icons/icon-512.png'
];

// Install: pre-cache estáticos
self.addEventListener('install', e => {
  console.log('Service Worker: Install');

  e.waitUntil(caches.open(STATIC_CACHE).then(c => c.addAll(STATIC_ASSETS)));
  self.skipWaiting();
});

// Activate: limpia cachés viejos
self.addEventListener('activate', e => {
  console.log('Service Worker: Activate');
  e.waitUntil((async () => {
    const keys = await caches.keys();
    await Promise.all(keys.map(k => (k !== STATIC_CACHE ? caches.delete(k) : null)));
    await self.clients.claim();
  })());
});

// Fetch: estrategias
self.addEventListener('fetch', e => {
  const req = e.request;
  const url = new URL(req.url);

  // 1) API → network-first con fallback offline
  if (url.pathname.startsWith('/api/')) {
    e.respondWith(networkFirst(req));
    return;
  }

  // 2) Navegación (document) → network con fallback a index cacheado
  if (req.mode === 'navigate') {
    e.respondWith(fetch(req).catch(() => caches.match('/')));
    return;
  }

  // 3) Estáticos → stale-while-revalidate
  e.respondWith(staleWhileRevalidate(req));
});

async function networkFirst(req) {
  try {
    return await fetch(req);
  } catch {
    const body = JSON.stringify(
      { error: 'offline', detail: 'Sin conexión. Intenta de nuevo más tarde.' },
      null, 2
    );
    return new Response(body, { status: 503, headers: { 'Content-Type': 'application/json' } });
  }
}

async function staleWhileRevalidate(req) {
  const cache = await caches.open(STATIC_CACHE);
  const cached = await cache.match(req);
  const network = fetch(req).then(res => { cache.put(req, res.clone()); return res; }).catch(() => null);
  return cached || network || new Response('', { status: 504 });
}
