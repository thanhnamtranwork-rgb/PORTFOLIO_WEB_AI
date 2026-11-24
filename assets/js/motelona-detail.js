(function () {
  'use strict';

  var PALETTE_KEY = 'animfolio-theme';
  var MODE_KEY = 'animfolio_theme_dark_default';
  var LANGUAGE_KEY = 'animfolio-language';
  var palettes = {
    midnight: { accent: '#5665D8', hover: '#4F5DC7', secondary: '#A9B7FF', accentRgb: '86, 101, 216', secondaryRgb: '169, 183, 255', lightAccent: '#4957C8', lightHover: '#3F4BB4', lightSecondary: '#5368B4', lightAccentRgb: '73, 87, 200', lightSecondaryRgb: '83, 104, 180' },
    tree: { accent: '#247A53', hover: '#1D6948', secondary: '#91B96A', accentRgb: '36, 122, 83', secondaryRgb: '145, 185, 106', lightSecondary: '#4D6E30', lightSecondaryRgb: '77, 110, 48' },
    peach: { accent: '#B8426D', hover: '#A23860', secondary: '#F0A2B2', accentRgb: '184, 66, 109', secondaryRgb: '240, 162, 178', lightSecondary: '#A54764', lightSecondaryRgb: '165, 71, 100' },
    apricot: { accent: '#986005', hover: '#855000', secondary: '#EBC45D', accentRgb: '152, 96, 5', secondaryRgb: '235, 196, 93', lightSecondary: '#855000', lightSecondaryRgb: '133, 80, 0' }
  };
  var currentLanguage = 'en';
  var rainbowTimer = null;
  var rainbowFrameId = null;
  var rainbowHue = 0;
  var rainbowLastTime = null;
  var paletteTransitionTimer = null;
  var RAINBOW_DEGREES_PER_MS = 0.054;

  function safeGet(key, fallback) {
    try { return localStorage.getItem(key) || fallback; } catch (_) { return fallback; }
  }
  function safeSet(key, value) {
    try { localStorage.setItem(key, value); } catch (_) { /* Page remains usable without storage. */ }
  }

  function hslToRgb(h, s, l) {
    h = ((h % 360) + 360) % 360 / 360;
    s /= 100; l /= 100;
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

  function themeRoots() {
    return [document.documentElement, document.querySelector('.motelona-detail-root')].filter(Boolean);
  }

  function setRainbowFrame() {
    var secondaryHue = (rainbowHue + 58) % 360;
    var thirdHue = (rainbowHue + 132) % 360;
    var accent = 'hsl(' + rainbowHue.toFixed(2) + ' 74% 47%)';
    var hover = 'hsl(' + rainbowHue.toFixed(2) + ' 76% 39%)';
    var secondary = 'hsl(' + secondaryHue.toFixed(2) + ' 76% 64%)';
    var accentRgb = hslToRgb(rainbowHue, 74, 47);
    var secondaryRgb = hslToRgb(secondaryHue, 76, 64);
    themeRoots().forEach(function (root) {
      root.style.setProperty('--ml-accent', accent, 'important');
      root.style.setProperty('--ml-accent-hover', hover, 'important');
      root.style.setProperty('--ml-secondary', secondary, 'important');
      root.style.setProperty('--ml-accent-rgb', accentRgb, 'important');
      root.style.setProperty('--ml-secondary-rgb', secondaryRgb, 'important');
      root.style.setProperty('--ml-rainbow-gradient', 'linear-gradient(120deg, ' + accent + ', ' + secondary + ', hsl(' + thirdHue.toFixed(2) + ' 76% 58%))', 'important');
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

  function advanceRainbow(timestamp) {
    if (!document.documentElement.classList.contains('ml-rainbow-active')) return;
    var now = typeof timestamp === 'number' ? timestamp : Date.now();
    if (rainbowLastTime === null) rainbowLastTime = now;
    var elapsed = Math.min(Math.max(now - rainbowLastTime, 0), 100);
    rainbowLastTime = now;
    rainbowHue = (rainbowHue + elapsed * RAINBOW_DEGREES_PER_MS) % 360;
    setRainbowFrame();

    if (typeof window.requestAnimationFrame === 'function') {
      rainbowFrameId = window.requestAnimationFrame(advanceRainbow);
    }
  }

  function startRainbow() {
    cancelRainbowAnimation();
    rainbowHue = 0;
    var detailRoot = document.querySelector('.motelona-detail-root');
    document.documentElement.classList.add('ml-rainbow-active');
    if (detailRoot) detailRoot.classList.add('ml-rainbow-active');
    setRainbowFrame();
    if (window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches) return;

    if (typeof window.requestAnimationFrame === 'function') {
      rainbowFrameId = window.requestAnimationFrame(advanceRainbow);
    } else {
      rainbowTimer = window.setInterval(function () { advanceRainbow(Date.now()); }, 16);
    }
  }

  function stopRainbow() {
    cancelRainbowAnimation();
    document.documentElement.classList.remove('ml-rainbow-active');
    var detailRoot = document.querySelector('.motelona-detail-root');
    if (detailRoot) detailRoot.classList.remove('ml-rainbow-active');
  }

  function clearPaletteTransition() {
    if (paletteTransitionTimer) window.clearTimeout(paletteTransitionTimer);
    paletteTransitionTimer = null;
    document.documentElement.classList.remove('ml-theme-transitioning');
    var detailRoot = document.querySelector('.motelona-detail-root');
    if (detailRoot) detailRoot.classList.remove('ml-theme-transitioning');
  }

  function animatePalette() {
    if (window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches) return;
    var detailRoot = document.querySelector('.motelona-detail-root');
    document.documentElement.classList.add('ml-theme-transitioning');
    if (detailRoot) detailRoot.classList.add('ml-theme-transitioning');
    if (paletteTransitionTimer) window.clearTimeout(paletteTransitionTimer);
    paletteTransitionTimer = window.setTimeout(function () {
      document.documentElement.classList.remove('ml-theme-transitioning');
      if (detailRoot) detailRoot.classList.remove('ml-theme-transitioning');
      paletteTransitionTimer = null;
    }, 260);
  }

  function applyPalette(theme, animate) {
    if (theme !== 'rainbow' && !Object.prototype.hasOwnProperty.call(palettes, theme)) theme = 'midnight';
    if (animate && theme !== 'rainbow') animatePalette();
    else clearPaletteTransition();
    var root = document.querySelector('.motelona-detail-root');
    if (!root) return;
    root.setAttribute('data-portfolio-theme', theme);
    document.documentElement.setAttribute('data-portfolio-theme', theme);
    if (theme === 'rainbow') {
      startRainbow();
    } else {
      stopRainbow();
      var palette = palettes[theme];
      var lightMode = document.documentElement.getAttribute('data-ml-theme') === 'light';
      themeRoots().forEach(function (element) {
        element.style.setProperty('--ml-accent', lightMode && palette.lightAccent ? palette.lightAccent : palette.accent);
        element.style.setProperty('--ml-accent-hover', lightMode && palette.lightHover ? palette.lightHover : palette.hover);
        element.style.setProperty('--ml-secondary', lightMode && palette.lightSecondary ? palette.lightSecondary : palette.secondary);
        element.style.setProperty('--ml-accent-rgb', lightMode && palette.lightAccentRgb ? palette.lightAccentRgb : palette.accentRgb);
        element.style.setProperty('--ml-secondary-rgb', lightMode && palette.lightSecondaryRgb ? palette.lightSecondaryRgb : palette.secondaryRgb);
        element.style.removeProperty('--ml-rainbow-gradient');
      });
    }
    document.querySelectorAll('[data-theme-choice]').forEach(function (button) {
      button.setAttribute('aria-pressed', button.getAttribute('data-theme-choice') === theme ? 'true' : 'false');
    });
  }

  function applyMode(mode) {
    mode = mode === 'light' ? 'light' : 'dark';
    document.documentElement.setAttribute('data-ml-theme', mode);
    document.documentElement.style.colorScheme = mode;
    var selectedPalette = document.documentElement.getAttribute('data-portfolio-theme') || safeGet(PALETTE_KEY, 'midnight');
    if (selectedPalette !== 'rainbow') applyPalette(selectedPalette, false);
    var themeColor = document.querySelector('meta[name="theme-color"]');
    if (themeColor) themeColor.setAttribute('content', mode === 'light' ? '#F6F7FC' : '#0B1020');
    var button = document.querySelector('[data-ml-theme-toggle]');
    if (button) {
      var target = mode === 'dark' ? 'light' : 'dark';
      var label = currentLanguage === 'vi'
        ? (target === 'light' ? 'Chuyển sang chế độ sáng' : 'Chuyển sang chế độ tối')
        : (target === 'light' ? 'Switch to light mode' : 'Switch to dark mode');
      button.setAttribute('aria-label', label);
      button.setAttribute('title', label);
    }
  }

  function setLanguage(language) {
    var next = language === 'vi' ? 'vi' : 'en';
    currentLanguage = next;
    document.documentElement.lang = next;
    document.title = next === 'vi'
      ? 'MoteLona — Tổng quan dự án | Trần Thành Nam'
      : 'MoteLona — Project Overview | Trần Thành Nam';
    document.querySelectorAll('[data-en][data-vi]').forEach(function (node) {
      var value = node.getAttribute('data-' + next);
      if (node.tagName === 'META') node.setAttribute('content', value);
      else node.textContent = value;
    });
    document.querySelectorAll('[data-lang]').forEach(function (button) {
      var active = button.getAttribute('data-lang') === next;
      button.classList.toggle('is-active', active);
      button.setAttribute('aria-pressed', active ? 'true' : 'false');
    });
    var languageGroup = document.querySelector('.ml-language');
    if (languageGroup) languageGroup.setAttribute('aria-label', next === 'vi' ? 'Chọn ngôn ngữ' : 'Choose language');
    var sectionNav = document.querySelector('.ml-section-nav');
    if (sectionNav) sectionNav.setAttribute('aria-label', next === 'vi' ? 'Điều hướng nội dung trang' : 'On this page');
    var brandLink = document.querySelector('.ml-brand');
    if (brandLink) brandLink.setAttribute('aria-label', next === 'vi' ? 'Quay lại trang chủ của Trần Thành Nam' : 'Back to Trần Thành Nam home page');
    var snapshot = document.querySelector('.ml-snapshot');
    if (snapshot) snapshot.setAttribute('aria-label', next === 'vi' ? 'Tổng quan dự án' : 'Project snapshot');
    var openAssistant = document.querySelector('[data-motelona-open]');
    if (openAssistant) openAssistant.setAttribute('aria-label', next === 'vi' ? 'Mở trợ lý MoteLona' : 'Open MoteLona assistant');
    var colorSummary = document.querySelector('.ml-color-picker > summary');
    if (colorSummary) {
      var colorLabel = next === 'vi' ? 'Chọn bảng màu' : 'Choose color scheme';
      colorSummary.setAttribute('aria-label', colorLabel);
      colorSummary.setAttribute('title', colorLabel);
    }
    var colorMenu = document.querySelector('.ml-color-menu');
    if (colorMenu) colorMenu.setAttribute('aria-label', next === 'vi' ? 'Bảng màu' : 'Color schemes');
    var technologyTags = document.querySelector('.ml-tags');
    if (technologyTags) technologyTags.setAttribute('aria-label', next === 'vi' ? 'Công nghệ sử dụng' : 'Technology stack');
    var processingPipeline = document.querySelector('.ml-pipeline');
    if (processingPipeline) processingPipeline.setAttribute('aria-label', next === 'vi' ? 'Luồng xử lý MoteLona' : 'MoteLona processing pipeline');
    var glossarySearch = document.querySelector('#ml-glossary-search');
    if (glossarySearch) glossarySearch.setAttribute('placeholder', next === 'vi' ? 'Thử NLP, TF-IDF, tách token…' : 'Try NLP, TF-IDF, tokenization…');
    safeSet(LANGUAGE_KEY, next);
    applyMode(safeGet(MODE_KEY, 'dark'));
    window.dispatchEvent(new CustomEvent('animfolio:language-change', { detail: { language: next } }));
  }

  function init() {
    applyPalette(safeGet(PALETTE_KEY, 'midnight'), false);
    applyMode(safeGet(MODE_KEY, 'dark'));
    document.querySelectorAll('[data-theme-choice]').forEach(function (button) {
      button.addEventListener('click', function () {
        var theme = button.getAttribute('data-theme-choice');
        if (theme !== 'rainbow' && !Object.prototype.hasOwnProperty.call(palettes, theme)) return;
        applyPalette(theme, true);
        safeSet(PALETTE_KEY, theme);
        var menu = button.closest('details');
        if (menu) menu.open = false;
      });
    });
    var modeToggle = document.querySelector('[data-ml-theme-toggle]');
    if (modeToggle) {
      modeToggle.addEventListener('click', function () {
        var next = document.documentElement.getAttribute('data-ml-theme') === 'dark' ? 'light' : 'dark';
        safeSet(MODE_KEY, next);
        applyMode(next);
      });
    }
    document.querySelectorAll('[data-lang]').forEach(function (button) {
      button.addEventListener('click', function () { setLanguage(button.getAttribute('data-lang')); });
    });

    // Keep the section rail in sync with the content currently being read.
    var sectionNav = document.querySelector('.ml-section-nav');
    if (sectionNav && 'IntersectionObserver' in window) {
      var navLinks = Array.from(sectionNav.querySelectorAll('a[href^="#"]'));
      var sections = navLinks.map(function (link) {
        return document.getElementById(link.getAttribute('href').slice(1));
      }).filter(Boolean);
      var sectionObserver = new IntersectionObserver(function (entries) {
        var visible = entries.filter(function (entry) { return entry.isIntersecting; })
          .sort(function (a, b) { return a.boundingClientRect.top - b.boundingClientRect.top; });
        if (!visible.length) return;
        var activeId = visible[0].target.id;
        navLinks.forEach(function (link) {
          if (link.getAttribute('href') === '#' + activeId) link.setAttribute('aria-current', 'location');
          else link.removeAttribute('aria-current');
        });
      }, { rootMargin: '-18% 0px -68% 0px', threshold: 0 });
      sections.forEach(function (section) { sectionObserver.observe(section); });
    }

    setLanguage(safeGet(LANGUAGE_KEY, 'en') === 'vi' ? 'vi' : 'en');
    window.addEventListener('storage', function (event) {
      if (event.key === PALETTE_KEY) applyPalette(safeGet(PALETTE_KEY, 'midnight'), true);
      if (event.key === MODE_KEY) applyMode(safeGet(MODE_KEY, 'dark'));
      if (event.key === LANGUAGE_KEY) setLanguage(safeGet(LANGUAGE_KEY, 'en') === 'vi' ? 'vi' : 'en');
    });
  }

  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', init);
  else init();
})();
