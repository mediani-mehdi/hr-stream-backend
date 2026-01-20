# hr-stream

## Docker Compose

### Common issue: `.env` not being picked up

Docker Compose loads a `.env` file **from the directory you run the command in** (the project directory).

This repo includes `env_file: ./.env` in `compose.yaml` to make behavior more deterministic.

#### Verify what Compose sees

Run this from the repo root (`D:\hr-stream`):

```powershell
cd D:\hr-stream
docker compose config
```

You should see concrete values rendered for `POSTGRES_*` and `MINIO_ROOT_*`.

### Reset Postgres credentials (dev)

Postgres only applies `POSTGRES_PASSWORD` on first init. If you changed `.env` after the volume was created, reset the volume:

```powershell
cd D:\hr-stream
docker compose down -v
docker compose up -d
```

