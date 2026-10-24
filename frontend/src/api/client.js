import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json'
  }
});

// Attach correlation ID to every outgoing request
api.interceptors.request.use((config) => {
  if (!config.headers['X-Correlation-ID']) {
    config.headers['X-Correlation-ID'] = 'ui-' + Math.random().toString(36).substring(2, 10);
  }
  return config;
}, (error) => Promise.reject(error));

export default api;
