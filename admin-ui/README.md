# Dealer Admin UI

Vue 3 + TypeScript + Vite + Element Plus admin frontend for the dealer portal.

## Stack

- Vue 3
- TypeScript
- Vite
- Element Plus
- Pinia
- Vue Router
- Axios
- ECharts

## Local Development

```bash
pnpm install
pnpm dev
```

The dev server defaults to `http://127.0.0.1:8083` and proxies `VITE_APP_BASE_API` to the backend configured in `vite.config.ts`.

## Build

```bash
pnpm typecheck
pnpm build
pnpm preview
```

Runtime settings are loaded from `public/_app.config.js` and copied into `dist/_app.config.js` during build. Change that file after deployment when only API base URL or external monitor links need to change.

Google login is controlled by runtime config. Set `VITE_APP_GOOGLE_CLIENT_ID` in `public/_app.config.js` or the generated `dist/_app.config.js`; leave it empty to hide the Google login button. The backend must use the same Web Client ID through `GOOGLE_CLIENT_ID`.

## Docker / Nginx Deployment

Production Docker Compose deployment should prefer same-origin Nginx proxy:

- Keep `VITE_APP_BASE_API` as `/dev-api` in `public/_app.config.js`.
- Serve frontend static files from the Nginx container.
- Proxy `/dev-api/*` to the backend through `API_PROXY_PASS`.
- In Docker Compose, `API_PROXY_PASS` should use the backend service name, for example `http://bocoo-admin:8081`.
- Do not put Docker Compose service names in browser-facing frontend config.

See `../docs/deployment-nginx-same-origin.md` for the full deployment note and CORS guidance.

## Source Layout

- `src/api`: typed backend API wrappers
- `src/components`: reusable shared components
- `src/layout`: admin shell, sidebar, navbar and tags view
- `src/pages`: TypeScript Vue pages aligned with backend menu components
- `src/router`: static routes and route guard
- `src/stores`: Pinia stores
- `src/utils`: request, auth, dict, datetime and shared helpers
- `public`: static runtime assets
- `vite`: Vite plugin helpers
