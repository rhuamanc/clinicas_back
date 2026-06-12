$ErrorActionPreference = 'Stop'

# Fuerza JDK 21 solo para esta ejecucion
$jdk21 = 'C:\Program Files\Microsoft\jdk-21.0.1.12-hotspot'
if (-not (Test-Path $jdk21)) {
    Write-Error "No se encontro JDK 21 en: $jdk21"
}

$env:JAVA_HOME = $jdk21
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

mvn spring-boot:run
