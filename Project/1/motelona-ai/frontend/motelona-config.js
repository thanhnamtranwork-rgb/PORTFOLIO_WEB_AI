/*
 * Shared MoteLona endpoint configuration.
 * Local development uses the Java backend at 127.0.0.1:8080.
 * A public static website must set window.MOTELONA_API_BASE to its own HTTPS
 * backend URL before motelona-config.js is loaded. Never put API secrets here.
 */
(function () {
  'use strict';
  var host = (window.location && window.location.hostname || '').toLowerCase();
  var localHost = host === 'localhost' || host === '127.0.0.1' || host === '::1' || window.location.protocol === 'file:';
  var configured = typeof window.MOTELONA_API_BASE === 'string' && window.MOTELONA_API_BASE.trim();
  if (!configured) {
    window.MOTELONA_API_BASE = localHost ? 'http://127.0.0.1:8080' : '';
  }
  window.MOTELONA_API_BASE = String(window.MOTELONA_API_BASE || '').trim().replace(/\/$/, '');
})();
