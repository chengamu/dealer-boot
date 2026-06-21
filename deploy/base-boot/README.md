# Base Boot Docker Deploy

Full operational runbook: `DEPLOYMENT.md`.

This template keeps public port 80 on a dedicated Nginx proxy container. The `admin-ui` container only binds to `127.0.0.1:18080` for local server debugging, and `bocoo-admin`, PostgreSQL, Redis, and MinIO stay inside the Docker network.

Runtime config and data stay on the host:

- Compose secrets: `.env`
- Spring Boot config: `config/application-prod.yml`
- PostgreSQL data: `data/postgres`
- Redis data: `data/redis`
- MinIO data: `data/minio`
- Backend logs: `logs`

## Port Layout

- Proxy Nginx container: `0.0.0.0:80`
- Admin UI container: `127.0.0.1:18080 -> 80`
- Backend: Docker internal `bocoo-admin:8081`
- PostgreSQL: Docker internal `postgres:5432`
- Redis: Docker internal `redis:6379`
- MinIO internal API: Docker internal `minio:9000`
- MinIO public object path: Proxy Nginx `/oss/` -> `minio:9000`

## First Deploy

1. Keep `.env` and `config/application-prod.yml` next to `docker-compose.prod.yml`. They contain local deployment secrets and are ignored by git.
2. Build the backend jar locally or on the server:

   ```bash
   mvn -pl bocoo-admin -am -DskipTests package
   ```

3. Put the built jar next to `docker-compose.prod.yml` as `bocoo-admin.jar`.
4. Start services:

   ```bash
   docker compose --env-file .env -f docker-compose.prod.yml up -d --build
   ```

5. Initialize PostgreSQL after the database is healthy. Run the SQL files from the repository root, in this order:

   ```bash
   docker compose --env-file deploy/base-boot/.env -f deploy/base-boot/docker-compose.prod.yml exec -T postgres \
     sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB"' < sql/postgresql/base.sql

   docker compose --env-file deploy/base-boot/.env -f deploy/base-boot/docker-compose.prod.yml exec -T postgres \
     sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB"' < sql/postgresql/product_capability.sql
   ```

Add `sql/postgresql/pay.sql` only when the payment module is needed.

6. Sync the database OSS config to the local MinIO container credentials:

   ```bash
   docker compose --env-file deploy/base-boot/.env -f deploy/base-boot/docker-compose.prod.yml exec -T postgres \
     sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB"' < deploy/base-boot/sql/local-minio-oss-config.sql
   ```

## Verify

```bash
docker compose --env-file .env -f docker-compose.prod.yml ps
curl -I http://127.0.0.1:18080
curl -I http://127.0.0.1/
docker compose --env-file .env -f docker-compose.prod.yml logs --tail=100 bocoo-admin
```

Do not expose backend, database, Redis, or MinIO ports to the public security group unless there is a specific operational reason.
