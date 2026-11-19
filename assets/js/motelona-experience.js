(function () {
  'use strict';
  if (window.__motelonaGuideExperienceReady) return;
  window.__motelonaGuideExperienceReady = true;

  var root = document.querySelector('.motelona-detail-root');
  if (!root) return;
  var doc = document.documentElement;
  var storageKey = 'motelona-release-checklist-v1';
  var search = document.getElementById('ml-glossary-search');
  var clearSearch = document.getElementById('ml-glossary-clear');
  var count = document.getElementById('ml-glossary-count');
  var empty = document.getElementById('ml-glossary-empty');
  var cards = Array.prototype.slice.call(document.querySelectorAll('.ml-glossary-card'));
  var checklist = Array.prototype.slice.call(document.querySelectorAll('[data-release-task]'));
  var progressValue = document.getElementById('ml-check-progress-value');
  var progressBar = document.getElementById('ml-check-progress-bar');
  var resetChecklist = document.getElementById('ml-checklist-reset');

  function currentLanguage() {
    try { return localStorage.getItem('animfolio-language') === 'vi' ? 'vi' : 'en'; }
    catch (_) { return document.documentElement.lang === 'vi' ? 'vi' : 'en'; }
  }
  function label(en, vi) { return currentLanguage() === 'vi' ? vi : en; }
  function updateSearchLanguage() {
    if (!search) return;
    var vi = currentLanguage() === 'vi';
    search.placeholder = vi ? 'Thử NLP, TF-IDF, tokenization…' : 'Try NLP, TF-IDF, tokenization…';
    search.setAttribute('aria-label', vi ? 'Tìm thuật ngữ NLP' : 'Search NLP concepts');
  }

  /* Reading progress and a low-distraction return-to-top control. */
  var progress = document.createElement('div');
  progress.className = 'ml-reading-progress';
  progress.setAttribute('aria-hidden', 'true');
  progress.innerHTML = '<span></span>';
  document.body.appendChild(progress);
  var progressFill = progress.firstElementChild;
  var topButton = document.createElement('button');
  topButton.type = 'button';
  topButton.className = 'ml-back-top';
  topButton.textContent = '↑';
  topButton.setAttribute('aria-label', label('Back to top', 'Lên đầu trang'));
  topButton.title = topButton.getAttribute('aria-label');
  topButton.addEventListener('click', function () {
    window.scrollTo({ top: 0, behavior: window.matchMedia('(prefers-reduced-motion: reduce)').matches ? 'auto' : 'smooth' });
  });
  document.body.appendChild(topButton);

  function updateScroll() {
    var max = Math.max(1, doc.scrollHeight - window.innerHeight);
    var position = Math.max(0, Math.min(1, (window.scrollY || doc.scrollTop) / max));
    progressFill.style.transform = 'scaleX(' + position.toFixed(4) + ')';
    topButton.classList.toggle('is-visible', (window.scrollY || doc.scrollTop) > 520);
  }
  var scrollPending = false;
  window.addEventListener('scroll', function () {
    if (scrollPending) return;
    scrollPending = true;
    window.requestAnimationFrame(function () { updateScroll(); scrollPending = false; });
  }, { passive: true });
  window.addEventListener('resize', updateScroll, { passive: true });

  /* Add a reading estimate without making the page feel like a dashboard. */
  var nav = document.querySelector('.ml-section-nav');
  if (nav && !nav.querySelector('.ml-reading-meta')) {
    var articleText = root.innerText || root.textContent || '';
    var words = articleText.trim().split(/\s+/).filter(Boolean).length;
    var minutes = Math.max(4, Math.ceil(words / 210));
    var meta = document.createElement('span');
    meta.className = 'ml-reading-meta';
    meta.textContent = label(minutes + ' min read', 'Đọc khoảng ' + minutes + ' phút');
    nav.appendChild(meta);
  }

  /* Active section state for the technical guide's anchor navigation. */
  var sections = Array.prototype.slice.call(document.querySelectorAll('.ml-main section[id]'));
  var navLinks = Array.prototype.slice.call(document.querySelectorAll('.ml-section-nav a[href^="#"]'));
  if ('IntersectionObserver' in window && sections.length && navLinks.length) {
    var observer = new IntersectionObserver(function (entries) {
      entries.forEach(function (entry) {
        if (!entry.isIntersecting) return;
        var id = entry.target.id;
        navLinks.forEach(function (link) {
          if (link.getAttribute('href') === '#' + id) link.setAttribute('aria-current', 'location');
          else link.removeAttribute('aria-current');
        });
      });
    }, { rootMargin: '-20% 0px -68% 0px', threshold: 0 });
    sections.forEach(function (section) { observer.observe(section); });
  }

  /* Search the bilingual glossary after the language switch has updated its text. */
  function runGlossarySearch() {
    if (!search) return;
    var query = search.value.trim().toLocaleLowerCase();
    var visible = 0;
    cards.forEach(function (card) {
      var match = !query || (card.textContent || '').toLocaleLowerCase().indexOf(query) !== -1;
      card.hidden = !match;
      if (match) visible++;
    });
    if (count) count.textContent = currentLanguage() === 'vi'
      ? visible + '/' + cards.length + ' thuật ngữ phù hợp'
      : visible + '/' + cards.length + ' matching concepts';
    if (empty) empty.hidden = visible !== 0;
  }
  if (search) search.addEventListener('input', runGlossarySearch);
  updateSearchLanguage();
  if (clearSearch) clearSearch.addEventListener('click', function () { search.value = ''; runGlossarySearch(); search.focus(); });

  /* Persist the release checklist locally; checked items mean verified, not merely planned. */
  function readChecklist() {
    try { var saved = JSON.parse(localStorage.getItem(storageKey) || '{}'); return saved && typeof saved === 'object' ? saved : {}; }
    catch (_) { return {}; }
  }
  function writeChecklist() {
    var state = {};
    checklist.forEach(function (input) { state[input.dataset.releaseTask] = input.checked; });
    try { localStorage.setItem(storageKey, JSON.stringify(state)); } catch (_) { /* Keep the checklist usable for this session. */ }
  }
  function updateChecklist() {
    var completed = checklist.filter(function (input) { return input.checked; }).length;
    if (progressValue) progressValue.textContent = completed + '/' + checklist.length;
    if (progressBar) progressBar.style.width = (checklist.length ? completed / checklist.length * 100 : 0) + '%';
    writeChecklist();
  }
  var savedChecklist = readChecklist();
  checklist.forEach(function (input) {
    input.checked = savedChecklist[input.dataset.releaseTask] === true;
    input.addEventListener('change', updateChecklist);
  });
  if (resetChecklist) resetChecklist.addEventListener('click', function () {
    checklist.forEach(function (input) { input.checked = false; });
    updateChecklist();
  });
  updateChecklist();

  /* Add native copy actions to the documented API routes. */
  function copyText(text) {
    if (navigator.clipboard && window.isSecureContext) return navigator.clipboard.writeText(text);
    return new Promise(function (resolve, reject) {
      var field = document.createElement('textarea');
      field.value = text; field.setAttribute('readonly', '');
      field.style.position = 'fixed'; field.style.opacity = '0'; field.style.pointerEvents = 'none';
      document.body.appendChild(field); field.select();
      var ok = false;
      try { ok = document.execCommand('copy'); } catch (_) { ok = false; }
      field.remove();
      if (ok) resolve(); else reject(new Error('Copy unavailable'));
    });
  }
  document.querySelectorAll('.ml-endpoints code').forEach(function (code) {
    if (code.parentElement.querySelector('.ml-copy-endpoint')) return;
    var button = document.createElement('button');
    button.type = 'button'; button.className = 'ml-copy-endpoint';
    button.textContent = label('Copy', 'Sao chép');
    button.setAttribute('aria-label', label('Copy endpoint', 'Sao chép endpoint'));
    button.addEventListener('click', function () {
      var original = label('Copy', 'Sao chép');
      copyText(code.textContent.trim()).then(function () {
        button.textContent = label('Copied', 'Đã chép');
        window.setTimeout(function () { button.textContent = original; }, 1400);
      }).catch(function () {
        button.textContent = label('Select to copy', 'Hãy chọn để sao chép');
        window.setTimeout(function () { button.textContent = original; }, 1800);
      });
    });
    code.insertAdjacentElement('afterend', button);
  });

  window.addEventListener('animfolio:language-change', function () {
    topButton.setAttribute('aria-label', label('Back to top', 'Lên đầu trang'));
    topButton.title = topButton.getAttribute('aria-label');
    var meta = document.querySelector('.ml-reading-meta');
    if (meta) {
      var words = (root.innerText || root.textContent || '').trim().split(/\s+/).filter(Boolean).length;
      var minutes = Math.max(4, Math.ceil(words / 210));
      meta.textContent = label(minutes + ' min read', 'Đọc khoảng ' + minutes + ' phút');
    }
    document.querySelectorAll('.ml-copy-endpoint').forEach(function (button) {
      button.textContent = label('Copy', 'Sao chép');
      button.setAttribute('aria-label', label('Copy endpoint', 'Sao chép endpoint'));
    });
    updateSearchLanguage();
    runGlossarySearch();
  });
  window.addEventListener('storage', function (event) { if (event.key === 'animfolio-language') { updateSearchLanguage(); runGlossarySearch(); } });
  updateScroll();
  runGlossarySearch();
})();
