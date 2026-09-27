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
    docker compose up -d --wait
    if ($LASTEXITCODE -ne 0) {
        throw 'Não foi possível iniciar o PostgreSQL. Verifique se o Docker Desktop está aberto e com o mecanismo Linux em execução.'
    }

    docker compose ps
    if ($LASTEXITCODE -ne 0) {
        throw 'O Docker não conseguiu consultar o status do PostgreSQL.'
    }

    Write-Host 'PostgreSQL iniciado. Agora execute .\run-postgres.ps1 para subir a API.' -ForegroundColor Green
} finally {
    Pop-Location
}
