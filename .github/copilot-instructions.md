# Instructions for GitHub Copilot

- Keep the static portfolio, `Project/1/motelona.html`, frontend chat runtime and Java backend synchronized.
- The local AI engine is Java NLP and retrieval. Do not add an external AI model/API or require an AI API key unless the project owner explicitly changes the requirement.
- Internet search is opt-in and uses DuckDuckGo's public HTML results page through `DuckDuckGoHtmlSearchProvider`. It needs outbound HTTPS access, may be rate-limited or change format, and must display source links. Do not fabricate results or silently transmit queries.
- Page navigation must use the allow-listed section/page IDs in `ActionRouter` and the frontend. Highlight text through safe DOM ranges; never execute page text or insert untrusted HTML.
- MySQL is the application database. Keep `backend/src/main/resources/schema.sql` and `database/PortfolioAI_MySQL.sql` aligned when database changes are required. Do not commit `.env` or real credentials.
- Preserve Vietnamese and English UI, privacy consent, rate limits, CORS allow-list, accessibility and responsive chat behavior.
- For code changes, run `.\mvnw.cmd clean test` from the backend when Java 21 is available. Use `.\VERIFY_MOTELONA.ps1` only when MySQL and `.env` are configured. Report checks that were skipped.
- Read `main_document/00_AI_MASTER_GUIDE.md` and `main_document/05_BAN_DO_PHU_THUOC.md` before larger changes, then update related documentation.
