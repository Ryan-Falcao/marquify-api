param([Parameter(Mandatory=$true)][string]$BackupDirectory)
$ErrorActionPreference = 'Stop'
$backup = (Resolve-Path -LiteralPath $BackupDirectory).Path
if (-not (Test-Path -LiteralPath (Join-Path $backup 'COMPLETE'))) { throw 'Backup incompleto.' }
$checks = Get-Content -LiteralPath (Join-Path $backup 'checksums.json') -Raw | ConvertFrom-Json
foreach ($entry in $checks) {
    $file = [IO.Path]::GetFullPath((Join-Path $backup $entry.Path))
    if (-not $file.StartsWith($backup + [IO.Path]::DirectorySeparatorChar, [StringComparison]::OrdinalIgnoreCase)) { throw 'Caminho inválido no manifesto.' }
    if ((Get-FileHash -LiteralPath $file -Algorithm SHA256).Hash -ne $entry.SHA256) { throw "Checksum inválido: $($entry.Path)" }
}
$name = 'marquify-restore-' + [guid]::NewGuid().ToString('N').Substring(0,12)
function Docker { & docker.exe @args; if ($LASTEXITCODE -ne 0) { throw 'Falha no ensaio de restauração.' } }
$created = $false
try {
    Docker run -d --name $name -e POSTGRES_PASSWORD=restore-test-only -e POSTGRES_DB=restore_test postgres:17-alpine
    $created = $true
    $ready = $false
    for ($i=0; $i -lt 30; $i++) {
        & docker.exe exec $name pg_isready -U postgres -d restore_test *> $null
        if ($LASTEXITCODE -eq 0) { $ready=$true; break }
        Start-Sleep -Seconds 1
    }
    if (-not $ready) { throw 'PostgreSQL de restauração indisponível.' }
    Docker cp (Join-Path $backup 'database.dump') "${name}:/tmp/restore.dump"
    Docker exec $name pg_restore -U postgres -d restore_test --no-owner --no-acl --exit-on-error /tmp/restore.dump
    Docker exec $name psql -U postgres -d restore_test -v ON_ERROR_STOP=1 -c 'SELECT count(*) AS agendamentos FROM agendamentos;'
    Write-Host 'Dump restaurado em banco isolado e checksums das imagens verificados. Produção não foi alterada.'
} finally {
    if ($created) { Docker rm -f -v $name }
}
