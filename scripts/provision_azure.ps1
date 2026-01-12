# Azure Provisioning Script for Shopping App
# Usage: .\provision_azure.ps1

$RESOURCE_GROUP = "shopping-rg"
$LOCATION = "eastus"
$ACR_NAME = "shoppingregistry$(Get-Random -Minimum 1000 -Maximum 9999)"
$ENVIRONMENT_NAME = "shopping-env"
$BACKEND_APP_NAME = "shopping-backend"
$FRONTEND_APP_NAME = "shopping-frontend"

Write-Host "--- Starting Azure Provisioning ---" -ForegroundColor Cyan

# 1. Login Check
if (!(az account show)) {
    Write-Host "Please run 'az login' first." -ForegroundColor Red
    exit
}

# 2. Create Resource Group
Write-Host "Creating Resource Group: $RESOURCE_GROUP..."
az group create --name $RESOURCE_GROUP --location $LOCATION

# 3. Create ACR
Write-Host "Creating Azure Container Registry: $ACR_NAME..."
az acr create --resource-group $RESOURCE_GROUP --name $ACR_NAME --sku Basic --admin-enabled true

$LOGIN_SERVER = "$(az acr show --name $ACR_NAME --query loginServer --output tsv)"

# 4. Build and Push Backend
Write-Host "Building and Pushing Backend Image..."
az acr build --registry $ACR_NAME --image backend:latest ./backend

# 5. Build and Push Frontend
Write-Host "Building and Pushing Frontend Image..."
az acr build --registry $ACR_NAME --image frontend:latest ./frontend

# 6. Create ACA Environment
Write-Host "Creating Container App Environment..."
az containerapp env create --name $ENVIRONMENT_NAME --resource-group $RESOURCE_GROUP --location $LOCATION

# 7. Deploy Backend
Write-Host "Deploying Backend Container App..."
az containerapp create `
    --name $BACKEND_APP_NAME `
    --resource-group $RESOURCE_GROUP `
    --environment $ENVIRONMENT_NAME `
    --image "$LOGIN_SERVER/backend:latest" `
    --target-port 7070 `
    --ingress 'external' `
    --query-encryption-secret "mysecret" `
    --env-vars "ALLOWED_ORIGINS=*" # Initially allow all, refine later

$BACKEND_URL = "$(az containerapp show --name $BACKEND_APP_NAME --resource-group $RESOURCE_GROUP --query properties.configuration.ingress.fqdn --output tsv)"
Write-Host "Backend deployed at: https://$BACKEND_URL" -ForegroundColor Green

# 8. Deploy Frontend
Write-Host "Deploying Frontend Container App..."
az containerapp create `
    --name $FRONTEND_APP_NAME `
    --resource-group $RESOURCE_GROUP `
    --environment $ENVIRONMENT_NAME `
    --image "$LOGIN_SERVER/frontend:latest" `
    --target-port 80 `
    --ingress 'external'

$FRONTEND_URL = "$(az containerapp show --name $FRONTEND_APP_NAME --resource-group $RESOURCE_GROUP --query properties.configuration.ingress.fqdn --output tsv)"
Write-Host "Frontend deployed at: https://$FRONTEND_URL" -ForegroundColor Green

Write-Host "--- Provisioning Complete ---" -ForegroundColor Cyan
Write-Host "Note: You will need to manually configure DB, Redis, and Kafka connection strings in the Backend environment variables." -ForegroundColor Yellow
