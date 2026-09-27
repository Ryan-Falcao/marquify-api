$ErrorActionPreference = 'Stop'

$jdkBase = Join-Path $PSScriptRoot '..\.tools\jdk21'
$jdk = Get-ChildItem $jdkBase -Directory -ErrorAction SilentlyContinue | Select-Object -First 1
if ($null -eq $jdk) { throw "Java 21 portátil não foi encontrado em $jdkBase." }

$envFile = Join-Path $PSScriptRoot '.env'
if (-not (Test-Path $envFile)) { throw 'Arquivo .env não encontrado. Execute .\start-postgres.ps1 primeiro.' }
Get-Content $envFile | Where-Object { $_ -match '^[A-Z_]+=' } | ForEach-Object {
    $name, $value = $_ -split '=', 2
    Set-Item "Env:$name" $value
}

$postgresPort = if ($env:POSTGRES_PORT) { [int]$env:POSTGRES_PORT } else { 5432 }
if (-not (Test-NetConnection -ComputerName 'localhost' -Port $postgresPort -InformationLevel Quiet -WarningAction SilentlyContinue)) {
    throw "PostgreSQL não está acessível em localhost:${postgresPort}. Execute .\start-postgres.ps1 depois de abrir o Docker Desktop."
}

$env:JAVA_HOME = $jdk.FullName
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
$env:SPRING_PROFILES_ACTIVE = 'postgres'
$userHome = [Environment]::GetFolderPath('UserProfile')
$env:MAVEN_OPTS = "-Duser.home=$userHome"
$maven = Get-ChildItem (Join-Path $userHome '.m2\wrapper\dists') -Filter 'mvn.cmd' -Recurse -ErrorAction SilentlyContinue | Select-Object -First 1
if ($null -eq $maven) { throw 'Maven do wrapper não encontrado. Execute .\mvnw.cmd -v uma vez.' }

Push-Location $PSScriptRoot
try {
    # Sem o restart do DevTools, falhas de inicialização retornam erro ao Maven.
    & $maven.FullName '-Dspring-boot.run.jvmArguments=-Dspring.devtools.restart.enabled=false' spring-boot:run
    if ($LASTEXITCODE -ne 0) { throw "A API encerrou com erro (código $LASTEXITCODE). Consulte o log acima." }
} finally {
    Pop-Location
}
