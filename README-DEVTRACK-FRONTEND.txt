DevTrack frontend source

This archive contains the frontend source and the integrations completed so far:
- Existing authentication and registration flow preserved
- Projects section
- Issues section wired to the backend API
- Team members section wired to the backend API
- Dashboard using project and issue API data

Excluded: node_modules, .next, and local environment secrets (.env.local).

Setup:
1. Extract this folder.
2. Copy .env.example to .env.local.
3. Set NEXT_PUBLIC_API_BASE_URL to your backend URL.
4. Run npm install (or npm ci if using package-lock.json).
5. Run npm run dev.

The archive is assembled from the latest frontend source ZIP available in this conversation plus the prepared issues, team, and dashboard integration files. It has not been independently rebuilt or deployment-tested as part of packaging.
