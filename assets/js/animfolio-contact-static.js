/** Contact form integration: submits to the configured backend and can send an email notification. */
(function () {
  'use strict';
  var EMAIL = (window.ANIMFOLIO_CONTACT_EMAIL || '').trim();
  var API_BASE = String(window.MOTELONA_API_BASE || 'http://127.0.0.1:8080').replace(/\/$/, '');

  function language() {
    try { return localStorage.getItem('animfolio-language') === 'vi' ? 'vi' : 'en'; }
    catch (_) { return document.documentElement.lang === 'vi' ? 'vi' : 'en'; }
  }
  function tr(en, vi) { return language() === 'vi' ? vi : en; }
  function validationMessage(key) {
    var messages = {
      name: ['Name is required.', 'Vui lòng nhập họ tên.'],
      email: ['Email is required.', 'Vui lòng nhập email.'],
      email_invalid: ['Please enter a valid email address.', 'Vui lòng nhập địa chỉ email hợp lệ.'],
      message: ['Message is required.', 'Vui lòng nhập nội dung.']
    };
    var pair = messages[key];
    return pair ? pair[language() === 'vi' ? 1 : 0] : '';
  }
  function renderSuccess(success) {
    var messages = {
      both: ['Thank you! Your message was saved and an email notification was sent.', 'Cảm ơn bạn! Tin nhắn đã được lưu và email thông báo đã được gửi.'],
      database: ['Your message was saved to the Portfolio database. Email notification was unavailable.', 'Tin nhắn đã được lưu vào cơ sở dữ liệu Portfolio. Hiện chưa gửi được email thông báo.'],
      email: ['Thank you! Your message was sent by email.', 'Cảm ơn bạn! Tin nhắn đã được gửi qua email.']
    };
    var pair = messages[success.dataset.contactOutcome];
    if (pair) success.textContent = pair[language() === 'vi' ? 1 : 0];
  }
  function renderGlobalError(element) {
    var parts = [];
    if (element.dataset.databaseUnavailable === 'true') {
      parts.push(tr('The Portfolio database is not reachable.', 'Không kết nối được cơ sở dữ liệu Portfolio.'));
    }
    if (element.dataset.emailConfigured === 'true') {
      parts.push(tr('The message could not be sent. Please try again or email ', 'Không gửi được tin nhắn. Hãy thử lại hoặc gửi email đến ') + EMAIL + tr(' directly.', ' trực tiếp.'));
    } else {
      parts.push(tr('The contact email is not configured. Please contact the site owner directly.', 'Website chưa cấu hình email liên hệ. Vui lòng liên hệ trực tiếp với chủ website.'));
    }
    element.textContent = parts.join(' ');
  }
  function refreshFeedbackLanguage() {
    document.querySelectorAll('.animfolio-form__error[data-contact-error]').forEach(function (node) {
      node.textContent = validationMessage(node.dataset.contactError);
    });
    document.querySelectorAll('.animfolio-form__success[data-contact-outcome]').forEach(renderSuccess);
    document.querySelectorAll('.animfolio-form__global-error[data-database-unavailable], .animfolio-form__global-error[data-email-configured]').forEach(renderGlobalError);
  }
  window.addEventListener('animfolio:language-change', refreshFeedbackLanguage);
  window.addEventListener('storage', function (event) {
    if (event.key === 'animfolio-language') refreshFeedbackLanguage();
  });

  document.querySelectorAll('.animfolio-form').forEach(function (form) {
    form.addEventListener('input', function (event) {
      var field = event.target;
      if (!field || !field.name) return;
      var group = field.closest('.animfolio-form__group');
      var message = group && group.querySelector('.animfolio-form__error[data-contact-error]');
      if (!message) return;
      var key = fieldErrorKey(form, field.name);
      if (key) {
        message.dataset.contactError = key;
        message.textContent = validationMessage(key);
      } else {
        delete message.dataset.contactError;
        message.textContent = '';
        group.classList.remove('animfolio-form__group--error');
      }
    });

    form.addEventListener('submit', async function (event) {
      event.preventDefault();
      var errors = validate(form);
      if (errors.length) { showErrors(form, errors); return; }
      clearValidationErrors(form);
      var honeypot = form.querySelector('[name="animfolio_hp"]');
      if (honeypot && honeypot.value) return;

      var name = value(form, 'name');
      var email = value(form, 'email');
      var message = value(form, 'message');
      var button = form.querySelector('.animfolio-form__submit');
      if (button) {
        button.classList.add('animfolio-form__submit--loading');
        button.disabled = true;
        button.setAttribute('aria-busy', 'true');
      }

      var dbSaved = false;
      var emailSent = false;
      var databaseUnavailable = false;
      try {
        var status = await fetchJson(API_BASE + '/api/portfolio/status', { method: 'GET' }, 5000);
        if (status && status.connected === true) {
          var saved = await fetchJson(API_BASE + '/api/portfolio/contact', {
            method: 'POST',
            body: JSON.stringify({
              name: name,
              email: email,
              subject: tr('Portfolio contact message', 'Tin nhắn liên hệ Portfolio'),
              message: message,
              honeypot: ''
            })
          }, 10000);
          dbSaved = saved && saved.received === true;
        }
      } catch (_) {
        databaseUnavailable = true;
      }

      if (EMAIL && EMAIL.indexOf('@') > 0) {
        try {
          var body = new FormData();
          body.append('name', name);
          body.append('email', email);
          body.append('message', message);
          body.append('_subject', tr('New portfolio message from ', 'Tin nhắn mới từ ') + (name || tr('your site', 'website của bạn')));
          body.append('_captcha', 'false');
          body.append('_template', 'table');
          var response = await fetch('https://formsubmit.co/ajax/' + encodeURIComponent(EMAIL), {
            method: 'POST', headers: { Accept: 'application/json' }, body: body
          });
          var result = await response.json().catch(function () { return null; });
          emailSent = response.ok && result && (result.success === true || result.success === 'true');
        } catch (_) { emailSent = false; }
      }

      if (button) {
        button.classList.remove('animfolio-form__submit--loading');
        button.disabled = false;
        button.removeAttribute('aria-busy');
      }
      if (dbSaved || emailSent) {
        var success = form.querySelector('.animfolio-form__success');
        if (success) {
          success.dataset.contactOutcome = dbSaved && emailSent ? 'both' : dbSaved ? 'database' : 'email';
          renderSuccess(success);
          success.classList.add('animfolio-form__success--visible');
          setTimeout(function () { success.classList.remove('animfolio-form__success--visible'); }, 8000);
        }
        form.reset();
      } else {
        showFormError(form, databaseUnavailable, Boolean(EMAIL && EMAIL.indexOf('@') > 0));
      }
    });
  });

  function value(form, name) {
    var input = form.querySelector('[name="' + name + '"]');
    return input ? input.value.trim() : '';
  }
  function fieldErrorKey(form, name) {
    if (name === 'name') return value(form, 'name') ? '' : 'name';
    if (name === 'email') {
      var email = value(form, 'email');
      if (!email) return 'email';
      return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email) ? '' : 'email_invalid';
    }
    if (name === 'message') return value(form, 'message') ? '' : 'message';
    return '';
  }
  function fetchJson(url, options, timeout) {
    var controller = new AbortController();
    var timer = setTimeout(function () { controller.abort(); }, timeout || 8000);
    return fetch(url, Object.assign({}, options || {}, {
      signal: controller.signal,
      headers: Object.assign({ Accept: 'application/json', 'Content-Type': 'application/json' }, (options || {}).headers || {})
    })).then(function (response) {
      if (!response.ok) throw new Error('Request failed.');
      return response.json();
    }).finally(function () { clearTimeout(timer); });
  }
  function validate(form) {
    var errors = [];
    ['name', 'email', 'message'].forEach(function (name) {
      var key = fieldErrorKey(form, name);
      if (key) errors.push({ field: name, key: key });
    });
    return errors;
  }
  function clearValidationErrors(form) {
    form.querySelectorAll('.animfolio-form__group--error').forEach(function (group) {
      group.classList.remove('animfolio-form__group--error');
    });
    form.querySelectorAll('.animfolio-form__error[data-contact-error]').forEach(function (node) {
      delete node.dataset.contactError;
      node.textContent = '';
    });
  }
  function showErrors(form, errors) {
    clearValidationErrors(form);
    errors.forEach(function (error) {
      var field = form.querySelector('[name="' + error.field + '"]');
      var group = field && field.closest('.animfolio-form__group');
      if (!group) return;
      group.classList.add('animfolio-form__group--error');
      var message = group.querySelector('.animfolio-form__error');
      if (message) {
        message.dataset.contactError = error.key;
        message.textContent = validationMessage(error.key);
      }
    });
    var first = form.querySelector('.animfolio-form__group--error input, .animfolio-form__group--error textarea');
    if (first) first.focus();
  }
  function showFormError(form, databaseUnavailable, emailConfigured) {
    var element = form.querySelector('.animfolio-form__global-error');
    if (!element) {
      element = document.createElement('div');
      element.className = 'animfolio-form__global-error';
      element.setAttribute('role', 'alert');
      element.setAttribute('aria-live', 'assertive');
      element.style.cssText = 'padding:12px;margin-bottom:16px;border-radius:6px;background:rgba(239,68,68,0.1);color:var(--animfolio-error);border:1px solid rgba(239,68,68,0.2);';
      form.prepend(element);
    }
    element.dataset.databaseUnavailable = databaseUnavailable ? 'true' : 'false';
    element.dataset.emailConfigured = emailConfigured ? 'true' : 'false';
    renderGlobalError(element);
  }
})();
