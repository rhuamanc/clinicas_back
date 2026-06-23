$ErrorActionPreference = 'Stop'

# Fuerza JDK 21 solo para esta ejecucion
$jdk21 = 'C:\Program Files\Microsoft\jdk-21.0.1.12-hotspot'
if (-not (Test-Path $jdk21)) {
    Write-Error "No se encontro JDK 21 en: $jdk21"
}

$env:JAVA_HOME = $jdk21
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

$mvnCmd = Get-Command mvn -ErrorAction SilentlyContinue
if ($mvnCmd) {
    & mvn spring-boot:run
    exit $LASTEXITCODE
}

$embeddedMvn = 'C:\Users\xp66925\.vscode\extensions\.8d66f13d-6663-4649-9841-45b950e52754\nbcode\java\maven\bin\mvn.cmd'
if (Test-Path $embeddedMvn) {
    Write-Host "mvn no encontrado en PATH. Usando Maven embebido de VS Code..."
    & $embeddedMvn spring-boot:run
    exit $LASTEXITCODE
}

Write-Error "No se encontro Maven. Instala Maven o ajusta la ruta de Maven embebido en run-backend.ps1"
