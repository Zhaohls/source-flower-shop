param([ValidateSet("check","setup","start","stop","status")][string]$Action="check")
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$EnvFile = Join-Path $Root ".env"
$Runtime = Join-Path $Root ".runtime"
$BackendDir = Join-Path $Root "flower-shop-backend"
$FrontendDir = Join-Path $Root "flower-shop-frontend"
$BackendPid = Join-Path $Runtime "backend.pid"
$FrontendPid = Join-Path $Runtime "frontend.pid"
$BackendLog = Join-Path $Runtime "backend.log"
$FrontendLog = Join-Path $Runtime "frontend.log"

function Import-Env {
  if (Test-Path $EnvFile) {
    Get-Content $EnvFile -Encoding UTF8 | ForEach-Object {
      if ($_ -match '^([A-Za-z_][A-Za-z0-9_]*)=(.*)$') {
        [Environment]::SetEnvironmentVariable($Matches[1], $Matches[2], "Process")
      }
    }
  }
}
function Setting($Name,$Default) {
  $value=[Environment]::GetEnvironmentVariable($Name,"Process")
  if ($null -ne $value -and $value -ne "") {$value} else {$Default}
}
function Set-DerivedEnv {
  $dbHost=Setting "DB_HOST" "127.0.0.1";$dbPort=Setting "DB_PORT" "3306";$db=Setting "DB_NAME" "flower_shop"
  $backendPort=Setting "BACKEND_PORT" "5303";$frontendPort=Setting "FRONTEND_PORT" "5302"
  [Environment]::SetEnvironmentVariable("MYSQL_URL","jdbc:mysql://${dbHost}:${dbPort}/${db}?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false","Process")
  [Environment]::SetEnvironmentVariable("MYSQL_USERNAME",(Setting "DB_USER" "root"),"Process")
  [Environment]::SetEnvironmentVariable("MYSQL_PASSWORD",(Setting "DB_PASSWORD" ""),"Process")
  [Environment]::SetEnvironmentVariable("BACKEND_PORT",$backendPort,"Process")
  [Environment]::SetEnvironmentVariable("FRONTEND_PORT",$frontendPort,"Process")
  [Environment]::SetEnvironmentVariable("VITE_BACKEND_TARGET",(Setting "VITE_BACKEND_TARGET" "http://127.0.0.1:$backendPort"),"Process")
  [Environment]::SetEnvironmentVariable("CORS_ALLOWED_ORIGINS",(Setting "CORS_ALLOWED_ORIGINS" "http://127.0.0.1:$frontendPort,http://localhost:$frontendPort"),"Process")
}
function Is-OurProcess($PidFile) {
  if (!(Test-Path $PidFile)) { return $false }
  try {
    $processId = [int](Get-Content $PidFile -Raw)
    $process = Get-CimInstance Win32_Process -Filter "ProcessId=$processId" -ErrorAction Stop
    return ($null -ne $process.CommandLine -and $process.CommandLine.Contains($Root))
  } catch { return $false }
}
function Project-Running { return (Is-OurProcess $BackendPid) -and (Is-OurProcess $FrontendPid) }
function Check {
  Import-Env; Set-DerivedEnv; $errors=0
  if ((Test-Path (Join-Path $BackendDir "pom.xml")) -and (Test-Path (Join-Path $FrontendDir "package-lock.json")) -and (Test-Path (Join-Path $BackendDir "sql\flower_shop_ddl.sql")) -and (Test-Path (Join-Path $Root ".env.example"))) {
    Write-Host "[通过] 项目核心文件完整" -ForegroundColor Green
  } else { Write-Host "[错误] 项目核心文件不完整" -ForegroundColor Red; $errors++ }
  foreach($tool in @("java.exe","mvn.cmd","node.exe","npm.cmd","mysql.exe","redis-cli.exe","curl.exe")) {
    if(Get-Command $tool -ErrorAction SilentlyContinue){Write-Host "[通过] $tool" -ForegroundColor Green}
    else{Write-Host "[错误] 未找到 $tool" -ForegroundColor Red;$errors++}
  }
  if(Get-Command java.exe -ErrorAction SilentlyContinue){
    $version=((& java.exe -version 2>&1)[0] -replace '.*version "([0-9]+).*','$1')
    if([int]$version -lt 17){Write-Host "[错误] 需要 JDK 17+" -ForegroundColor Red;$errors++}
  }
  if(Get-Command node.exe -ErrorAction SilentlyContinue){
    $parts=(& node.exe --version).TrimStart('v').Split('.');$major=[int]$parts[0];$minor=[int]$parts[1]
    $compatible=($major -gt 22) -or (($major -eq 22) -and ($minor -ge 12)) -or (($major -eq 20) -and ($minor -ge 19))
    if(!$compatible){Write-Host "[错误] 需要 Node.js 20.19+ 或 22.12+" -ForegroundColor Red;$errors++}
  }
  $env:MYSQL_PWD=Setting "DB_PASSWORD" ""
  if(Get-Command mysql.exe -ErrorAction SilentlyContinue){
    try {
      $v=& mysql.exe --protocol=tcp -h (Setting "DB_HOST" "127.0.0.1") -P (Setting "DB_PORT" "3306") -u (Setting "DB_USER" "root") -Nse "SELECT VERSION();"
      if(!$v.StartsWith("8.")){throw "需要 MySQL 8"}
      Write-Host "[通过] MySQL $v" -ForegroundColor Green
    } catch {Write-Host "[错误] MySQL 无法连接，请检查服务与 .env" -ForegroundColor Red;$errors++}
  }
  if(Get-Command redis-cli.exe -ErrorAction SilentlyContinue){
    $env:REDISCLI_AUTH=Setting "REDIS_PASSWORD" ""
    try {
      $reply=& redis-cli.exe -h (Setting "REDIS_HOST" "127.0.0.1") -p (Setting "REDIS_PORT" "6379") -n (Setting "REDIS_DATABASE" "0") ping
      if($reply -ne "PONG"){throw "Redis 未返回 PONG"}
      Write-Host "[通过] Redis 连接成功" -ForegroundColor Green
    } catch {Write-Host "[错误] Redis 无法连接，请检查服务与 .env" -ForegroundColor Red;$errors++}
  }
  $jwt=Setting "JWT_SECRET" "FlowerShopLocalDemoKey-ChangeBeforeProduction-2026!"
  if($jwt.Length -lt 32){Write-Host "[错误] JWT_SECRET 至少 32 位" -ForegroundColor Red;$errors++}
  foreach($portName in @("BACKEND_PORT","FRONTEND_PORT")){
    $default=if($portName -eq "BACKEND_PORT"){"5303"}else{"5302"};$port=Setting $portName $default
    $used=Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
    if(Project-Running){Write-Host "[通过] 端口 $port 由本项目使用" -ForegroundColor Green}
    elseif($used){Write-Host "[错误] 端口 $port 已被占用" -ForegroundColor Red;$errors++}else{Write-Host "[通过] 端口 $port 可用" -ForegroundColor Green}
  }
  if($errors){throw "环境检测未通过：$errors 个错误"}
  Write-Host "环境检测通过。" -ForegroundColor Green
}
function Import-Sql($Database,$File) {
  $args=@("--protocol=tcp","-h",(Setting "DB_HOST" "127.0.0.1"),"-P",(Setting "DB_PORT" "3306"),"-u",(Setting "DB_USER" "root"),"--default-character-set=utf8mb4",$Database)
  $process=Start-Process "mysql.exe" -ArgumentList $args -RedirectStandardInput $File -NoNewWindow -Wait -PassThru
  if($process.ExitCode -ne 0){throw "导入 SQL 失败：$File"}
}
function Setup {
  New-Item -ItemType Directory -Force $Runtime | Out-Null
  if(!(Test-Path $EnvFile)){Copy-Item (Join-Path $Root ".env.example") $EnvFile;Write-Host "[信息] 已生成 .env；MySQL 有密码时请先填写 DB_PASSWORD。"}
  Check; Import-Env; Set-DerivedEnv
  $env:MYSQL_PWD=Setting "DB_PASSWORD" "";$dbHost=Setting "DB_HOST" "127.0.0.1";$dbPort=Setting "DB_PORT" "3306";$dbUser=Setting "DB_USER" "root";$db=Setting "DB_NAME" "flower_shop"
  if($db -notmatch '^[A-Za-z0-9_]+$'){throw "DB_NAME 只能包含字母、数字和下划线"}
  & mysql.exe --protocol=tcp -h $dbHost -P $dbPort -u $dbUser -e "CREATE DATABASE IF NOT EXISTS ``$db`` CHARACTER SET utf8mb4;"
  $count=& mysql.exe --protocol=tcp -h $dbHost -P $dbPort -u $dbUser -Nse "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='$db' AND table_name='user';"
  $allCount=& mysql.exe --protocol=tcp -h $dbHost -P $dbPort -u $dbUser -Nse "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='$db';"
  if($count -eq 0){
    if([int]$allCount -gt 0){throw "数据库 $db 已有其他表但缺少 user；请改用空数据库名。"}
    Write-Host "[信息] 正在导入表结构与演示数据……"
    Import-Sql $db (Join-Path $BackendDir "sql\flower_shop_ddl.sql")
    Import-Sql $db (Join-Path $BackendDir "sql\flower_shop_init_data.sql")
  } else {Write-Host "[通过] 数据库已有结构，保留现有数据。" -ForegroundColor Green}
  Push-Location $BackendDir;try{& mvn.cmd -q clean package;if($LASTEXITCODE){throw "Maven 构建失败"}}finally{Pop-Location}
  Push-Location $FrontendDir;try{& npm.cmd ci --no-audit --no-fund;if($LASTEXITCODE){throw "npm 安装失败"};& npm.cmd run build;if($LASTEXITCODE){throw "前端构建失败"}}finally{Pop-Location}
  Write-Host "[通过] 后端测试、打包与前端构建通过。" -ForegroundColor Green
}
function Stop-App {
  foreach($file in @($FrontendPid,$BackendPid)){
    if(Test-Path $file){
      if(Is-OurProcess $file){$processId=[int](Get-Content $file -Raw);& taskkill.exe /PID $processId /T /F 2>$null | Out-Null}
      Remove-Item $file -Force
    }
  }
  Write-Host "[通过] 系统已停止" -ForegroundColor Green
}
function Start-App {
  Import-Env; Set-DerivedEnv
  if(Project-Running){Write-Host "[通过] 系统已运行：http://127.0.0.1:$(Setting 'FRONTEND_PORT' '5302')" -ForegroundColor Green;return}
  Setup;Import-Env;Set-DerivedEnv;New-Item -ItemType Directory -Force $Runtime|Out-Null
  $backendPort=Setting "BACKEND_PORT" "5303";$frontendPort=Setting "FRONTEND_PORT" "5302"
  $backendJar=Join-Path $BackendDir "target\flower-shop-backend-1.0.0.jar"
  $backend=Start-Process "java.exe" -ArgumentList @("-jar",$backendJar) -WorkingDirectory $BackendDir -RedirectStandardOutput $BackendLog -RedirectStandardError (Join-Path $Runtime "backend-error.log") -PassThru -WindowStyle Hidden
  Set-Content $BackendPid $backend.Id -Encoding ASCII
  for($i=0;$i -lt 60;$i++){try{Invoke-WebRequest "http://127.0.0.1:$backendPort/api/common/categories" -UseBasicParsing -TimeoutSec 2|Out-Null;break}catch{Start-Sleep 1}}
  if($i -ge 60){Stop-App;throw "后端启动超时，请查看 $BackendLog"}
  $frontend=Start-Process "npm.cmd" -ArgumentList @("--prefix",$FrontendDir,"run","preview","--","--host","127.0.0.1","--port",$frontendPort) -WorkingDirectory $FrontendDir -RedirectStandardOutput $FrontendLog -RedirectStandardError (Join-Path $Runtime "frontend-error.log") -PassThru -WindowStyle Hidden
  Set-Content $FrontendPid $frontend.Id -Encoding ASCII
  for($i=0;$i -lt 30;$i++){try{Invoke-WebRequest "http://127.0.0.1:$frontendPort" -UseBasicParsing -TimeoutSec 2|Out-Null;Write-Host "[通过] http://127.0.0.1:$frontendPort" -ForegroundColor Green;Write-Host "买家：customer_li / customer123`n商家：merchant_rose / merchant123`n管理员：admin / admin123";Start-Process "http://127.0.0.1:$frontendPort";return}catch{Start-Sleep 1}}
  Stop-App;throw "前端启动超时，请查看 $FrontendLog"
}
switch($Action){"check"{Check};"setup"{Setup};"start"{Start-App};"stop"{Stop-App};"status"{if(Project-Running){Write-Host "[运行]"}else{Write-Host "[停止]"}}}
