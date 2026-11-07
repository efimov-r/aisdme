# Пульт отладки: отправляет команды запущенному приложению (gradlew run -PdebugCtl) и ждёт ответ.
# Каждый аргумент — одна команда: .\scripts\debug-ctl.ps1 "click 80 250" "shot a"
# Выход: 0 — все команды выполнены, 1 — команда вернула error, 2 — нет команд, 3 — приложение не запущено, 4 — нет ответа.
# Сообщения скрипта — по-английски: PowerShell 5.1 читает .ps1 без BOM как ANSI, русские строки испортились бы.
param([Parameter(ValueFromRemainingArguments = $true)][string[]]$Commands)

$utf8 = New-Object System.Text.UTF8Encoding $false
[Console]::OutputEncoding = $utf8
$dir = Join-Path (Split-Path $PSScriptRoot -Parent) 'build\debug-ctl'
$timeoutSec = 180 # с запасом на сборку, если приложение ещё стартует

function Test-AppAlive { # само приложение или gradle, который его собирает и запускает
    $procs = Get-CimInstance Win32_Process -Filter "Name='java.exe'" | Where-Object {
        $_.CommandLine -like '*non.era.aisdme.MainKt*' -or
        ($_.CommandLine -like '*GradleWrapperMain*' -and $_.CommandLine -like '* run *')
    }
    return [bool]$procs
}

if (-not $Commands) { Write-Output 'no commands given'; exit 2 }
if (-not (Test-AppAlive)) { Write-Output 'app is not running: start it in background with .\gradlew.bat run -PdebugCtl'; exit 3 }

New-Item -ItemType Directory -Force $dir | Out-Null
$id = (Get-Date -Format 'HHmmssfff') + '-' + (Get-Random -Maximum 999)
[System.IO.File]::WriteAllText("$dir\$id.tmp", (($Commands -join "`n") + "`n"), $utf8) # без BOM
Move-Item "$dir\$id.tmp" "$dir\$id.cmd" # приложение берёт только готовый .cmd

$out = "$dir\$id.out"
$deadline = (Get-Date).AddSeconds($timeoutSec)
$ticks = 0
while (-not (Test-Path $out)) {
    if ((Get-Date) -gt $deadline) { Write-Output "no answer in $timeoutSec s"; exit 4 }
    if ((++$ticks % 50 -eq 0) -and -not (Test-AppAlive)) { Write-Output 'app exited, no answer will come'; exit 3 }
    Start-Sleep -Milliseconds 100
}

$text = [System.IO.File]::ReadAllText($out, $utf8)
Remove-Item $out
Write-Output $text
if ($text -match '-> error') { exit 1 }