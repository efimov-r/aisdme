# Пульт отладки: отправляет команды запущенному приложению (gradlew run -PdebugCtl) и ждёт ответ.
# Каждый аргумент — одна команда: .\scripts\debug-ctl.ps1 "click 80 250" "shot a"
# Аргумент вида @путь\к\файлу.txt — команды из файла (по одной в строке; строки с # пропускаются).
# Выход: 0 — все команды выполнены, 1 — команда вернула error, 2 — нет команд, 3 — приложение не запущено, 4 — нет ответа.
param([Parameter(ValueFromRemainingArguments = $true)][string[]]$Commands)

. "$PSScriptRoot\debug-common.ps1"
$timeoutSec = 180 # с запасом на сборку, если приложение ещё стартует

$Commands = @($Commands | ForEach-Object {
    if ($_ -like '@*') { Get-Content -Encoding UTF8 $_.Substring(1) } else { $_ }
})

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
exit 0 # явно: иначе у вызывающего остаётся прежний $LASTEXITCODE