#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
ENV_FILE="$PROJECT_DIR/.env"
RUNTIME_DIR="$PROJECT_DIR/.runtime"
BACKEND_DIR="$PROJECT_DIR/flower-shop-backend"
FRONTEND_DIR="$PROJECT_DIR/flower-shop-frontend"
BACKEND_LOG="$RUNTIME_DIR/backend.log"
FRONTEND_LOG="$RUNTIME_DIR/frontend.log"
BACKEND_PID_FILE="$RUNTIME_DIR/backend.pid"
FRONTEND_PID_FILE="$RUNTIME_DIR/frontend.pid"
ERRORS=0

info() { printf '[信息] %s\n' "$1"; }
ok() { printf '[通过] %s\n' "$1"; }
error() { ERRORS=$((ERRORS + 1)); printf '[错误] %s\n' "$1" >&2; }

load_config() {
  if [[ -f "$ENV_FILE" ]]; then
    while IFS='=' read -r key value; do
      key="${key#${key%%[![:space:]]*}}"; key="${key%${key##*[![:space:]]}}"
      [[ -z "$key" || "$key" == \#* || ! "$key" =~ ^[A-Za-z_][A-Za-z0-9_]*$ ]] && continue
      export "$key=${value%$'\r'}"
    done < "$ENV_FILE"
  fi
  DB_HOST="${DB_HOST:-127.0.0.1}"; DB_PORT="${DB_PORT:-3306}"
  DB_NAME="${DB_NAME:-flower_shop}"; DB_USER="${DB_USER:-root}"; DB_PASSWORD="${DB_PASSWORD:-}"
  REDIS_HOST="${REDIS_HOST:-127.0.0.1}"; REDIS_PORT="${REDIS_PORT:-6379}"
  REDIS_PASSWORD="${REDIS_PASSWORD:-}"; REDIS_DATABASE="${REDIS_DATABASE:-0}"
  BACKEND_PORT="${BACKEND_PORT:-5303}"; FRONTEND_PORT="${FRONTEND_PORT:-5302}"
  FRONTEND_HOST="${FRONTEND_HOST:-127.0.0.1}"
  CORS_ALLOWED_ORIGINS="${CORS_ALLOWED_ORIGINS:-http://127.0.0.1:$FRONTEND_PORT,http://localhost:$FRONTEND_PORT}"
  VITE_BACKEND_TARGET="${VITE_BACKEND_TARGET:-http://127.0.0.1:$BACKEND_PORT}"
  UPLOAD_DIR="${UPLOAD_DIR:-uploads}"
  JWT_SECRET="${JWT_SECRET:-FlowerShopLocalDemoKey-ChangeBeforeProduction-2026!}"
  JWT_EXPIRE_SECONDS="${JWT_EXPIRE_SECONDS:-604800}"
  MYSQL_URL="jdbc:mysql://$DB_HOST:$DB_PORT/$DB_NAME?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false"
  MYSQL_USERNAME="$DB_USER"; MYSQL_PASSWORD="$DB_PASSWORD"
  export DB_HOST DB_PORT DB_NAME DB_USER DB_PASSWORD MYSQL_URL MYSQL_USERNAME MYSQL_PASSWORD
  export REDIS_HOST REDIS_PORT REDIS_PASSWORD REDIS_DATABASE BACKEND_PORT FRONTEND_PORT FRONTEND_HOST
  export CORS_ALLOWED_ORIGINS VITE_BACKEND_TARGET UPLOAD_DIR JWT_SECRET JWT_EXPIRE_SECONDS
}

version_major() { printf '%s' "$1" | sed -E 's/^v?([0-9]+).*/\1/'; }
mysql_cmd() { MYSQL_PWD="$DB_PASSWORD" mysql --protocol=tcp --connect-timeout=3 -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" --default-character-set=utf8mb4 "$@"; }
redis_cmd() {
  if [[ -n "$REDIS_PASSWORD" ]]; then
    REDISCLI_AUTH="$REDIS_PASSWORD" redis-cli -h "$REDIS_HOST" -p "$REDIS_PORT" -n "$REDIS_DATABASE" "$@"
  else
    env -u REDISCLI_AUTH redis-cli -h "$REDIS_HOST" -p "$REDIS_PORT" -n "$REDIS_DATABASE" "$@"
  fi
}
port_used() { lsof -nP -iTCP:"$1" -sTCP:LISTEN >/dev/null 2>&1; }
pid_is_ours() {
  local pid="$1" cwd=""
  [[ "$pid" =~ ^[0-9]+$ ]] && kill -0 "$pid" 2>/dev/null || return 1
  cwd="$(lsof -a -p "$pid" -d cwd -Fn 2>/dev/null | sed -n 's/^n//p' | head -1)"
  [[ "$cwd" == "$PROJECT_DIR" || "$cwd" == "$PROJECT_DIR/"* ]] || ps -p "$pid" -o command= 2>/dev/null | grep -Fq "$PROJECT_DIR"
}
pid_file_running() { [[ -f "$1" ]] && pid_is_ours "$(tr -dc '0-9' < "$1")"; }
running() { pid_file_running "$BACKEND_PID_FILE" && pid_file_running "$FRONTEND_PID_FILE"; }

check() {
  ERRORS=0; load_config
  [[ -f "$BACKEND_DIR/pom.xml" && -f "$FRONTEND_DIR/package-lock.json" && -f "$BACKEND_DIR/sql/flower_shop_ddl.sql" && -f "$BACKEND_DIR/sql/flower_shop_init_data.sql" && -f "$PROJECT_DIR/.env.example" ]] && ok '项目核心文件完整' || error '项目核心文件不完整。'
  if command -v java >/dev/null 2>&1; then
    local jv; jv="$(java -version 2>&1 | awk -F\" 'NR==1{print $2}')"
    (( $(version_major "$jv") >= 17 )) && ok "JDK $jv" || error "JDK 版本过低：$jv，需要 17+。"
  else error '未找到 JDK 17+。'; fi
  if command -v mvn >/dev/null 2>&1; then
    local mv; mv="$(mvn -version 2>/dev/null | awk 'NR==1{print $3}')"
    (( $(version_major "$mv") >= 3 )) && ok "Maven $mv" || error "Maven 版本过低：$mv，需要 3.8+。"
  else error '未找到 Maven 3.8+。'; fi
  if command -v node >/dev/null 2>&1 && command -v npm >/dev/null 2>&1; then
    local nv major minor; nv="$(node --version)"; major="$(version_major "$nv")"; minor="$(printf '%s' "$nv" | sed -E 's/^v?[0-9]+\.([0-9]+).*/\1/')"
    if (( major > 22 || (major == 22 && minor >= 12) || (major == 20 && minor >= 19) )); then ok "Node.js ${nv#v} / npm $(npm --version)"; else error "Node.js 版本不兼容：${nv#v}，需要 20.19+ 或 22.12+。"; fi
  else error '未找到 Node.js 20.19+ / 22.12+ 与 npm。'; fi
  if command -v mysql >/dev/null 2>&1; then
    local dbv; dbv="$(mysql_cmd -Nse 'SELECT VERSION();' 2>/dev/null || true)"
    [[ "${dbv%%.*}" == 8 ]] && ok "MySQL $dbv 连接成功" || error 'MySQL 8 无法连接，请检查服务与 .env。'
  else error '未找到 MySQL 客户端。'; fi
  if command -v redis-cli >/dev/null 2>&1; then
    local redis_reply; redis_reply="$(redis_cmd ping 2>/dev/null || true)"
    [[ "$redis_reply" == "PONG" ]] && ok 'Redis 连接成功' || error 'Redis 无法连接，请检查服务与 .env。'
  else error '未找到 redis-cli，需要 Redis 6+。'; fi
  command -v curl >/dev/null 2>&1 && ok 'curl 可用' || error '未找到 curl。'
  command -v lsof >/dev/null 2>&1 && ok 'lsof 可用' || error '未找到 lsof。'
  [[ "$DB_NAME" =~ ^[A-Za-z0-9_]+$ ]] || error 'DB_NAME 只能包含字母、数字和下划线。'
  [[ "$DB_PORT" =~ ^[0-9]+$ && "$REDIS_PORT" =~ ^[0-9]+$ && "$REDIS_DATABASE" =~ ^[0-9]+$ ]] || error '数据库端口和 Redis 库编号必须是数字。'
  [[ "$BACKEND_PORT" =~ ^[0-9]+$ && "$FRONTEND_PORT" =~ ^[0-9]+$ ]] || error '前后端端口必须是数字。'
  (( ${#JWT_SECRET} >= 32 )) || error 'JWT_SECRET 至少需要 32 个字符。'
  local available_kb; available_kb="$(df -Pk "$PROJECT_DIR" | awk 'NR==2{print $4}')"
  (( available_kb >= 1048576 )) && ok '剩余磁盘空间不少于 1 GB' || error '剩余磁盘空间不足 1 GB。'
  for port in "$BACKEND_PORT" "$FRONTEND_PORT"; do
    if running; then ok "端口 $port 由本项目使用"; elif port_used "$port"; then error "端口 $port 已被占用。"; else ok "端口 $port 可用"; fi
  done
  (( ERRORS == 0 )) || { printf '环境检测未通过：%d 个错误。\n' "$ERRORS" >&2; return 1; }
  printf '环境检测通过。\n'
}

setup_database() {
  mysql_cmd -e "CREATE DATABASE IF NOT EXISTS \`$DB_NAME\` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"
  local user_table_count all_table_count
  user_table_count="$(mysql_cmd -Nse "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='$DB_NAME' AND table_name='user';")"
  all_table_count="$(mysql_cmd -Nse "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='$DB_NAME';")"
  if [[ "$user_table_count" == 0 ]]; then
    [[ "$all_table_count" == 0 ]] || { error "数据库 $DB_NAME 已有其他表但缺少 user；为防止误覆盖，请改用空数据库名。"; return 1; }
    info '检测到空数据库，正在导入表结构与演示数据……'
    mysql_cmd "$DB_NAME" < "$BACKEND_DIR/sql/flower_shop_ddl.sql"
    mysql_cmd "$DB_NAME" < "$BACKEND_DIR/sql/flower_shop_init_data.sql"
    ok '数据库初始化完成。'
  else
    ok '数据库已有结构，保留现有数据。'
  fi
}

setup() {
  mkdir -p "$RUNTIME_DIR"
  if [[ ! -f "$ENV_FILE" ]]; then
    cp "$PROJECT_DIR/.env.example" "$ENV_FILE"; chmod 600 "$ENV_FILE" 2>/dev/null || true
    info '已生成 .env；MySQL 有密码时请填写 DB_PASSWORD 后重新运行。'
  fi
  load_config; check; setup_database
  (cd "$BACKEND_DIR" && mvn -q clean package)
  (cd "$FRONTEND_DIR" && npm ci --no-audit --no-fund && npm run build)
  ok '后端测试、打包与前端构建通过。'
}

stop_pid_file() {
  local file="$1" pid=""
  [[ -f "$file" ]] || return 0
  pid="$(tr -dc '0-9' < "$file")"
  if [[ -n "$pid" ]] && pid_is_ours "$pid"; then
    kill "$pid" 2>/dev/null || true
    local attempt
    for ((attempt=1; attempt<=50; attempt++)); do kill -0 "$pid" 2>/dev/null || break; sleep 0.1; done
    kill -0 "$pid" 2>/dev/null && pid_is_ours "$pid" && kill -9 "$pid" 2>/dev/null || true
  fi
  rm -f "$file"
}
stop_stale() { stop_pid_file "$FRONTEND_PID_FILE"; stop_pid_file "$BACKEND_PID_FILE"; }
stop() { stop_stale; ok '系统已停止。'; }

start() {
  load_config
  if running; then ok "系统已运行：http://127.0.0.1:$FRONTEND_PORT"; return; fi
  setup; load_config; stop_stale
  info '正在启动花漾集多商家鲜花商城……'
  node "$SCRIPT_DIR/spawn_detached.mjs" --cwd "$BACKEND_DIR" --log "$BACKEND_LOG" -- java -jar target/flower-shop-backend-1.0.0.jar > "$BACKEND_PID_FILE"
  local i
  for ((i=1; i<=60; i++)); do
    if curl -fsS --max-time 2 "http://127.0.0.1:$BACKEND_PORT/api/common/categories" >/dev/null 2>&1; then break; fi
    sleep 1
  done
  if (( i > 60 )); then tail -n 80 "$BACKEND_LOG" >&2 || true; stop; return 1; fi
  node "$SCRIPT_DIR/spawn_detached.mjs" --cwd "$FRONTEND_DIR" --log "$FRONTEND_LOG" -- npm run preview -- --host "$FRONTEND_HOST" --port "$FRONTEND_PORT" > "$FRONTEND_PID_FILE"
  for ((i=1; i<=30; i++)); do
    if curl -fsS --max-time 2 "http://127.0.0.1:$FRONTEND_PORT" >/dev/null 2>&1; then
      ok "启动成功：http://127.0.0.1:$FRONTEND_PORT"
      printf '买家：customer_li / customer123\n商家：merchant_rose / merchant123\n管理员：admin / admin123\n'
      [[ "${FLOWER_SHOP_NO_OPEN:-0}" == 1 ]] || { command -v open >/dev/null 2>&1 && open "http://127.0.0.1:$FRONTEND_PORT" >/dev/null 2>&1 || true; }
      return
    fi
    sleep 1
  done
  tail -n 80 "$FRONTEND_LOG" >&2 || true; stop; return 1
}

status() { load_config; running && printf '[运行] http://127.0.0.1:%s\n' "$FRONTEND_PORT" || printf '[停止]\n'; printf '日志目录：%s\n' "$RUNTIME_DIR"; }

case "${1:-help}" in
  check) check ;; setup) setup ;; start) start ;; stop) stop ;; status) status ;;
  *) printf '用法：bash scripts/launcher.sh {check|setup|start|stop|status}\n' ;;
esac
