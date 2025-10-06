const { createProxyMiddleware } = require('http-proxy-middleware');

module.exports = function (app) {
  console.log('[proxy] setupProxy.js loaded');
  // Proxy all API requests to the Spring Boot backend
  app.use(
    '/api',
    createProxyMiddleware({
      target: 'http://localhost:8080/api',
      changeOrigin: true,
      secure: false,
      ws: true,
      logLevel: 'debug',
      pathRewrite: {
        '^/api': '',
      },
      onProxyReq: (proxyReq, req, res) => {
        console.log(`[proxy] -> ${req.method} ${req.originalUrl} -> ${proxyReq.getHeader('host')}${proxyReq.path}`);
      },
      onProxyRes: (proxyRes, req, res) => {
        console.log(`[proxy] <- ${req.method} ${req.originalUrl} <- ${proxyRes.statusCode}`);
      },
    })
  );
};