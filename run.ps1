# Stopping any process on port 8081
Get-NetTCPConnection -LocalPort 8081 -ErrorAction SilentlyContinue | ForEach-Object { 
    Get-Process -Id $_.OwningProcess | Stop-Process -Force 
}
# Starting Pokedex Application
.\mvnw.cmd spring-boot:run
