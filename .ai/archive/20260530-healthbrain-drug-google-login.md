# 20260530 Healthbrain Drug Review Remediation / Google Login Archive

## Summary

- Completed the Healthbrain drug review remediation batch.
- Added lightweight `BaseBo` and migrated current BO classes away from direct `BaseEntity` inheritance where applicable.
- Updated the generator BO template to emit `extends BaseBo`.
- Added Google Identity Services login for existing enabled users by email.
- Added Google application-state handling for unmatched emails: apply required, pending, rejected, or approved-but-user-missing.
- Added local development CORS allowance for Vite port `8083`.
- Documented local Google login configuration and Google Console `Authorized JavaScript origins` guidance in `README.md`.

## Key Decisions

- Do not split `IBaseService` / `BaseServiceImpl` in this batch; keep service authoring lightweight.
- Do not remove audit fields or query helper compatibility from existing entities in this batch.
- Google login does not auto-create system users and does not add a Google binding table in the first version.
- Google login only succeeds after ID token verification and matching an existing enabled system user email.
- Runtime Google Client ID remains externalized; default runtime config is empty and must be set per environment.

## Validation

- `pnpm i18n:sync`: passed.
- `codegraph sync`: passed, already up to date.
- `mvn -pl bocoo-admin -am -DskipTests compile`: passed.
- `pnpm --dir admin-ui typecheck`: passed.
- `pnpm --dir admin-ui build`: passed.
- `mvn -pl bocoo-admin -am -DskipTests package`: passed.
- Browser smoke for login page without Google Client ID: passed.
- Local Google login CORS validation from `http://127.0.0.1:8083`: passed.
- Real Google login flow was confirmed by the user as working end to end.

## Cleanup

- Temporary local Google Client ID in `admin-ui/public/_app.config.js` was restored to empty.
- Temporary runtime services on ports `8081` and `8083` were stopped.

## Documentation

- `README.md` now records where to configure the frontend runtime Google Web Client ID.
- `README.md` now records backend `GOOGLE_CLIENT_ID` environment-variable usage.
- `README.md` clarifies that Google Console origins are frontend site origins, not every user IP, and must not include paths.

## Remaining TODO

- Configure production Google OAuth origin after final deployment domain or same-origin Nginx topology is confirmed.
- Keep Cookie/CSRF and production CORS tightening as a deployment-topology TODO.
- If Google login needs account linking later, plan a separate binding-table design instead of overloading this first version.
