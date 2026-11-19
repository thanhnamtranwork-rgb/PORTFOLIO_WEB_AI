## 2026-11-13 — Mobile overflow correction and maintenance audit
- Corrected two confirmed horizontal page-overflow cases on the MoteLona detail page: (1) at widths ≤560px, the oversized decorative `.ml-hero::before` halo extended beyond the viewport; the existing hero now clips only horizontal overflow while keeping vertical overflow visible; (2) at widths 561–760px, the endpoint list's three-column minimum widths exceeded the two-column card width, so the existing compact endpoint layout now applies throughout the tablet breakpoint. Text, endpoints, and copy controls remain present. No section or feature was removed.
- Confirmed `assets/css/typography-refinement.css` is loaded last by both `index.html` and `Project/1/motelona.html`; synchronized the existing file tree, CSS cascade, and dependency documentation. The existing ZIP contains 111 files.
- Restored the Unix executable permission on the existing `Project/1/motelona-ai/backend/mvnw` wrapper. Its contents, Maven version pin, and checksum validation remain unchanged.
- Browser smoke checks passed for Portfolio launcher/open/settings/close, header drag, pointer resize, keyboard resize, detail-page EN/VI and Rainbow palette, and responsive viewport widths. The mobile detail-page overflow regression was reproduced before the CSS correction and was absent after it.
- JavaScript syntax, HTML IDs/references, CSS parsing/local URLs, and the two matching 17-table SQL schemas passed static checks. Maven tests and live MySQL/API integration were skipped because Maven could not be downloaded in this environment (DNS/network unavailable) and no MySQL server is installed.

# Portfolio + MoteLona — Upgrade notes

## 2026-11-18 — Local Java assistant, safe web lookup, and synchronized handoff

- Added opt-in public web search through DuckDuckGo's non-JavaScript HTML results page. No AI/search API key is required; search terms are sent only when a visitor enables Search web or explicitly asks for an Internet search. Results show source links and snippets, and the assistant explains that snippets are not independent verification.
- Added explicit allow-listed navigation between the portfolio and MoteLona detail page, with a requested phrase highlighted in yellow after scrolling to the matching section. Unknown destinations do not trigger navigation.
- Improved local Java answers so missing evidence is disclosed instead of guessed; retained local glossary explanations and MySQL-backed services.
- Updated `motelona.html`, bilingual chat labels, the home page project card, endpoint/architecture notes, and all relevant `main_document` references together.
- Added root `README.md`, `.gitignore`, and `.github/copilot-instructions.md` for a simple VS Code/Copilot/GitHub handoff.
- Verification: `mvnw clean verify` passed (36 tests, no failures); all 24 JavaScript files passed `node --check`; both HTML pages passed duplicate-ID and local-link/anchor checks; all 21 stylesheets have valid local `url()` assets; the two active MySQL schemas contain the same 17 tables. Live MySQL/API and browser interaction checks were not run because no MySQL instance or browser automation environment is available here.

## 2026-11-18 — Full website language pass

- Expanded the homepage language switch to cover later-added text and accessibility attributes, and added bilingual page metadata. Corrected the language code to `VI`, translated the project-card title and clearer “View project details” action. The MoteLona detail page now also localizes its technology/pipeline labels and glossary search placeholder.
- Localized contact-form validation, database/email errors, success messages, mail subjects, and the image lightbox controls. These messages now update when the visitor changes language while a state is visible.
- Localized the Java/MySQL-powered portfolio records with optional Vietnamese education, skill, and project fields; dates and category labels follow the selected language. Existing records without translated values keep their original text.
- Added `V002_BilingualContent_MySQL.sql` for existing database installations and synchronized the schema, API field selection, frontend rendering, README, setup guide, architecture map, dependency map, and QA checklist.
- Verification: `mvnw clean verify` passed (36 tests, no failures); all project JavaScript passed `node --check`; the MoteLona dictionary has 85 matching EN/VI keys and all home-page MoteLona translation references resolve; both active MySQL schemas contain the same 17 tables and all eight optional Vietnamese content fields; all 3 home and 5 detail-page metadata entries have complete EN/VI values; both HTML pages have unique IDs and valid local links/assets. A live MySQL migration and browser visual/interaction pass were unavailable in this environment, so those remain for a local run.

## 2026-11-18 — Java/Spring Boot/MySQL study and presentation guide

- Added `main_document/08_HUONG_DAN_HOC_VA_BAO_VE_JAVA.md`, a code-first learning guide with architecture map, Java concepts, local NLP flow, REST/DI, MySQL/JDBC, consent/search limits, a five-minute presentation outline and likely defense questions.
- Linked the guide from `README.md`, the AI master guide and the file map so it is easy to find when opening the ZIP in VS Code.
- The guide was checked against the current Java source, endpoint mappings, `pom.xml`, `application.yml`, SQL schemas and existing test names; it distinguishes implemented behavior from future/unsupported claims.

## 2026-11-18 — Portfolio sections adjusted to 75% on desktop

- Changed the active desktop scale for Portfolio sections after `#hero` from `zoom: 0.67` to `zoom: 0.75` in `assets/css/portfolio-visual-scale.css`.
- Adjusted the section canvas to `133.333333vw` and the anchor offset to `101.333333px` to retain full-width backgrounds and the existing sticky-navigation spacing at the new scale.
- Kept `#hero`, navigation, floating assistant, detail page and responsive/touch breakpoints outside this scale change; updated current setup and maintenance documentation to 75%.
- Static CSS and project-reference checks are recorded for this update; no real-browser visual pass was available in this environment.

## 2026-11-18 — MySQL download and connection guide

- Added `main_document/09_HUONG_DAN_TAI_MYSQL_VA_KET_NOI.md` with official MySQL Server 8.4 LTS and Workbench download links, Windows MSI/Configurator steps, local port setup, database/user creation, `.env` configuration, backend verification and troubleshooting.
- Updated the existing setup guide and README to point to the new walk-through; added the `REFERENCES` database privilege required for the schema's foreign keys.
- Verified the download links and Windows install/Configurator details against MySQL's official download and reference manual pages.

## What changed

- Added a restrained visual refinement layer for the portfolio and MoteLona guide without replacing the existing theme system or rewriting the core page structure.
- Added reading progress, a compact return-to-top control, visible keyboard focus styles and active-section navigation states.
- Removed the duplicate MoteLona open button from the Featured Projects card. The floating MoteLona launcher on the portfolio home page remains; the project card now has one clear route to the technical overview.
- Added a searchable bilingual NLP glossary to `Project/1/motelona.html`.
- Added a bilingual FAQ that accurately explains the difference between the current local NLP assistant and a general-purpose generative LLM.
- Converted the deployment checklist into an interactive checklist that stores its checked state in this browser. It does not mark tasks complete automatically.
- Added copy buttons beside the documented REST API endpoints.
- Corrected the detail-page launcher scope after the chat mount point is moved to `body`, so the detail page uses its explicit hero CTA instead of showing a second floating launcher.
- Corrected default API endpoint selection: localhost/file-based development uses `http://127.0.0.1:8080`; a public website does not silently try the visitor's own loopback address.
- Added Open Graph/Twitter metadata, a detail-page favicon and a small amount of accessibility/responsive hardening.

## MoteLona backend configuration

The chatbot's browser interface is HTML/CSS/JavaScript, but live answers still require the Java backend. For local development, open the project on localhost and start the Spring Boot backend on port `8080`.

For a public deployment, set the backend URL before loading `motelona-config.js`:

```html
<script>
  window.MOTELONA_API_BASE = "https://your-api-domain.example";
</script>
<script src="./Project/1/motelona-ai/frontend/motelona-config.js"></script>
```

Replace the example URL with your actual HTTPS backend. Configure allowed CORS origins and server-side environment variables on the backend. Never place database passwords, provider keys or other secrets in this frontend file.

## Scope and validation

The existing assistant is a self-built Java NLP/retrieval application. This upgrade does not relabel it as ChatGPT or an external generative model, and it does not invent a production backend URL. The static website can be hosted separately, but the Java service and database must also be deployed and configured for live chat, history or memory.

## Final interaction and accessibility pass

- The portfolio theme button now announces the correct next action in English or Vietnamese, exposes its pressed state, and updates the browser theme color when switching between light and dark.
- The active portfolio section is now exposed with `aria-current="location"`, not only a visual CSS class.
- The portfolio color menu now closes when the user clicks outside it or presses Escape; Escape also returns focus to its trigger.
- MoteLona's technical guide now translates key screen-reader labels for the language switch, section navigation, portfolio return link, project snapshot and assistant action.
- Increased the minimum touch area for the guide's section rail, language controls, palette options, glossary actions, checklist and FAQ summaries; the mobile section rail uses horizontal scroll snapping.
- Glossary result and no-result feedback use polite status semantics for assistive technology.
- Replaced the inherited generic favicon on the portfolio homepage with the MoteLona mark so the browser tab matches the site identity.

## Final static checks

- JavaScript files pass `node --check`.
- Stylesheets parse without CSS syntax errors.
- The two main HTML documents have no duplicate IDs or missing local file references.
- Visual review was performed at desktop and 390px mobile widths. A full browser automation pass was not available in this environment, so live API availability and deployed-host behavior still need to be verified after deployment.


## 2026-11-12 — Navigation labels and animated color themes
- Corrected the project-detail return links: the top link now returns to the homepage in both languages; the footer returns to Featured Projects / Dự án tiêu biểu.
- Removed the duplicated short “Nam” brand token so the MoteLona header displays the name once.
- Reworded the hero badge to “ABOUT ME” / “GIỚI THIỆU BẢN THÂN”.
- Added the Rainbow Spectrum / Cầu vồng chuyển sắc palette to both pages.
- Regular palette changes animate over approximately 420ms; rainbow colors cycle smoothly while the theme is active. The system respects reduced-motion preferences.

## 2026-11-12 — Rainbow palette and faster color controls
- Reworked the homepage Rainbow Spectrum from a stepped 90ms interval into a frame-synchronized hue animation. The hue now advances continuously at a steady rate, with long frame gaps capped to prevent sudden jumps when returning to a background tab.
- Fixed the theme-variable priority conflict: base theme tokens declared with `!important` now receive matching inline priority while Rainbow Spectrum is active, so the accent, secondary color, hover shades, subtle backgrounds, gradients, focus ring and text accent all change together.
- Removed the extra 150ms CSS color transition layered over every rainbow frame. This avoids color lag and lets the frame-by-frame palette remain visually continuous.
- Kept ordinary palette changes on a shorter 260ms crossfade and reduced the color-picker hover response to 90–100ms, with a small immediate pointer lift.
- Applied the same continuous rainbow animation and faster color-picker feedback to the MoteLona project-detail page. Reduced-motion preferences remain respected on both pages.

## 2026-11-12 — 75% detail-page scale and draggable MoteLona window
- Added a desktop-only 75% visual scale to `Project/1/motelona.html`. The main `index.html` portfolio page is deliberately unchanged; smaller/touch layouts keep their normal responsive sizing.
- Kept the detail page filling the viewport while scaled, and adjusted the sticky top navigation's horizontal alignment for the wider scaled layout.
- Made the open MoteLona chat panel draggable from its header using pointer events (mouse, pen and touch). The settings and close buttons remain clickable, the panel is constrained to the visible viewport, and its position is retained while the page remains open.


## 2026-11-12 — 67% visual scale at 100% browser zoom
- Added a desktop-only 67% visual scale to the About, Education, Skills, Projects and Contact sections on `index.html`, while preserving the Home hero, navigation and floating MoteLona assistant at their existing visual sizes. The browser zoom itself remains at 100%.
- Expanded each scaled section's layout canvas so section backgrounds remain full width and centered content follows the same proportions as the 67% browser-zoom reference. Anchor spacing is adjusted for the unchanged sticky navigation.
- Updated the MoteLona project-detail page from 75% to 67% visual scale and recalculated its page width and top-navigation alignment. Touch/mobile layouts continue to use their regular responsive sizing.
- Preserved the existing draggable chat-panel implementation and its mouse, pen and touch pointer handling.

## 2026-11-12 — AI-readable main_document rebuild
- Rebuilt the main documentation set against the actual `Portfolio_Web_67Zoom_VisualScale.zip` source tree rather than relying on older file descriptions.
- Added `00_AI_MASTER_GUIDE.md` as the central reading order, source-of-truth policy, current capability description, preservation rules, safe-change workflow and delivery contract for future AI-assisted edits.
- Regenerated `01_SO_DO_FILE.md` from the current on-disk project tree, including CSS/JS additions, Java backend, frontend, SQL, tests and the expanded `main_document` folder.
- Corrected `02_HUONG_DAN_CAI_DAT_SU_DUNG.md` to describe the current 67% CSS zoom implementation, local Java NLP backend, disabled Internet search provider and correct MySQL setup.
- Rewrote `03_GIAI_THICH_SO_DO_FILE.md` to map the current source architecture and responsibilities of the HTML, CSS, JavaScript, Spring controllers/services, DTOs and schema.
- Added `05_BAN_DO_PHU_THUOC.md` to map user-visible features to the files, endpoints and database objects that must be reviewed together.
- Added `06_PROMPT_CHINH_SUA_MAU.md` as a reusable prompt for handing the ZIP to another AI.
- Added `07_CHECKLIST_KIEM_THU.md` for static checks, Java tests, integration checks, responsive review and handoff.
- Documentation-only update: application source files, frontend behavior, Java classes and database scripts were not modified in this documentation pass.


## 2026-11-12 — Clearer MoteLona chat layout at 100% browser zoom
- Added a compact-height layout in `Project/1/motelona-ai/frontend/motelona.css` for viewports up to 760 CSS pixels tall.
- Converted the tools row and suggested prompts into horizontally scrollable single rows in compact mode, recovering vertical space for the actual conversation. All controls remain in the DOM and accessible by horizontal scrolling.
- Slightly reduced header, status, composer and footer spacing in compact mode; very short viewports receive an additional spacing refinement.
- Preserved the normal multi-row layout on taller viewports and left the chat JavaScript, backend, theme logic, and pointer-based draggable behavior unchanged.
- Updated the AI master guide, architecture explanation, dependency map, reusable AI prompt, and QA checklist to document the new responsive behavior.
- Static source update only in this pass; a real browser visual test and Maven/MySQL end-to-end test were not completed.


## 2026-11-12 — Main-document synchronization for compact-height MoteLona
- Expanded `00_AI_MASTER_GUIDE.md` to distinguish the 760 CSS-pixel viewport-height breakpoint from browser zoom percentage; 100% browser zoom alone does not guarantee compact mode.
- Added concrete behavior for the horizontally scrollable tools row, optional search row, horizontally scrollable suggested prompts, vertically scrollable conversation region, compact composer/footer, and the additional `max-height: 500px` refinement.
- Updated setup/use instructions, architecture explanation, dependency map, AI editing prompt and QA checklist so future AI edits know which file/selectors to inspect and what to preserve.
- Reconfirmed that the compact-height behavior is CSS-only: no chat JavaScript, Java backend, API contract, database schema or draggable-panel logic was changed in this documentation update.
- At that documentation-only checkpoint, the ZIP file tree remained at 109 files; the subsequent MoteLona visual redesign added `assets/css/motelona-premium.css`, bringing the tree at that 2026-11-12 checkpoint to 110 files. The current 2026-11-13 tree contains 111 files, including the shared `assets/css/typography-refinement.css` stylesheet.


## 2026-11-12 — MoteLona visual redesign and detail-page navigation
- Replaced the layered detail-page CSS cascade with a single purpose-built `assets/css/motelona-premium.css` stylesheet. The detail page now uses its natural desktop scale at browser zoom 100%; the old 67% detail-page zoom is no longer loaded. The separate Portfolio section scale remains unchanged.
- Rebuilt the MoteLona visual language: deeper forest/charcoal canvas, controlled green/secondary accents, clearer hero hierarchy, project snapshot panel, compact sticky section rail, consistent cards, pipeline, glossary, database endpoints, quality checklist, FAQ and roadmap. Added responsive tablet/mobile layouts plus light-mode, alternate palette, reduced-motion and print handling.
- Refined the MoteLona chat launcher/panel skin in `Project/1/motelona-ai/frontend/motelona.css` without changing chat API payloads, consent behavior or the Java local NLP architecture.
- Improved the detail page's `pageContext()` so the Java retrieval service receives separate context for the actual MoteLona guide sections. Expanded `ActionRouter` and its tests for navigation to Overview, Capabilities, Architecture, NLP basics, Data & API, Quality, Scope and Roadmap; frontend navigation remains restricted to a fixed allow-list.
- Updated the main documentation and QA checklist so future edits do not restore the old detail-page zoom layer.
- Moved the section rail outside the hero so sticky positioning works across the entire page; added IntersectionObserver-based active-section highlighting and aligned the browser theme color with the new canvas.
- Improved route matching to prefer the most specific destination phrase (for example, “project architecture” resolves to Architecture rather than Projects); added a regression test and passed a standalone Java 21 compile/smoke check for `ActionRouter`.
- Static JavaScript syntax checks passed. Maven tests, browser-rendered visual checks and MySQL end-to-end validation could not be completed in this environment (the Maven Wrapper distribution was not available locally and network name resolution failed; headless Chromium did not complete startup).


## 2026-11-12 — Resizable MoteLona assistant window
- Added a visible bottom-right resize grip to the floating MoteLona chat window. Dragging the grip changes width and height independently of the existing header-drag behavior.
- Implemented pointer-based resizing for mouse, pen and touch, with viewport-aware minimum/maximum bounds and scale compensation for CSS zoom.
- Added keyboard resizing with arrow keys (Shift for larger increments) and double-click reset to a near-default size. The grip label/title follows EN/VI language changes.
- Reserved space in the panel footer so the grip does not obstruct the chat controls; mobile layouts use smaller safe minimum dimensions.
- Updated the AI master guide, setup instructions, architecture notes, dependency map, reusable edit prompt and QA checklist.
- Static JavaScript/CSS/archive checks are included in this delivery; full browser pointer interaction and deployed backend behavior still require manual browser verification.
