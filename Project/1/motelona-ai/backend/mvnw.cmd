@echo off
setlocal
set "MAVEN_VERSION=3.10.0"
set "MAVEN_SHA512=22d31676d5b92ed53308c19ecded3245d0efd0cddfec51ec7bab62f4f13ad70ddddceff8e62940e0e8e2421f3cde8106737f4becec0aa034ffeff2a754e88337"
set "CACHE=%USERPROFILE%\.motelona"
set "MAVEN_HOME=%CACHE%\apache-maven-%MAVEN_VERSION%"
if exist "%MAVEN_HOME%\bin\mvn.cmd" goto runmaven
if not exist "%CACHE%" mkdir "%CACHE%"
set "ZIP=%CACHE%\apache-maven-%MAVEN_VERSION%-bin.zip"
set "PART=%ZIP%.part"
echo Downloading Apache Maven %MAVEN_VERSION% and verifying SHA-512 (first run only)...
powershell -NoProfile -ExecutionPolicy Bypass -Command "$ProgressPreference='SilentlyContinue'; $urls=@('https://dlcdn.apache.org/maven/maven-3/%MAVEN_VERSION%/binaries/apache-maven-%MAVEN_VERSION%-bin.zip','https://downloads.apache.org/maven/maven-3/%MAVEN_VERSION%/binaries/apache-maven-%MAVEN_VERSION%-bin.zip'); $downloaded=$false; foreach($u in $urls) { try { Invoke-WebRequest -Uri $u -OutFile '%PART%'; $downloaded=$true; break } catch { Remove-Item -LiteralPath '%PART%' -Force -ErrorAction SilentlyContinue } }; if(-not $downloaded) { Write-Error 'Could not download Maven from official mirrors.'; exit 1 }; $actual=(Get-FileHash -LiteralPath '%PART%' -Algorithm SHA512).Hash.ToLowerInvariant(); if($actual -ne '%MAVEN_SHA512%') { Remove-Item -LiteralPath '%PART%' -Force; Write-Error 'Maven SHA-512 verification failed; archive rejected.'; exit 1 }; Move-Item -LiteralPath '%PART%' -Destination '%ZIP%' -Force; Expand-Archive -LiteralPath '%ZIP%' -DestinationPath '%CACHE%' -Force" || goto downloadfailed
if not exist "%MAVEN_HOME%\bin\mvn.cmd" goto downloadfailed
:runmaven
call "%MAVEN_HOME%\bin\mvn.cmd" %*
exit /b %ERRORLEVEL%
:downloadfailed
echo Maven could not be safely downloaded or extracted. Check Internet access and retry.
exit /b 1
