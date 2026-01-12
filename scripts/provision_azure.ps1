# Azure Provisioning Script for Shopping App
# Usage: .\provision_azure.ps1

$ErrorActionPreference = 'Stop'

$RESOURCE_GROUP = "shopping-rg"
$LOCATION = "eastus"
$ACR_NAME = "shoppingregistry$(Get-Random -Minimum 1000 -Maximum 9999)"
$ENVIRONMENT_NAME = "shopping-env"
$BACKEND_APP_NAME = "shopping-backend"
$FRONTEND_APP_NAME = "shopping-frontend"

Write-Host "--- Starting Azure Provisioning ---" -ForegroundColor Cyan

# 0. Pre-requisite Checks
if (!(az account show)) {
    Write-Host "Please run 'az login' first." -ForegroundColor Red
    exit
}

if (!(Get-Command docker -ErrorAction SilentlyContinue)) {
    Write-Host "Docker is not installed or not in PATH." -ForegroundColor Red
    exit
}

# 1. Create Resource Group
Write-Host "Creating Resource Group: $RESOURCE_GROUP..."
az group create --name $RESOURCE_GROUP --location $LOCATION

# 2. Create ACR
Write-Host "Creating Azure Container Registry: $ACR_NAME..."
az acr create --resource-group $RESOURCE_GROUP --name $ACR_NAME --sku Basic --admin-enabled true

$LOGIN_SERVER = cmd /c "az acr show --name $ACR_NAME --query loginServer --output tsv"
# Trim any potential whitespace from login server
$LOGIN_SERVER = $LOGIN_SERVER.Trim()

# 3. Login to ACR (for Docker push)
Write-Host "Logging into ACR..."
az acr login --name $ACR_NAME

# 4. Build and Push Backend
Write-Host "Building and Pushing Backend Image..."
docker build -t "$LOGIN_SERVER/backend:latest" ./backend
docker push "$LOGIN_SERVER/backend:latest"

# 5. Build and Push Frontend
Write-Host "Building and Pushing Frontend Image..."
docker build -t "$LOGIN_SERVER/frontend:latest" ./frontend
docker push "$LOGIN_SERVER/frontend:latest"

# 6. Create ACA Environment
Write-Host "Creating Container App Environment..."
az containerapp env create --name $ENVIRONMENT_NAME --resource-group $RESOURCE_GROUP --location $LOCATION

# Get ACR Credentials for Internal Pull
$ACR_USERNAME = cmd /c "az acr credential show --name $ACR_NAME --query username --output tsv"
$ACR_PASSWORD = cmd /c "az acr credential show --name $ACR_NAME --query passwords[0].value --output tsv"

# 7. Deploy Backend
Write-Host "Deploying Backend Container App..."
az containerapp create `
    --name $BACKEND_APP_NAME `
    --resource-group $RESOURCE_GROUP `
    --environment $ENVIRONMENT_NAME `
    --image "$LOGIN_SERVER/backend:latest" `
    --target-port 7070 `
    --ingress 'external' `
    --registry-server $LOGIN_SERVER `
    --registry-username $ACR_USERNAME `
    --registry-password $ACR_PASSWORD `
    --env-vars "ALLOWED_ORIGINS=*"

$BACKEND_URL = cmd /c "az containerapp show --name $BACKEND_APP_NAME --resource-group $RESOURCE_GROUP --query properties.configuration.ingress.fqdn --output tsv"
Write-Host "Backend deployed at: https://$BACKEND_URL" -ForegroundColor Green

# 8. Deploy Frontend
Write-Host "Deploying Frontend Container App..."
az containerapp create `
    --name $FRONTEND_APP_NAME `
    --resource-group $RESOURCE_GROUP `
    --environment $ENVIRONMENT_NAME `
    --image "$LOGIN_SERVER/frontend:latest" `
    --target-port 80 `
    --ingress 'external' `
    --registry-server $LOGIN_SERVER `
    --registry-username $ACR_USERNAME `
    --registry-password $ACR_PASSWORD

$FRONTEND_URL = cmd /c "az containerapp show --name $FRONTEND_APP_NAME --resource-group $RESOURCE_GROUP --query properties.configuration.ingress.fqdn --output tsv"
Write-Host "Frontend deployed at: https://$FRONTEND_URL" -ForegroundColor Green

Write-Host "--- Provisioning Complete ---" -ForegroundColor Cyan
Write-Host "Note: You will need to manually configure DB, Redis, and Kafka connection strings in the Backend environment variables." -ForegroundColor Yellow
