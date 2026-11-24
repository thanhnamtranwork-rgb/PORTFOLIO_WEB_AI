(function () {
  'use strict';

  var apiBase = String(window.MOTELONA_API_BASE || '').trim().replace(/\/$/, '');
  var data = null;
  var statusNode = document.getElementById('ml-db-status');

  function isVietnamese() {
    return document.documentElement.lang === 'vi';
  }

  function set(selector, value) {
    var node = document.querySelector(selector);
    if (node && value !== null && value !== undefined && String(value).trim()) node.textContent = String(value);
  }

  function localized(record, field, fallback) {
    if (!record) return fallback || '';
    var vi = isVietnamese();
    var value = vi
      ? (record[field + 'Vi'] || record[field + 'En'] || record[field])
      : (record[field + 'En'] || record[field]);
    return value === null || value === undefined || !String(value).trim() ? (fallback || '') : String(value).trim();
  }

  function countLabel(count, singularEn, pluralEn, singularVi, pluralVi) {
    var value = Number(count) || 0;
    if (isVietnamese()) return value + ' ' + (value === 1 ? singularVi : pluralVi);
    return value + ' ' + (value === 1 ? singularEn : pluralEn);
  }

  function render(nextData) {
    if (nextData) data = nextData;
    if (!data) return;

    var profile = data.profile || {};
    var education = Array.isArray(data.education) ? data.education : [];
    var experience = Array.isArray(data.experience) ? data.experience : [];
    var skills = Array.isArray(data.skills) ? data.skills : [];
    var projects = Array.isArray(data.projects) ? data.projects : [];
    var knowledge = Array.isArray(data.knowledge) ? data.knowledge : [];
    var knowledgeCount = Number.isFinite(Number(data.knowledgeCount)) ? Number(data.knowledgeCount) : knowledge.length;
    var firstEducation = education[0] || {};
    var firstProject = projects[0] || {};

    set('[data-db-value="profile"]', profile.fullName || (isVietnamese() ? 'Chưa có dữ liệu' : 'No public profile'));
    set('[data-db-description="profile"]', localized(profile, 'headline', isVietnamese() ? 'Chưa có tiêu đề hồ sơ' : 'No profile headline supplied'));

    set('[data-db-value="education"]', countLabel(education.length, 'record', 'records', 'bản ghi', 'bản ghi'));
    var school = localized(firstEducation, 'schoolName');
    var major = localized(firstEducation, 'major');
    set('[data-db-description="education"]', [school, major].filter(Boolean).join(' · ') || (isVietnamese() ? 'Chưa có dữ liệu học vấn' : 'No education record supplied'));

    set('[data-db-value="experience"]', countLabel(experience.length, 'record', 'records', 'hoạt động', 'hoạt động'));
    set('[data-db-description="experience"]', experience.length
      ? (isVietnamese() ? 'Tự học phát triển Full-Stack · hoạt động cá nhân, không phải việc làm hưởng lương' : (experience[0].position || 'Self-directed learning') + ' · personal activity, not paid employment')
      : (isVietnamese() ? 'Chưa có hoạt động được công khai' : 'No public activity record supplied'));

    set('[data-db-value="skills"]', countLabel(skills.length, 'skill', 'skills', 'kỹ năng', 'kỹ năng'));
    var skillNames = skills.slice(0, 4).map(function (skill) { return localized(skill, 'skillName'); }).filter(Boolean);
    set('[data-db-description="skills"]', skillNames.join(' · ') || (isVietnamese() ? 'Chưa có kỹ năng được công khai' : 'No public skills supplied'));

    set('[data-db-value="projects"]', countLabel(projects.length, 'project', 'projects', 'dự án', 'dự án'));
    var projectTitle = localized(firstProject, 'title');
    var projectStatus = String(firstProject.projectStatus || '').toLowerCase();
    var statusLabel = projectStatus === 'in_progress'
      ? (isVietnamese() ? 'đang thực hiện' : 'in progress')
      : projectStatus.replace(/_/g, ' ');
    set('[data-db-description="projects"]', [projectTitle, statusLabel].filter(Boolean).join(' · ') || (isVietnamese() ? 'Chưa có dự án công khai' : 'No published project supplied'));

    set('[data-db-value="knowledge"]', countLabel(knowledgeCount, 'record', 'records', 'bản ghi', 'bản ghi'));
    set('[data-db-description="knowledge"]', knowledgeCount
      ? (isVietnamese() ? 'Mục tri thức được duyệt để hỗ trợ truy xuất.' : 'Reviewed knowledge entries are available for retrieval.')
      : (isVietnamese() ? 'FAQ và từ điển tích hợp xử lý câu hỏi thường gặp.' : 'Common questions use the built-in FAQ and glossary.'));
  }

  function refresh() {
    if (!apiBase || typeof window.fetch !== 'function') return;
    var controller = typeof AbortController === 'function' ? new AbortController() : null;
    var timer = controller ? window.setTimeout(function () { controller.abort(); }, 4500) : null;
    var options = controller ? { signal: controller.signal, headers: { Accept: 'application/json' } } : { headers: { Accept: 'application/json' } };
    window.fetch(apiBase + '/api/portfolio/status', options)
      .then(function (response) { if (!response.ok) throw new Error('Portfolio status unavailable'); return response.json(); })
      .then(function (status) {
        if (!status || status.connected !== true) throw new Error('Portfolio database is not connected');
        return window.fetch(apiBase + '/api/portfolio/public', options)
          .then(function (response) { if (!response.ok) throw new Error('Portfolio data unavailable'); return response.json(); });
      })
      .then(function (result) {
        render(result || {});
        if (statusNode) statusNode.textContent = isVietnamese()
          ? 'Đã kết nối Portfolio database · số liệu vừa được cập nhật.'
          : 'Portfolio database connected · counts refreshed just now.';
      })
      .catch(function () {
        if (statusNode) statusNode.textContent = isVietnamese()
          ? 'Đang hiển thị ảnh chụp dữ liệu gần nhất; chưa làm mới được từ Portfolio API.'
          : 'Showing the latest saved Portfolio snapshot; a live refresh was not available.';
      })
      .finally(function () { if (timer) window.clearTimeout(timer); });
  }

  window.addEventListener('animfolio:language-change', function () { render(); });
  refresh();
})();
