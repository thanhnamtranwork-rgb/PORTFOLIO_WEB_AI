[CmdletBinding()]
param(
    [string]$MongoInstallRoot = (Join-Path $env:LOCALAPPDATA 'Programs\MongoDB\Server'),
    [string]$MongoDataPath = (Join-Path $env:LOCALAPPDATA 'MongoDB\data\db'),
    [int]$Port = 27017
)

$ErrorActionPreference = 'Stop'
$logDirectory = Join-Path $env:LOCALAPPDATA 'MongoDB\log'
$logPath = Join-Path $logDirectory 'mongod.log'
$listener = Get-NetTCPConnection -State Listen -LocalPort $Port -ErrorAction SilentlyContinue | Select-Object -First 1
if ($listener) {
    $runningProcess = Get-Process -Id $listener.OwningProcess -ErrorAction SilentlyContinue
    if ($runningProcess -and $runningProcess.ProcessName -eq 'mongod') {
        Write-Output "MongoDB is already listening on 127.0.0.1:$Port."
        return
    }
    throw "Port $Port is already used by another process. MongoDB was not started."
}

$searchRoots = @($MongoInstallRoot, (Join-Path $env:ProgramFiles 'MongoDB\Server')) |
    Where-Object { $_ -and (Test-Path -LiteralPath $_) }
$mongod = $searchRoots | ForEach-Object {
    Get-ChildItem -LiteralPath $_ -Filter mongod.exe -File -Recurse -ErrorAction SilentlyContinue
} | ForEach-Object {
    $match = [regex]::Match($_.FullName, '\\Server\\(\d+(?:\.\d+)+)\\')
    $version = if ($match.Success) { try { [version]::Parse($match.Groups[1].Value) } catch { [version]'0.0' } } else { [version]'0.0' }
    [pscustomobject]@{ File = $_; Version = $version }
} | Sort-Object Version -Descending | Select-Object -First 1 -ExpandProperty File
if (!$mongod) {
    throw "mongod.exe was not found. Install MongoDB Community Server or pass -MongoInstallRoot. Compass alone does not include the server."
}

New-Item -ItemType Directory -Path $MongoDataPath, $logDirectory -Force | Out-Null
$arguments = @(
    '--dbpath', ('"{0}"' -f $MongoDataPath),
    '--bind_ip', '127.0.0.1',
    '--port', [string]$Port,
    '--logpath', ('"{0}"' -f $logPath),
    '--logappend'
)
$process = Start-Process -FilePath $mongod.FullName -ArgumentList $arguments -WindowStyle Hidden -PassThru

for ($attempt = 0; $attempt -lt 15; $attempt++) {
    Start-Sleep -Seconds 1
    $test = Test-NetConnection 127.0.0.1 -Port $Port -InformationLevel Quiet -WarningAction SilentlyContinue
    if ($test) {
        Write-Output "MongoDB is running on 127.0.0.1:$Port (PID $($process.Id))."
        Write-Output 'In Compass, connect to mongodb://127.0.0.1:27017.'
        return
    }
    if ($process.HasExited) {
        $tail = if (Test-Path -LiteralPath $logPath) { (Get-Content -LiteralPath $logPath -Tail 15) -join [Environment]::NewLine } else { 'No MongoDB log was created.' }
        throw "MongoDB exited before opening its port.`n$tail"
    }
}

throw "MongoDB did not begin listening on port $Port. Check $logPath."
