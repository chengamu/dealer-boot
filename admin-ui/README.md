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
npm install
npm run dev
```

The dev server defaults to `http://127.0.0.1:8083` and proxies `VITE_APP_BASE_API` to the backend configured in `vite.config.ts`.

## Build

```bash
npm run typecheck
npm run build
npm run preview
```

Runtime settings are loaded from `public/_app.config.js` and copied into `dist/_app.config.js` during build. Change that file after deployment when only API base URL or external monitor links need to change.

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
