param([string]$EnvFile = (Join-Path $PSScriptRoot '.env'), [string]$OutputDirectory = (Join-Path $PSScriptRoot 'backups'))
$ErrorActionPreference = 'Stop'
$composeFile = Join-Path $PSScriptRoot 'compose.prod.yml'
$folder = Join-Path $OutputDirectory (Get-Date -Format 'yyyyMMdd-HHmmss-fff')
New-Item -ItemType Directory -Path $folder -ErrorAction Stop | Out-Null
$folder = (Resolve-Path -LiteralPath $folder).Path
function Compose { & docker compose --env-file $EnvFile -f $composeFile @args; if ($LASTEXITCODE -ne 0) { throw 'Comando Docker falhou; backup incompleto.' } }
$stopped = $false
try {
    # Stop writes while dumping DB and copying service images as a consistent set.
    Compose stop api
    $stopped = $true
    Compose exec -T db pg_dump -U marquify -d marquify -Fc -f /tmp/marquify-backup.dump
    Compose cp db:/tmp/marquify-backup.dump (Join-Path $folder 'database.dump')
    Compose cp api:/app/data/service-images (Join-Path $folder 'service-images')
    $files = Get-ChildItem -LiteralPath $folder -File -Recurse
    $files | ForEach-Object { [PSCustomObject]@{ Path = $_.FullName.Substring($folder.Length+1); SHA256 = (Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256).Hash } } | ConvertTo-Json | Set-Content (Join-Path $folder 'checksums.json')
    Set-Content (Join-Path $folder 'COMPLETE') ('Backup concluído em ' + (Get-Date -Format o))
    Write-Host "Backup completo: $folder"
} finally {
    if ($stopped) { Compose start api }
}
