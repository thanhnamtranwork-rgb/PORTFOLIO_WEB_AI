param()
$ErrorActionPreference = 'Stop'
$BackendDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $BackendDir

# Load .env without printing secrets.
$EnvFile = Join-Path $BackendDir '.env'
if (Test-Path $EnvFile) {
    Get-Content $EnvFile | ForEach-Object {
        $line = $_.Trim()
        if ($line -and -not $line.StartsWith('#') -and $line.Contains('=')) {
            $parts = $line.Split('=', 2)
            $name = $parts[0].Trim()
            if ($name -match '^[A-Za-z_][A-Za-z0-9_]*$') { [Environment]::SetEnvironmentVariable($name, $parts[1].Trim(), 'Process') }
        }
    }
}

$Port = if ($env:MOTELONA_PORT) { $env:MOTELONA_PORT } else { '8080' }
$Base = "http://127.0.0.1:$Port"
$Jar = Join-Path $BackendDir 'target/motelona-assistant-0.2.0-SNAPSHOT.jar'
$OutLog = Join-Path $BackendDir 'verify-backend.out.log'
$ErrLog = Join-Path $BackendDir 'verify-backend.err.log'
$Process = $null

try {
    .\mvnw.cmd clean verify
    if (-not (Test-Path $Jar)) { throw "Executable jar not found: $Jar" }

    $Process = Start-Process -FilePath 'java' -ArgumentList @('-jar', $Jar) -WorkingDirectory $BackendDir -PassThru -RedirectStandardOutput $OutLog -RedirectStandardError $ErrLog
    $Ready = $false
    for ($i = 0; $i -lt 60; $i++) {
        Start-Sleep -Seconds 1
        try {
            $Health = Invoke-RestMethod -Uri "$Base/api/ai/health" -TimeoutSec 3
            if ($Health.backend -eq 'up') { $Ready = $true; break }
        } catch { if ($Process.HasExited) { throw "Backend process exited. Check $OutLog and $ErrLog" } }
    }
    if (-not $Ready) { throw 'Backend did not become ready within 60 seconds.' }
    Write-Host 'HTTP backend health: passed' -ForegroundColor Green

    $Db = Invoke-RestMethod -Uri "$Base/api/portfolio/status" -TimeoutSec 5
    if (-not $Db.connected -or $Db.provider -ne 'mysql' -or $Db.database -ne 'PortfolioAI_DB') { throw 'MySQL status endpoint did not confirm the expected PortfolioAI_DB database.' }
    Write-Host "MySQL connection: passed ($($Db.database))" -ForegroundColor Green

    $PublicData = Invoke-RestMethod -Uri "$Base/api/portfolio/public" -TimeoutSec 8
    if (-not $PublicData.profile -or $null -eq $PublicData.education -or $null -eq $PublicData.projects) { throw 'Public Portfolio API did not return the expected profile/education/projects contract.' }
    Write-Host 'MySQL Portfolio read API: passed' -ForegroundColor Green

    $SearchBody = @{ query = 'portfolio'; websiteContext = 'This portfolio presents Java, MySQL and AI projects.'; language = 'en' } | ConvertTo-Json -Depth 5
    $Search = Invoke-RestMethod -Method Post -Uri "$Base/api/ai/search" -ContentType 'application/json' -Body $SearchBody -TimeoutSec 8
    if ($Search.source -ne 'website' -or $Search.count -lt 1) { throw 'Local website search did not return the expected result.' }
    Write-Host 'Local website search API: passed' -ForegroundColor Green

    $Health = Invoke-RestMethod -Uri "$Base/api/ai/health" -TimeoutSec 5
    if ($Health.backend -ne 'up' -or $Health.provider -ne 'java-local' -or $Health.apiKeyRequired -ne $false) { throw 'Local Java AI health response does not match the API-free engine contract.' }
    Write-Host "Local Java NLP engine ready; external API key required: $($Health.apiKeyRequired)." -ForegroundColor Green

    $MemorySession = 'qa-memory-' + [Guid]::NewGuid().ToString('N')
    $Content = 'MoteLona verification memory ' + [Guid]::NewGuid().ToString('N')
    $Saved = Invoke-RestMethod -Method Post -Uri "$Base/api/ai/memories" -ContentType 'application/json' -Body (@{ sessionId = $MemorySession; consent = $true; content = $Content } | ConvertTo-Json)
    if (-not $Saved.saved -or -not $Saved.item.id) { throw 'MySQL memory save API did not return a stored memory.' }
    $List = Invoke-RestMethod -Uri "$Base/api/ai/memories?sessionId=$MemorySession" -TimeoutSec 5
    if (-not ($List.items | Where-Object { $_.id -eq $Saved.item.id })) { throw 'MySQL memory list API did not return the saved record.' }
    $OtherSession = 'qa-isolation-' + [Guid]::NewGuid().ToString('N')
    $OtherList = Invoke-RestMethod -Uri "$Base/api/ai/memories?sessionId=$OtherSession" -TimeoutSec 5
    if ($OtherList.items | Where-Object { $_.id -eq $Saved.item.id }) { throw 'A memory leaked across two different session IDs.' }
    Invoke-RestMethod -Method Delete -Uri "$Base/api/ai/memories/$($Saved.item.id)?sessionId=$MemorySession" -TimeoutSec 5 | Out-Null
    $After = Invoke-RestMethod -Uri "$Base/api/ai/memories?sessionId=$MemorySession" -TimeoutSec 5
    if ($After.items | Where-Object { $_.id -eq $Saved.item.id }) { throw 'MySQL memory delete API did not remove the test record.' }
    Write-Host 'MySQL memory create/list/delete + session partition: passed' -ForegroundColor Green

    $Session = 'qa-' + [Guid]::NewGuid().ToString('N')
    $Request = @{ sessionId = $Session; message = 'Xin chào'; language = 'vi'; websiteContext = 'Portfolio assistant verification. Skills: Java; MySQL.'; historyEnabled = $true; memoryEnabled = $false; history = @() } | ConvertTo-Json -Depth 6
    $Chat = Invoke-RestMethod -Method Post -Uri "$Base/api/ai/chat" -ContentType 'application/json' -Body $Request -TimeoutSec 15
    if (-not $Chat.aiPowered -or $Chat.source -ne 'local-ai' -or [string]::IsNullOrWhiteSpace([string]$Chat.message)) { throw 'Local Java AI chat did not return a non-empty AI-powered response.' }
    $History = Invoke-RestMethod -Uri "$Base/api/ai/history/$Session" -TimeoutSec 5
    if ($History.count -lt 2) { throw 'Opted-in conversation history was not written to MySQL.' }
    Invoke-RestMethod -Method Delete -Uri "$Base/api/ai/history/$Session" -TimeoutSec 5 | Out-Null

    $ExplainRequest = @{ sessionId = 'qa-explain-' + [Guid]::NewGuid().ToString('N'); selectedText = 'TF-IDF là gì?'; level = 'basic'; language = 'vi'; websiteContext = 'MoteLona uses a local Java TF-IDF retrieval pipeline.' } | ConvertTo-Json -Depth 5
    $Explain = Invoke-RestMethod -Method Post -Uri "$Base/api/ai/explain" -ContentType 'application/json' -Body $ExplainRequest -TimeoutSec 15
    if (-not $Explain.aiPowered -or $Explain.source -ne 'local-ai' -or $Explain.message -notmatch 'TF-IDF') { throw 'Local Java explain did not return a glossary-backed explanation.' }
    Write-Host 'API-free chat + glossary explain + MySQL history: passed' -ForegroundColor Green

    Write-Host 'PASS: Maven verify, HTTP backend, MySQL Portfolio reads, local search, memory CRUD/session isolation, API-free chat and glossary explain passed.' -ForegroundColor Green
}
finally {
    if ($Process -and -not $Process.HasExited) { Stop-Process -Id $Process.Id -Force }
}
