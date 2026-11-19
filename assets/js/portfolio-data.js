/* PortfolioAI_DB MySQL bridge. Keeps bundled HTML as a fallback when MySQL is offline. */
(function () {
  'use strict';

  var API_BASE = String(window.MOTELONA_API_BASE || 'http://127.0.0.1:8080').replace(/\/$/, '');
  var timeoutMs = 5000;
  var currentData = null;

  function language() {
    if (document.documentElement.lang === 'vi') return 'vi';
    try { return localStorage.getItem('animfolio-language') === 'vi' ? 'vi' : 'en'; }
    catch (_) { return 'en'; }
  }
  function localized(record, field, fallback) {
    if (!record) return fallback || '';
    var value = language() === 'vi'
      ? (record[field + 'Vi'] || record[field + 'En'] || record[field])
      : (record[field + 'En'] || record[field]);
    if (language() === 'vi' && !record[field + 'Vi'] && field === 'categoryName' && value) {
      var categories = {
        'Frontend': 'Giao diện người dùng', 'Backend': 'Phía máy chủ', 'Database': 'Cơ sở dữ liệu',
        'Technical': 'Kỹ thuật', 'Learning & Research': 'Học tập & nghiên cứu', 'Soft Skills': 'Kỹ năng mềm',
        'AI và Machine Learning': 'Trí tuệ nhân tạo và học máy', 'Tools và Công cụ': 'Công cụ'
      };
      value = categories[value] || value;
    }
    return value === null || value === undefined || !String(value).trim() ? (fallback || '') : String(value).trim();
  }
  function formatDate(value) {
    if (!value) return '';
    var raw = String(value).slice(0, 10);
    var date = new Date(raw + 'T00:00:00Z');
    if (Number.isNaN(date.getTime())) return raw;
    return new Intl.DateTimeFormat(language() === 'vi' ? 'vi-VN' : 'en-US', {
      month: 'short', year: 'numeric', timeZone: 'UTC'
    }).format(date);
  }

  function request(url) {
    var controller = typeof AbortController !== 'undefined' ? new AbortController() : null;
    var timer = controller ? setTimeout(function () { controller.abort(); }, timeoutMs) : null;
    return fetch(url, controller ? { signal: controller.signal, headers: { Accept: 'application/json' } } : { headers: { Accept: 'application/json' } })
      .then(function (response) {
        if (!response.ok) throw new Error('Portfolio API is not available.');
        return response.json();
      })
      .finally(function () { if (timer) clearTimeout(timer); });
  }

  function text(node, value) {
    if (node && value !== null && value !== undefined && String(value).trim()) node.textContent = String(value);
  }

  function safeWebUrl(value) {
    if (!value) return '';
    try {
      var url = new URL(String(value), window.location.href);
      return url.protocol === 'https:' || url.protocol === 'http:' ? url.href : '';
    } catch (_) { return ''; }
  }

  function addProject(project, grid) {
    var title = localized(project, 'title');
    var titleVariants = [project.title, project.titleVi, project.titleEn].filter(Boolean).map(function (value) { return String(value).trim().toLowerCase(); });
    if (!title) return;
    var existing = Array.prototype.some.call(grid.querySelectorAll('.animfolio-project__title'), function (node) {
      var current = node.textContent.trim().toLowerCase();
      return titleVariants.indexOf(current) !== -1;
    });
    if (existing) return;

    var article = document.createElement('article');
    article.className = 'animfolio-project animfolio-card animfolio-card--bordered animfolio-stagger__item';
    article.setAttribute('data-portfolio-db-project', String(project.projectId || project.title || title));
    var mark = document.createElement('div'); mark.className = 'motelona-project-mark'; mark.setAttribute('aria-hidden', 'true'); mark.textContent = 'P';
    var heading = document.createElement('h3'); heading.className = 'animfolio-project__title animfolio-card__title'; text(heading, title);
    var description = document.createElement('p'); description.className = 'animfolio-project__description';
    text(description, localized(project, 'shortDescription') || localized(project, 'fullDescription'));
    article.append(mark, heading);
    if (description.textContent) article.appendChild(description);

    var stack = document.createElement('div'); stack.className = 'animfolio-project__stack animfolio-card__tags';
    (Array.isArray(project.technologies) ? project.technologies : []).forEach(function (technology) {
      var tag = document.createElement('span'); tag.className = 'animfolio-card__tag';
      text(tag, technology.technologyName || technology.name || '');
      if (tag.textContent) stack.appendChild(tag);
    });
    if (stack.childElementCount) article.appendChild(stack);

    var actions = document.createElement('div'); actions.className = 'motelona-project-actions';
    [['demoUrl', 'Live demo'], ['sourceCodeUrl', 'Source code']].forEach(function (spec) {
      var href = safeWebUrl(project[spec[0]]);
      if (!href) return;
      var link = document.createElement('a'); link.className = 'animfolio-button animfolio-button--secondary';
      link.href = href; link.target = '_blank'; link.rel = 'noopener noreferrer'; text(link, spec[1]); actions.appendChild(link);
    });
    if (actions.childElementCount) article.appendChild(actions);
    grid.appendChild(article);
  }

  function addSkills(skills) {
    if (!Array.isArray(skills) || !skills.length) return;
    var grid = document.querySelector('#skills .animfolio-grid');
    if (!grid) return;
    var groups = new Map();
    skills.forEach(function (skill) {
      var categoryKey = String(skill.categoryName || 'Other').trim() || 'Other';
      if (!groups.has(categoryKey)) groups.set(categoryKey, { name: localized(skill, 'categoryName', categoryKey), items: [] });
      groups.get(categoryKey).items.push(skill);
    });
    groups.forEach(function (group, categoryName) {
      var category = Array.prototype.find.call(grid.querySelectorAll('.animfolio-skills__category'), function (node) {
        var heading = node.querySelector('.animfolio-skills__category-title');
        var current = heading ? heading.textContent.trim().toLowerCase() : '';
        return current === categoryName.toLowerCase() || current === group.name.toLowerCase();
      });
      if (!category) {
        category = document.createElement('div'); category.className = 'animfolio-skills__category animfolio-stagger__item';
        category.setAttribute('data-portfolio-db-category', categoryName);
        var heading = document.createElement('h3'); heading.className = 'animfolio-skills__category-title'; text(heading, group.name);
        var list = document.createElement('div'); list.className = 'animfolio-skills__list';
        category.append(heading, list); grid.appendChild(category);
      }
      var list = category.querySelector('.animfolio-skills__list');
      if (!list) return;
      var already = new Set(Array.prototype.map.call(list.querySelectorAll('.animfolio-skills__label span:first-child'), function (node) { return node.textContent.trim().toLowerCase(); }));
      group.items.forEach(function (skill) {
        var name = localized(skill, 'skillName');
        var variants = [skill.skillName, skill.skillNameVi, skill.skillNameEn].filter(Boolean).map(function (value) { return String(value).trim().toLowerCase(); });
        if (!name || variants.some(function (variant) { return already.has(variant); })) return;
        var item = document.createElement('div'); item.className = 'animfolio-skills__item';
        item.setAttribute('data-portfolio-db-skill', String(skill.skillId || name));
        var label = document.createElement('div'); label.className = 'animfolio-skills__label';
        var nameNode = document.createElement('span'); text(nameNode, name);
        var percent = skill.proficiencyLevel === null || skill.proficiencyLevel === undefined || skill.proficiencyLevel === ''
          ? NaN : Number(skill.proficiencyLevel);
        var valueNode = document.createElement('span'); valueNode.className = 'animfolio-skills__value';
        if (Number.isFinite(percent)) text(valueNode, Math.max(0, Math.min(100, Math.round(percent))) + '%');
        label.append(nameNode, valueNode);
        var bar = document.createElement('div'); bar.className = 'animfolio-skills__bar';
        var fill = document.createElement('div'); fill.className = 'animfolio-skills__bar-fill';
        if (Number.isFinite(percent)) fill.style.width = Math.max(0, Math.min(100, percent)) + '%';
        bar.appendChild(fill); item.append(label, bar); list.appendChild(item); variants.forEach(function (variant) { already.add(variant); });
      });
    });
  }

  function addEducation(items) {
    if (!Array.isArray(items) || !items.length) return;
    var timeline = document.querySelector('#education .animfolio-timeline');
    if (!timeline) return;
    items.forEach(function (education) {
      var school = String(education.schoolName || '').trim();
      if (!school) return;
      var duplicate = Array.prototype.some.call(timeline.querySelectorAll('.animfolio-timeline__company'), function (node) {
        return node.textContent.trim().toLowerCase() === school.toLowerCase();
      });
      if (duplicate) return;
      var item = document.createElement('div'); item.className = 'animfolio-timeline__item animfolio-stagger__item';
      item.setAttribute('data-portfolio-db-education', String(education.educationId || school));
      var dot = document.createElement('div'); dot.className = 'animfolio-timeline__dot';
      var card = document.createElement('div'); card.className = 'animfolio-timeline__content animfolio-card animfolio-card--bordered';
      var role = document.createElement('div'); role.className = 'animfolio-timeline__role'; text(role, localized(education, 'degree') || localized(education, 'major') || (language() === 'vi' ? 'Học vấn' : 'Education'));
      var company = document.createElement('div'); company.className = 'animfolio-timeline__company'; text(company, school);
      var date = document.createElement('div'); date.className = 'animfolio-timeline__date';
      var start = formatDate(education.startDate);
      var end = education.endDate ? formatDate(education.endDate) : (language() === 'vi' ? 'Hiện tại' : 'Present');
      text(date, [start, end].filter(Boolean).join(' – '));
      var description = document.createElement('div'); description.className = 'animfolio-timeline__description'; text(description, localized(education, 'description'));
      card.append(role, company); if (date.textContent) card.appendChild(date); if (description.textContent) card.appendChild(description);
      item.append(dot, card); timeline.appendChild(item);
    });
  }

  function hydrate(data) {
    currentData = data;
    var profile = data && data.profile ? data.profile : {};
    if (profile.fullName) document.querySelectorAll('.animfolio-hero__name').forEach(function (node) { text(node, profile.fullName); });
    /* Headline/AboutMe remain in the HTML so the existing EN/VI language switch can keep its translations. */
    if (profile.email) document.querySelectorAll('.animfolio-email-link, .animfolio-contact__email-link').forEach(function (node) {
      text(node, profile.email); node.href = 'mailto:' + profile.email;
    });
    if (profile.githubUrl) {
      var github = safeWebUrl(profile.githubUrl);
      if (github) document.querySelectorAll('a[href*="github.com"]').forEach(function (node) { node.href = github; });
    }
    if (profile.linkedInUrl) {
      var linkedIn = safeWebUrl(profile.linkedInUrl);
      if (linkedIn) document.querySelectorAll('a[href*="linkedin.com"]').forEach(function (node) { node.href = linkedIn; });
    }
    addEducation(data.education);
    addSkills(data.skills);
    var projectsGrid = document.querySelector('#projects .animfolio-grid');
    if (projectsGrid && Array.isArray(data.projects)) data.projects.forEach(function (project) { addProject(project, projectsGrid); });
  }

  function refreshLocalizedRecords() {
    if (!currentData) return;
    document.querySelectorAll('[data-portfolio-db-project], [data-portfolio-db-education], [data-portfolio-db-skill], [data-portfolio-db-category]').forEach(function (node) { node.remove(); });
    hydrate(currentData);
  }
  window.addEventListener('animfolio:language-change', refreshLocalizedRecords);
  window.addEventListener('storage', function (event) {
    if (event.key === 'animfolio-language') refreshLocalizedRecords();
  });

  request(API_BASE + '/api/portfolio/status')
    .then(function (status) {
      if (!status || status.connected !== true) return null;
      window.PORTFOLIO_DB_CONNECTED = true;
      return request(API_BASE + '/api/portfolio/public');
    })
    .then(function (data) { if (data) hydrate(data); })
    .catch(function () { window.PORTFOLIO_DB_CONNECTED = false; /* Static page remains the offline fallback. */ });
})();
