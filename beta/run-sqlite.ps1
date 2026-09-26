$ErrorActionPreference = 'Stop'

$jdkBase = Join-Path $PSScriptRoot '..\.tools\jdk21'
$jdk = Get-ChildItem $jdkBase -Directory -ErrorAction SilentlyContinue | Select-Object -First 1
if ($null -eq $jdk) {
    throw "Java 21 portátil não foi encontrado em $jdkBase. Configure JAVA_HOME para um JDK 21."
}

$env:JAVA_HOME = $jdk.FullName
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
$env:SPRING_PROFILES_ACTIVE = 'sqlite'

$userHome = [Environment]::GetFolderPath('UserProfile')
if ([string]::IsNullOrWhiteSpace($userHome)) { $userHome = $HOME }
$env:MAVEN_OPTS = "-Duser.home=$userHome"

$mavenHome = Join-Path $userHome '.m2\wrapper\dists'
$maven = Get-ChildItem $mavenHome -Filter 'mvn.cmd' -Recurse -ErrorAction SilentlyContinue |
    Select-Object -First 1
if ($null -eq $maven) {
    throw "O Maven do wrapper ainda não foi baixado. Execute .\mvnw.cmd -v uma vez e rode este script novamente."
}

Write-Host 'Iniciando Marquify com SQLite em http://localhost:8080 ...'
& $maven.FullName spring-boot:run
