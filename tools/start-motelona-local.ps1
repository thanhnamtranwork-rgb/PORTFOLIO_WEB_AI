[CmdletBinding()]
param(
    [int]$SitePort = 5500,
    [int]$BackendPort = 8080
)

$ErrorActionPreference = 'Stop'
$workspace = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$backend = Join-Path $workspace 'Project\1\motelona-ai\backend'
$envFile = Join-Path $backend '.env'
$config = @{}
if (Test-Path -LiteralPath $envFile) {
    foreach ($line in Get-Content -LiteralPath $envFile) {
        if ($line -match '^\s*([^#=\s]+)\s*=\s*(.*)$') {
            $value = $Matches[2].Trim()
            if ($value.Length -ge 2 -and (($value.StartsWith('"') -and $value.EndsWith('"')) -or ($value.StartsWith("'") -and $value.EndsWith("'")))) {
                $value = $value.Substring(1, $value.Length - 2)
            }
            $config[$Matches[1]] = $value
        }
    }
}

$allowedKeys = @(
    'MONGODB_URI', 'GEMINI_API_KEY', 'GEMINI_MODEL', 'GEMINI_API_ENDPOINT',
    'GROQ_API_KEY', 'GROQ_MODEL', 'GROQ_API_ENDPOINT',
    'OPENROUTER_API_KEY', 'OPENROUTER_MODEL', 'OPENROUTER_API_ENDPOINT',
    'MOTELONA_CORS_ORIGINS', 'MOTELONA_SERVER_ADDRESS', 'MOTELONA_PORT'
)
foreach ($key in $allowedKeys) {
    if ($config.ContainsKey($key)) { [Environment]::SetEnvironmentVariable($key, $config[$key], 'Process') }
}
if (!$config.ContainsKey('MONGODB_URI')) { $env:MONGODB_URI = 'mongodb://127.0.0.1:27017/motelona_portfolio' }
$env:MOTELONA_PROJECT_ROOT = $workspace
$env:MOTELONA_PORT = [string]$BackendPort

$uri = [uri]$env:MONGODB_URI
$isLocalMongo = $uri.Host -in @('127.0.0.1', 'localhost', '::1')
if ($isLocalMongo) {
    $mongoPort = if ($uri.Port -gt 0) { $uri.Port } else { 27017 }
    $mongoListening = Test-NetConnection $uri.Host -Port $mongoPort -InformationLevel Quiet -WarningAction SilentlyContinue
    if (!$mongoListening) {
        & (Join-Path $PSScriptRoot 'start-mongodb-local.ps1') -Port $mongoPort
    }
}

$logDirectory = Join-Path $env:LOCALAPPDATA 'MoteLona\log'
New-Item -ItemType Directory -Path $logDirectory -Force | Out-Null
$backendUrl = "http://127.0.0.1:$BackendPort"
$backendStatusUrl = "$backendUrl/api/portfolio/status"
$backendListener = Get-NetTCPConnection -State Listen -LocalPort $BackendPort -ErrorAction SilentlyContinue | Select-Object -First 1
if ($backendListener) {
    try {
        $status = Invoke-RestMethod -Uri $backendStatusUrl -TimeoutSec 3
        if ($status.provider -ne 'mongodb' -or !$status.connected) { throw 'Backend is listening but MongoDB status is not healthy.' }
    } catch { throw "Port $BackendPort is occupied by another or unhealthy service; backend was not started. $_" }
} else {
    $commandShell = if ($env:ComSpec) { $env:ComSpec } else { 'cmd.exe' }
    Start-Process -FilePath $commandShell -ArgumentList @('/d', '/c', 'mvn', 'spring-boot:run') `
        -WorkingDirectory $backend -WindowStyle Hidden -PassThru `
        -RedirectStandardOutput (Join-Path $logDirectory 'backend.stdout.log') `
        -RedirectStandardError (Join-Path $logDirectory 'backend.stderr.log') | Out-Null
}

$siteUrl = "http://127.0.0.1:$SitePort"
$siteListener = Get-NetTCPConnection -State Listen -LocalPort $SitePort -ErrorAction SilentlyContinue | Select-Object -First 1
if ($siteListener) {
    try { Invoke-WebRequest -Uri "$siteUrl/index.html" -Method Head -TimeoutSec 3 | Out-Null }
    catch { throw "Port $SitePort is occupied by another or unhealthy site server. $_" }
} else {
    $python = Get-Command python -ErrorAction Stop
    Start-Process -FilePath $python.Source -ArgumentList @('-m', 'http.server', [string]$SitePort, '--bind', '127.0.0.1') `
        -WorkingDirectory $workspace -WindowStyle Hidden -PassThru `
        -RedirectStandardOutput (Join-Path $logDirectory 'site.stdout.log') `
        -RedirectStandardError (Join-Path $logDirectory 'site.stderr.log') | Out-Null
}

$healthy = $false
for ($attempt = 0; $attempt -lt 90; $attempt++) {
    try {
        $status = Invoke-RestMethod -Uri $backendStatusUrl -TimeoutSec 2
        if ($status.provider -eq 'mongodb' -and $status.connected) { $healthy = $true; break }
    } catch { }
    Start-Sleep -Seconds 1
}
if (!$healthy) { throw "Backend did not become healthy. Check logs in $logDirectory." }
try { Invoke-WebRequest -Uri "$siteUrl/index.html" -Method Head -TimeoutSec 5 | Out-Null }
catch { throw "Website server did not become available at $siteUrl. Check logs in $logDirectory." }

Write-Output "MongoDB backend is ready at $backendUrl."
Write-Output "Website is ready at $siteUrl."
Write-Output 'MongoDB Compass: mongodb://127.0.0.1:27017 → motelona_portfolio.'
Write-Output "Application logs: $logDirectory"
