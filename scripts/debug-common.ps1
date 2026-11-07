# Общее для скриптов пульта отладки. Подключается через dot-source: . "$PSScriptRoot\debug-common.ps1"
# Сообщения — по-английски: PowerShell 5.1 читает .ps1 без BOM как ANSI, русские строки испортились бы.

$utf8 = New-Object System.Text.UTF8Encoding $false
[Console]::OutputEncoding = $utf8
$root = Split-Path $PSScriptRoot -Parent
$dir = Join-Path $root 'build\debug-ctl'

function Get-AppProcs { # только наши: приложение с пультом или gradle, который его собирает; чужие запуски не трогаем
    Get-CimInstance Win32_Process -Filter "Name='java.exe'" | Where-Object {
        ($_.CommandLine -like '*non.era.aisdme.MainKt*' -and $_.CommandLine -like '*aisdme.debug.dir*') -or
        ($_.CommandLine -like '*GradleWrapperMain*' -and $_.CommandLine -like '* run *' -and $_.CommandLine -like '*-PdebugCtl*')
    }
}

function Test-AppAlive { return [bool](Get-AppProcs) }