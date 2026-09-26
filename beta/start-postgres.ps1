$ErrorActionPreference = 'Stop'

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    throw 'Docker não foi encontrado. Abra o Docker Desktop e abra um novo PowerShell.'
}

if (-not (Test-Path "$PSScriptRoot\.env")) {
    Copy-Item "$PSScriptRoot\.env.example" "$PSScriptRoot\.env"
    throw 'O arquivo .env foi criado. Defina DB_PASSWORD e execute este comando novamente.'
}

Push-Location $PSScriptRoot
try {
    docker compose up -d
    docker compose ps
    Write-Host 'PostgreSQL iniciado. Agora execute .\run-postgres.ps1 para subir a API.' -ForegroundColor Green
} finally {
    Pop-Location
}
