$ErrorActionPreference = 'Stop'
$composeFile = Join-Path $PSScriptRoot 'compose.test.yml'
$keys = @('TEST_DB_URL','TEST_DB_USERNAME','TEST_DB_PASSWORD','TEST_FLYWAY_LOCATIONS','TEST_FLYWAY_SUFFIXES')
$previous = @{}
foreach ($key in $keys) { $previous[$key] = [Environment]::GetEnvironmentVariable($key, 'Process') }
try {
    docker compose -f $composeFile up -d --wait
    if ($LASTEXITCODE -ne 0) { throw 'Banco de teste não iniciou.' }
    $env:TEST_DB_URL = 'jdbc:postgresql://localhost:55432/marquify_test'
    $env:TEST_DB_USERNAME = 'marquify_test'
    $env:TEST_DB_PASSWORD = 'local-test-only'
    $env:TEST_FLYWAY_LOCATIONS = 'classpath:db/migration/postgresql'
    $env:TEST_FLYWAY_SUFFIXES = '.pgsql'
    Push-Location (Join-Path $PSScriptRoot '../beta')
    try {
        .\mvnw.cmd -B verify
        if ($LASTEXITCODE -ne 0) { throw 'Testes PostgreSQL falharam.' }
    } finally { Pop-Location }
} finally {
    docker compose -f $composeFile down
    foreach ($key in $keys) { [Environment]::SetEnvironmentVariable($key, $previous[$key], 'Process') }
}
