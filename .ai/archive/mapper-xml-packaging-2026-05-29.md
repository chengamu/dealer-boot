# Archive: mapper XML packaging review

## Feature

Release package and module jar mapper XML packaging strategy review.

## Requirement Source

- User requested a standalone review of whether mapper XML files are correctly packaged into module jars and the admin release jar.

## Final Status

Accepted with Risks

## Scope

- Maven resource packaging for modules that contain `src/main/resources/mapper`.
- Module jar artifacts under each module `target`.
- Installed local Maven artifact for `bocoo-modules-system`.
- Admin release artifact under `bocoo-admin/target/dist/bocoo-admin.jar`.

## Out of Scope

- No business code changes.
- No POM/build configuration changes.
- No database, migration, or seed changes.
- No frontend changes.
- No runtime behavior changes.

## Completed

- Reviewed `bocoo-modules-system` effective Maven resource configuration.
- Rebuilt and inspected `bocoo-modules-system` module jar.
- Installed and inspected local Maven `bocoo-modules-system` artifact.
- Built and inspected admin release jar under `bocoo-admin/target/dist`.
- Verified nested `bocoo-modules-system` and `bocoo-modules-generator` jars in the release jar contain mapper XML.

## Not Completed

- No runtime server was started for this review because the request was specifically about packaging strategy and jar contents.

## Validation Summary

- `mvn -pl bocoo-modules-system help:effective-pom -Doutput=...`: passed.
- `mvn -pl bocoo-modules-system -DskipTests package`: passed.
- `mvn -pl bocoo-modules-system -DskipTests install`: passed.
- `mvn -pl bocoo-admin -am -DskipTests package`: passed.
- Jar inspection confirmed:
  - `bocoo-modules-system/target/bocoo-modules-system-1.0.0.jar` contains 18 mapper XML files.
  - Local Maven `bocoo-modules-system-1.0.0.jar` contains mapper XML.
  - `bocoo-admin/target/dist/bocoo-admin.jar` contains nested `BOOT-INF/lib/bocoo-modules-system-1.0.0.jar`, and that nested jar contains mapper XML including `mapper/system/SysMenuMapper.xml` and `mapper/system/SysRoleMapper.xml`.
  - `bocoo-modules-generator/target/bocoo-modules-generator-1.0.0.jar` contains 2 mapper XML files.
  - `bocoo-admin/target/dist/bocoo-admin.jar` nested `bocoo-modules-generator-1.0.0.jar` contains generator mapper XML.

## Remaining Risks

- No mapper XML packaging strategy defect was found.
- Operational risk remains: do not judge release packaging from stale module jars. Rebuild/package or inspect `bocoo-admin/target/dist/bocoo-admin.jar` before runtime validation.
- Existing unrelated working tree changes remained outside this review.

## Lessons from Learn

- If `target/classes` has expected resources but a jar does not, compare timestamps and rebuild before changing code or POM.
- In this project, `bocoo-admin/target/bocoo-admin.jar` is a skinny jar; the Spring Boot release jar is `bocoo-admin/target/dist/bocoo-admin.jar`.
- For mapper XML packaging checks, inspect nested `BOOT-INF/lib/*.jar` inside the release jar, not only the top-level admin jar entries.

## Key Decisions

- No POM or packaging configuration change is required.
- The earlier mapper XML concern is recorded as a stale artifact issue, not a build strategy issue.

## Files Modified

- `.ai/CURRENT.md`
- `.ai/TASKS.md`
- `.ai/MEMORY.md`
- `.ai/playbooks/java-build.md`
- `.ai/HANDOFF.md`
- `.ai/archive/mapper-xml-packaging-2026-05-29.md`

## Artifacts

- No long-lived runtime artifact was produced.
- Temporary jar extraction files were removed after inspection.

## Follow-up

- Before future packaged-runtime validation, rebuild or inspect `bocoo-admin/target/dist/bocoo-admin.jar`.
