# load-env.ps1
# This script loads environment variables from the .env file at the project root
# and then runs the backend application.

$envFile = Join-Path (Get-Item -Path $PSScriptRoot).Parent.FullName ".env"

if (Test-Path $envFile) {
    Write-Host "Loading environment variables from $envFile..." -ForegroundColor Cyan
    Get-Content $envFile | Where-Object { $_ -match '=' -and $_ -notmatch '^#' } | ForEach-Object {
        $name, $value = $_.Split('=', 2)
        [System.Environment]::SetEnvironmentVariable($name.Trim(), $value.Trim(), [System.EnvironmentVariableTarget]::Process)
    }
} else {
    Write-Error "No .env file found at $envFile. Please copy .env.example to .env and fill in the values."
    exit 1
}

Write-Host "Starting MediScan Backend..." -ForegroundColor Green
cd (Join-Path (Get-Item -Path $PSScriptRoot).Parent.FullName "backend")
./mvnw spring-boot:run
