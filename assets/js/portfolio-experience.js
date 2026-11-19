(function () {
  'use strict';
  if (window.__portfolioExperienceReady) return;
  window.__portfolioExperienceReady = true;

  var doc = document.documentElement;
  var progress = document.createElement('div');
  progress.className = 'portfolio-reading-progress';
  progress.setAttribute('aria-hidden', 'true');
  progress.innerHTML = '<span></span>';
  document.body.appendChild(progress);
  var progressFill = progress.firstElementChild;

  var topButton = document.createElement('button');
  topButton.type = 'button';
  topButton.className = 'portfolio-back-top';
  topButton.setAttribute('aria-label', 'Back to top');
  topButton.setAttribute('title', 'Back to top');
  topButton.textContent = '↑';
  topButton.addEventListener('click', function () {
    window.scrollTo({ top: 0, behavior: window.matchMedia('(prefers-reduced-motion: reduce)').matches ? 'auto' : 'smooth' });
  });
  document.body.appendChild(topButton);

  function language() {
    try { return localStorage.getItem('animfolio-language') === 'vi' ? 'vi' : 'en'; }
    catch (_) { return document.documentElement.lang === 'vi' ? 'vi' : 'en'; }
  }
  function updateLabels() {
    var vi = language() === 'vi';
    topButton.setAttribute('aria-label', vi ? 'Lên đầu trang' : 'Back to top');
    topButton.title = vi ? 'Lên đầu trang' : 'Back to top';
  }
  function updateScroll() {
    var max = Math.max(1, doc.scrollHeight - window.innerHeight);
    var current = Math.max(0, Math.min(1, (window.scrollY || doc.scrollTop) / max));
    progressFill.style.transform = 'scaleX(' + current.toFixed(4) + ')';
    topButton.classList.toggle('is-visible', (window.scrollY || doc.scrollTop) > 640);
  }
  var scrollQueued = false;
  window.addEventListener('scroll', function () {
    if (scrollQueued) return;
    scrollQueued = true;
    window.requestAnimationFrame(function () { updateScroll(); scrollQueued = false; });
  }, { passive: true });
  window.addEventListener('resize', updateScroll, { passive: true });
  window.addEventListener('animfolio:language-change', updateLabels);
  window.addEventListener('storage', function (event) { if (event.key === 'animfolio-language') updateLabels(); });

  document.querySelectorAll('.animfolio-nav__link[href^="#"]').forEach(function (link) {
    link.addEventListener('click', function () {
      var toggle = document.querySelector('.animfolio-nav__toggle');
      var list = document.querySelector('.animfolio-nav__list');
      if (toggle && list && window.matchMedia('(max-width: 900px)').matches) {
        toggle.setAttribute('aria-expanded', 'false');
        list.classList.remove('is-open', 'animfolio-nav__list--open');
      }
    });
  });

  updateLabels();
  updateScroll();
})();
