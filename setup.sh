#!/usr/bin/env bash
set -euo pipefail

# ---------------------------------------------
# HR-Stream ATS - Docker Setup Script
# ---------------------------------------------
# 1) Checks Docker + Compose
# 2) Generates strong secrets in ./secrets
# 3) Copies .env.example -> .env (if missing)
# 4) Prompts user to review .env
# 5) Builds + starts services
# 6) Shows status
# 7) Prints helpful management commands
# ---------------------------------------------

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
BOLD='\033[1m'
NC='\033[0m'

info()  { echo -e "${BLUE}ℹ${NC} $*"; }
warn()  { echo -e "${YELLOW}⚠${NC} $*"; }
ok()    { echo -e "${GREEN}✔${NC} $*"; }
fail()  { echo -e "${RED}✖${NC} $*"; exit 1; }

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SECRETS_DIR="$PROJECT_DIR/secrets"
ENV_EXAMPLE="$PROJECT_DIR/.env.example"
ENV_FILE="$PROJECT_DIR/.env"

require_cmd() {
  command -v "$1" >/dev/null 2>&1 || fail "Missing dependency: $1"
}

# Prefer Docker Compose v2 plugin (docker compose), but support legacy (docker-compose)
compose_cmd() {
  if docker compose version >/dev/null 2>&1; then
    echo "docker compose"
  elif command -v docker-compose >/dev/null 2>&1; then
    echo "docker-compose"
  else
    return 1
  fi
}

# Generate 32+ bytes secret tokens
rand_hex() {
  # 32 bytes => 64 hex chars
  openssl rand -hex 32
}

rand_password() {
  # 24 chars base64, strip non-url friendly chars a bit
  # (MinIO requires >= 8 chars)
  openssl rand -base64 32 | tr -d '\n' | tr -d '=/' | cut -c1-24
}

write_secret() {
  local file="$1"
  local value="$2"

  if [[ -f "$file" ]] && [[ -s "$file" ]]; then
    ok "Secret exists: ${file#$PROJECT_DIR/}"
    return 0
  fi

  mkdir -p "$(dirname "$file")"
  umask 077
  printf "%s" "$value" > "$file"
  ok "Generated secret: ${file#$PROJECT_DIR/}"
}

open_editor() {
  local file="$1"

  if [[ -n "${EDITOR:-}" ]]; then
    "$EDITOR" "$file" || true
    return 0
  fi

  if command -v nano >/dev/null 2>&1; then
    nano "$file" || true
    return 0
  fi

  if command -v vi >/dev/null 2>&1; then
    vi "$file" || true
    return 0
  fi

  warn "No editor found (EDITOR/nano/vi). Please open and edit manually: $file"
}

main() {
  echo -e "${BOLD}HR-Stream ATS Setup${NC}"
  echo -e "Project: ${PROJECT_DIR}"
  echo

  info "Checking prerequisites..."
  require_cmd docker

  local COMPOSE
  COMPOSE="$(compose_cmd)" || fail "Docker Compose not found. Install Docker Desktop (Compose v2) or docker-compose."
  ok "Docker found: $(docker --version)"
  ok "Compose found: $($COMPOSE version 2>/dev/null | head -n 1 || true)"

  if ! docker info >/dev/null 2>&1; then
    fail "Docker daemon not reachable. Is Docker running?"
  fi
  ok "Docker daemon is running"

  echo
  info "Preparing environment files..."
  if [[ ! -f "$ENV_EXAMPLE" ]]; then
    fail "Missing .env.example at $ENV_EXAMPLE"
  fi

  if [[ ! -f "$ENV_FILE" ]]; then
    cp "$ENV_EXAMPLE" "$ENV_FILE"
    ok "Created .env from .env.example"
  else
    ok ".env already exists"
  fi

  echo
  info "Generating secrets (only if missing)..."
  mkdir -p "$SECRETS_DIR"

  write_secret "$SECRETS_DIR/db_password.txt" "$(rand_password)"
  write_secret "$SECRETS_DIR/jwt_secret.txt" "$(rand_hex)"
  write_secret "$SECRETS_DIR/minio_root_password.txt" "$(rand_password)"
  write_secret "$SECRETS_DIR/minio_app_password.txt" "$(rand_password)"
  write_secret "$SECRETS_DIR/redis_password.txt" "$(rand_password)"

  # Gemini API key can't be auto-generated; keep placeholder if missing.
  if [[ ! -f "$SECRETS_DIR/gemini_api_key.txt" ]] || [[ ! -s "$SECRETS_DIR/gemini_api_key.txt" ]]; then
    mkdir -p "$SECRETS_DIR"
    umask 077
    printf "%s" "YOUR_GEMINI_API_KEY_HERE" > "$SECRETS_DIR/gemini_api_key.txt"
    warn "Created placeholder: secrets/gemini_api_key.txt (please set your real key)"
  else
    ok "Secret exists: secrets/gemini_api_key.txt"
  fi

  echo
  warn "Review your .env now (ports, usernames, bucket name, etc.)"
  read -r -p "Open .env in editor now? [Y/n] " ans
  ans=${ans:-Y}
  if [[ "$ans" =~ ^[Yy]$ ]]; then
    open_editor "$ENV_FILE"
  else
    warn "Skipping editor. You can edit later: $ENV_FILE"
  fi

  echo
  info "Building and starting services..."
  (cd "$PROJECT_DIR" && $COMPOSE up -d --build)
  ok "Services started"

  echo
  info "Current container status:"
  (cd "$PROJECT_DIR" && $COMPOSE ps)

  echo
  echo -e "${BOLD}Helpful commands${NC}"
  echo -e "  ${BLUE}# View logs${NC}"
  echo "  $COMPOSE logs -f"
  echo
  echo -e "  ${BLUE}# Restart all services${NC}"
  echo "  $COMPOSE restart"
  echo
  echo -e "  ${BLUE}# Stop services${NC}"
  echo "  $COMPOSE down"
  echo
  echo -e "  ${BLUE}# Stop + remove volumes (DANGEROUS: deletes DB data)${NC}"
  echo "  $COMPOSE down -v"
  echo
  echo -e "  ${BLUE}# Rebuild app only${NC}"
  echo "  $COMPOSE build app && $COMPOSE up -d app"
  echo
  echo -e "  ${BLUE}# Open Swagger UI${NC}"
  echo "  http://localhost:8090/swagger-ui/index.html"
  echo
  echo -e "  ${BLUE}# MinIO Console${NC}"
  echo "  http://localhost:9001"
  echo
  echo -e "  ${BLUE}# Prometheus${NC}"
  echo "  http://localhost:9090"
  echo
  echo -e "  ${BLUE}# Grafana${NC}"
  echo "  http://localhost:3000 (user: admin)"

  echo
  ok "Setup complete"
}

main "$@"

