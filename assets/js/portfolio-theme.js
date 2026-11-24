(function () {
  'use strict';

  var STORAGE_KEY = 'animfolio-theme';
  var allowed = ['midnight', 'tree', 'peach', 'apricot', 'rainbow'];
  var rainbowFrameId = null;
  var rainbowTimer = null;
  var rainbowHue = 0;
  var rainbowLastTime = null;
  var transitionTimer = null;
  var RAINBOW_DEGREES_PER_MS = 0.054; // 54 degrees/second; one full sweep takes about 6.7 seconds.
  var rainbowProps = [
    '--animfolio-accent', '--animfolio-accent-hover', '--animfolio-accent-light',
    '--animfolio-accent-subtle', '--animfolio-accent-shadow', '--animfolio-accent-glow',
    '--animfolio-secondary', '--animfolio-secondary-hover', '--animfolio-secondary-light',
    '--animfolio-accent-rgb', '--animfolio-secondary-rgb', '--animfolio-secondary-text',
    '--animfolio-gradient', '--animfolio-on-secondary', '--animfolio-accent-text',
    '--animfolio-focus-ring'
  ];

  function readTheme() {
    try {
      var saved = localStorage.getItem(STORAGE_KEY);
      return allowed.indexOf(saved) >= 0 ? saved : 'midnight';
    } catch (_) { return 'midnight'; }
  }

  function prefersReducedMotion() {
    return !!(window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches);
  }

  function hslToRgb(h, s, l) {
    h = ((h % 360) + 360) % 360 / 360;
    s /= 100;
    l /= 100;
    var r, g, b;
    if (s === 0) { r = g = b = l; }
    else {
      var q = l < 0.5 ? l * (1 + s) : l + s - l * s;
      var t = 2 * l - q;
      var hue = function (p, qv, t0) {
        if (t0 < 0) t0 += 1;
        if (t0 > 1) t0 -= 1;
        if (t0 < 1 / 6) return p + (qv - p) * 6 * t0;
        if (t0 < 1 / 2) return qv;
        if (t0 < 2 / 3) return p + (qv - p) * (2 / 3 - t0) * 6;
        return p;
      };
      r = hue(t, q, h + 1 / 3);
      g = hue(t, q, h);
      b = hue(t, q, h - 1 / 3);
    }
    return [r, g, b].map(function (v) { return Math.round(v * 255); }).join(', ');
  }

  function setRainbowFrame(root) {
    if (!root) return;
    var light = root.classList.contains('animfolio-container--light') || root.getAttribute('data-animfolio-scheme') === 'light';
    var accentRgb = hslToRgb(rainbowHue, 74, 47);
    var secondaryHue = (rainbowHue + 58) % 360;
    var secondaryRgb = hslToRgb(secondaryHue, 76, 64);
    var accent = 'hsl(' + rainbowHue.toFixed(2) + ' 74% 47%)';
    var hover = 'hsl(' + rainbowHue.toFixed(2) + ' 76% 39%)';
    var secondary = 'hsl(' + secondaryHue.toFixed(2) + ' 76% 64%)';
    var secondaryHover = 'hsl(' + secondaryHue.toFixed(2) + ' 68% 56%)';
    var thirdHue = (rainbowHue + 132) % 360;
    var values = {
      '--animfolio-accent': accent,
      '--animfolio-accent-hover': hover,
      '--animfolio-accent-light': 'rgba(' + accentRgb + ', .12)',
      '--animfolio-accent-subtle': 'rgba(' + accentRgb + ', .16)',
      '--animfolio-accent-shadow': 'rgba(' + accentRgb + ', .32)',
      '--animfolio-accent-glow': 'rgba(' + accentRgb + ', .42)',
      '--animfolio-secondary': secondary,
      '--animfolio-secondary-hover': secondaryHover,
      '--animfolio-secondary-light': 'rgba(' + secondaryRgb + ', .18)',
      '--animfolio-accent-rgb': accentRgb,
      '--animfolio-secondary-rgb': secondaryRgb,
      '--animfolio-secondary-text': '#17221B',
      '--animfolio-gradient': 'linear-gradient(120deg, ' + accent + ', ' + secondary + ', hsl(' + thirdHue.toFixed(2) + ' 76% 58%))',
      '--animfolio-on-secondary': '#17221B',
      '--animfolio-accent-text': light ? 'hsl(' + rainbowHue.toFixed(2) + ' 72% 31%)' : 'hsl(' + rainbowHue.toFixed(2) + ' 80% 70%)',
      '--animfolio-focus-ring': 'hsl(' + rainbowHue.toFixed(2) + ' 78% 58%)'
    };

    // The base HTML theme variables are declared with !important. Inline values
    // must use the same priority or the rainbow palette would only partly apply.
    Object.keys(values).forEach(function (property) {
      root.style.setProperty(property, values[property], 'important');
    });
  }

  function cancelRainbowAnimation() {
    if (rainbowFrameId !== null && typeof window.cancelAnimationFrame === 'function') {
      window.cancelAnimationFrame(rainbowFrameId);
    }
    if (rainbowTimer !== null) window.clearInterval(rainbowTimer);
    rainbowFrameId = null;
    rainbowTimer = null;
    rainbowLastTime = null;
  }

  function advanceRainbow(timestamp, root) {
    if (!root || !root.classList.contains('theme-rainbow-active')) return;
    var now = typeof timestamp === 'number' ? timestamp : Date.now();
    if (rainbowLastTime === null) rainbowLastTime = now;
    // Cap long pauses (for example, a background tab) so returning to the page
    // never causes a sudden color jump.
    var elapsed = Math.min(Math.max(now - rainbowLastTime, 0), 100);
    rainbowLastTime = now;
    rainbowHue = (rainbowHue + elapsed * RAINBOW_DEGREES_PER_MS) % 360;
    setRainbowFrame(root);

    if (typeof window.requestAnimationFrame === 'function') {
      rainbowFrameId = window.requestAnimationFrame(function (nextTimestamp) {
        advanceRainbow(nextTimestamp, root);
      });
    }
  }

  function clearRainbow(root) {
    cancelRainbowAnimation();
    if (!root) return;
    root.classList.remove('theme-rainbow-active');
    rainbowProps.forEach(function (property) { root.style.removeProperty(property); });
  }

  function startRainbow(root) {
    cancelRainbowAnimation();
    rainbowHue = 0;
    root.classList.add('theme-rainbow-active');
    setRainbowFrame(root);
    if (prefersReducedMotion()) return;

    if (typeof window.requestAnimationFrame === 'function') {
      rainbowFrameId = window.requestAnimationFrame(function (timestamp) {
        advanceRainbow(timestamp, root);
      });
    } else {
      // Fallback for older browsers without requestAnimationFrame.
      rainbowTimer = window.setInterval(function () {
        advanceRainbow(Date.now(), root);
      }, 16);
    }
  }

  function clearThemeTransition(root) {
    if (transitionTimer !== null) window.clearTimeout(transitionTimer);
    transitionTimer = null;
    root.classList.remove('theme-transitioning');
  }

  function animateTheme(root) {
    if (prefersReducedMotion()) return;
    root.classList.add('theme-transitioning');
    if (transitionTimer !== null) window.clearTimeout(transitionTimer);
    transitionTimer = window.setTimeout(function () {
      root.classList.remove('theme-transitioning');
      transitionTimer = null;
    }, 260);
  }

  function applyTheme(theme, animate) {
    var root = document.querySelector('.animfolio-container');
    if (!root) return;
    if (allowed.indexOf(theme) < 0) theme = 'midnight';

    // Rainbow is updated frame-by-frame by JavaScript, so do not layer a slower
    // CSS transition over it. Static palettes retain a quick crossfade.
    if (animate && theme !== 'rainbow') animateTheme(root);
    else clearThemeTransition(root);

    root.setAttribute('data-portfolio-theme', theme);
    if (theme === 'rainbow') startRainbow(root);
    else clearRainbow(root);

    document.querySelectorAll('[data-color-theme]').forEach(function (button) {
      var active = button.getAttribute('data-color-theme') === theme;
      button.setAttribute('aria-pressed', active ? 'true' : 'false');
    });
  }

  function init() {
    applyTheme(readTheme(), false);
    document.querySelectorAll('[data-color-theme]').forEach(function (button) {
      button.addEventListener('click', function () {
        var theme = button.getAttribute('data-color-theme');
        if (allowed.indexOf(theme) < 0) return;
        applyTheme(theme, true);
        try { localStorage.setItem(STORAGE_KEY, theme); } catch (_) {}
        var menu = button.closest('details');
        if (menu) menu.open = false;
      });
    });
    document.addEventListener('click', function (event) {
      document.querySelectorAll('.animfolio-color-picker[open]').forEach(function (menu) {
        if (!menu.contains(event.target)) menu.open = false;
      });
    });
    document.addEventListener('keydown', function (event) {
      if (event.key !== 'Escape') return;
      var openMenu = document.querySelector('.animfolio-color-picker[open]');
      if (openMenu) {
        openMenu.open = false;
        var trigger = openMenu.querySelector('summary');
        if (trigger) trigger.focus();
      }
    });
    window.addEventListener('storage', function (event) {
      if (event.key === STORAGE_KEY) applyTheme(readTheme(), true);
    });
  }

  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', init);
  else init();
})();
