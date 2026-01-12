#!/bin/bash
# Azure Provisioning Script for Shopping App
# Usage: bash ./provision_azure.sh

set -e  # Exit on error

RESOURCE_GROUP="shopping-rg"
LOCATION="eastus"
ACR_NAME="shoppingregistry$((RANDOM % 9000 + 1000))"
ENVIRONMENT_NAME="shopping-env"
BACKEND_APP_NAME="shopping-backend"
FRONTEND_APP_NAME="shopping-frontend"

echo "--- Starting Azure Provisioning ---"

# 1. Create Resource Group
echo "Creating Resource Group: $RESOURCE_GROUP..."
az group create --name $RESOURCE_GROUP --location $LOCATION

# 2. Create ACR
echo "Creating Azure Container Registry: $ACR_NAME..."
az acr create --resource-group $RESOURCE_GROUP --name $ACR_NAME --sku Basic --admin-enabled true

LOGIN_SERVER=$(az acr show --name $ACR_NAME --query loginServer --output tsv)
echo "ACR Login Server: $LOGIN_SERVER"

# 3. Login to ACR (for Docker push)
echo "Logging into ACR..."
az acr login --name $ACR_NAME

# 4. Build and Push Backend
echo "Building and Pushing Backend Image..."
docker build -t "${LOGIN_SERVER}/backend:latest" ./backend
docker push "${LOGIN_SERVER}/backend:latest"

# 5. Build and Push Frontend
echo "Building and Pushing Frontend Image..."
docker build -t "${LOGIN_SERVER}/frontend:latest" ./frontend
docker push "${LOGIN_SERVER}/frontend:latest"

# 6. Create ACA Environment
echo "Creating Container App Environment..."
az containerapp env create --name $ENVIRONMENT_NAME --resource-group $RESOURCE_GROUP --location $LOCATION

# Get ACR Credentials for Internal Pull
ACR_USERNAME=$(az acr credential show --name $ACR_NAME --query username --output tsv)
ACR_PASSWORD=$(az acr credential show --name $ACR_NAME --query "passwords[0].value" --output tsv)

# 7. Deploy Backend
echo "Deploying Backend Container App..."
az containerapp create \
    --name $BACKEND_APP_NAME \
    --resource-group $RESOURCE_GROUP \
    --environment $ENVIRONMENT_NAME \
    --image "${LOGIN_SERVER}/backend:latest" \
    --target-port 7070 \
    --ingress external \
    --registry-server $LOGIN_SERVER \
    --registry-username $ACR_USERNAME \
    --registry-password $ACR_PASSWORD \
    --env-vars "ALLOWED_ORIGINS=*"

BACKEND_URL=$(az containerapp show --name $BACKEND_APP_NAME --resource-group $RESOURCE_GROUP --query properties.configuration.ingress.fqdn --output tsv)
echo "Backend deployed at: https://$BACKEND_URL"

# 8. Deploy Frontend
echo "Deploying Frontend Container App..."
az containerapp create \
    --name $FRONTEND_APP_NAME \
    --resource-group $RESOURCE_GROUP \
    --environment $ENVIRONMENT_NAME \
    --image "${LOGIN_SERVER}/frontend:latest" \
    --target-port 80 \
    --ingress external \
    --registry-server $LOGIN_SERVER \
    --registry-username $ACR_USERNAME \
    --registry-password $ACR_PASSWORD

FRONTEND_URL=$(az containerapp show --name $FRONTEND_APP_NAME --resource-group $RESOURCE_GROUP --query properties.configuration.ingress.fqdn --output tsv)
echo "Frontend deployed at: https://$FRONTEND_URL"

echo ""
echo "--- Provisioning Complete ---"
echo "Frontend: https://$FRONTEND_URL"
echo "Backend: https://$BACKEND_URL"
echo ""
echo "Note: You will need to configure DB, Redis, and Kafka connection strings in the Backend environment variables."
