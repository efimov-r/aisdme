# Сценарии пульта отладки: каждый запускается на свежем приложении (gradlew run -PdebugCtl пересоздаёт тестовый проект).
# .\scripts\debug-scenario.ps1            — все сценарии из scripts\scenarios
# .\scripts\debug-scenario.ps1 theme tabs — только названные (имя файла без .txt)
# Выход: число упавших сценариев. Вывод упавшего сценария — подробно; лог приложения — build\debug-ctl\app.log.
param([Parameter(ValueFromRemainingArguments = $true)][string[]]$Names)

. "$PSScriptRoot\debug-common.ps1"
Set-Location $root

function Stop-App { # штатно через пульт; если не вышло — принудительно
    if (-not (Test-AppAlive)) { return }
    & "$PSScriptRoot\debug-ctl.ps1" 'close' | Out-Null
    for ($i = 0; $i -lt 100 -and (Test-AppAlive); $i++) { Start-Sleep -Milliseconds 100 }
    Get-AppProcs | ForEach-Object { Stop-Process -Id $_.ProcessId -Force }
}

# без имён — только основные сценарии; по имени находятся и сценарии из подпапок (known-bugs: падают, пока баг не починен)
$files = if ($Names) {
    Get-ChildItem "$PSScriptRoot\scenarios" -Filter *.txt -Recurse | Where-Object { $Names -contains $_.BaseName }
} else {
    Get-ChildItem "$PSScriptRoot\scenarios\*.txt"
}
if (-not $files) { Write-Output 'no scenarios found'; exit 1 }

$failed = 0
foreach ($file in $files) {
    Stop-App
    New-Item -ItemType Directory -Force (Join-Path $root 'build') | Out-Null
    Start-Process -FilePath "$root\gradlew.bat" -ArgumentList 'run', '-PdebugCtl' -WindowStyle Hidden `
        -RedirectStandardOutput "$root\build\debug-run.out" -RedirectStandardError "$root\build\debug-run.err" | Out-Null
    $started = Get-Date
    $result = & "$PSScriptRoot\debug-ctl.ps1" "@$($file.FullName)"
    $code = $LASTEXITCODE
    $secs = [int]((Get-Date) - $started).TotalSeconds
    if ($code -eq 0) {
        Write-Output "PASS $($file.BaseName) (${secs}s)"
    } else {
        $failed++
        Write-Output "FAIL $($file.BaseName) (${secs}s, exit $code)"
        Write-Output $result
    }
}
Stop-App
Write-Output "failed: $failed of $(@($files).Count)"
exit $failed