param([switch]$SkipBuild)
$ErrorActionPreference = 'Stop'
$BackendDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $BackendDir

# Load a private .env file if present. Values are assigned only to this PowerShell process.
$EnvFile = Join-Path $BackendDir '.env'
if (Test-Path $EnvFile) {
    Get-Content $EnvFile | ForEach-Object {
        $line = $_.Trim()
        if ($line -and -not $line.StartsWith('#') -and $line.Contains('=')) {
            $parts = $line.Split('=', 2)
            $name = $parts[0].Trim()
            $value = $parts[1].Trim()
            if ($name -match '^[A-Za-z_][A-Za-z0-9_]*$') { [Environment]::SetEnvironmentVariable($name, $value, 'Process') }
        }
    }
}

if (-not $env:MYSQL_PASSWORD) { throw 'MYSQL_PASSWORD is empty. Set it in .env or your environment.' }
Write-Host 'MoteLona Local NLP is API-free; no external AI key is needed.'
if (-not $SkipBuild) { .\mvnw.cmd clean verify }
.\mvnw.cmd spring-boot:run
