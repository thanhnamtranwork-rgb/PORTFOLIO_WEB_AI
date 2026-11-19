/**
 * Typed Text Module
 * Character-by-character typing effect with language-aware phrase lists.
 */
(function () {
  'use strict';

  var AF = window.animfolio;
  if (!AF) return;

  AF.registerModule('typed-text', {
    instances: [],

    init: function () {
      var self = this;
      var elements = document.querySelectorAll('.animfolio-hero__typed, [data-animfolio-typed]');
      elements.forEach(function (el) {
        var instance = self.createTyper(el);
        if (instance) self.instances.push(instance);
      });
    },

    createTyper: function (el) {
      function readPhrases(attribute) {
        try {
          var parsed = JSON.parse(el.getAttribute(attribute) || '[]');
          return Array.isArray(parsed) ? parsed.filter(function (item) { return typeof item === 'string'; }) : [];
        } catch (e) {
          return [];
        }
      }

      var englishPhrases = readPhrases('data-animfolio-typed');
      var vietnamesePhrases = readPhrases('data-animfolio-typed-vi');
      if (!englishPhrases.length) {
        var initial = (el.textContent || '').trim();
        if (initial) englishPhrases = [initial];
      }
      if (!englishPhrases.length) return null;
      if (!vietnamesePhrases.length) vietnamesePhrases = englishPhrases.slice();

      var typeSpeed = 80;
      var deleteSpeed = 40;
      var pauseTime = 2000;
      var charIndex = 0;
      var phraseIndex = 0;
      var isDeleting = false;
      var timeoutId = null;
      var disposed = false;
      var phrases = englishPhrases;
      var currentLanguage = document.documentElement.lang === 'vi' ? 'vi' : 'en';

      el.classList.add('animfolio-typed-cursor');

      function tick() {
        if (disposed || !phrases.length) return;
        var currentPhrase = phrases[phraseIndex] || '';
        var display;

        if (isDeleting) {
          charIndex = Math.max(0, charIndex - 1);
          display = currentPhrase.substring(0, charIndex);
        } else {
          charIndex = Math.min(currentPhrase.length, charIndex + 1);
          display = currentPhrase.substring(0, charIndex);
        }

        el.textContent = display;

        var delay;
        if (!isDeleting && charIndex >= currentPhrase.length) {
          delay = pauseTime;
          isDeleting = true;
        } else if (isDeleting && charIndex === 0) {
          isDeleting = false;
          phraseIndex = (phraseIndex + 1) % phrases.length;
          delay = 500;
        } else {
          delay = isDeleting ? deleteSpeed : typeSpeed;
        }

        timeoutId = window.setTimeout(tick, delay);
      }

      function setLanguage(language) {
        var nextLanguage = language === 'vi' ? 'vi' : 'en';
        if (nextLanguage === currentLanguage) return;
        currentLanguage = nextLanguage;
        if (timeoutId !== null) window.clearTimeout(timeoutId);
        phrases = nextLanguage === 'vi' ? vietnamesePhrases : englishPhrases;
        charIndex = 0;
        phraseIndex = 0;
        isDeleting = false;
        el.textContent = '';
        timeoutId = window.setTimeout(tick, 100);
      }

      function onLanguageChange(event) {
        var language = event && event.detail && event.detail.language;
        setLanguage(language || document.documentElement.lang);
      }
      window.addEventListener('animfolio:language-change', onLanguageChange);

      // Start in the currently selected language, including a saved VI preference.
      phrases = currentLanguage === 'vi' ? vietnamesePhrases : englishPhrases;
      tick();

      return {
        destroy: function () {
          disposed = true;
          if (timeoutId !== null) window.clearTimeout(timeoutId);
          window.removeEventListener('animfolio:language-change', onLanguageChange);
        },
      };
    },

    destroy: function () {
      this.instances.forEach(function (instance) {
        if (instance && instance.destroy) instance.destroy();
      });
      this.instances = [];
    },
  });
})();
