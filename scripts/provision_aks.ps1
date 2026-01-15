# Configuration
$RESOURCE_GROUP = "rg-ecommerce-aks"
$LOCATION = "eastus"
$CLUSTER_NAME = "aks-ecommerce"

Write-Host "Starting AKS Provisioning..." -ForegroundColor Cyan

# 1. Create Resource Group
Write-Host "Creating Resource Group: $RESOURCE_GROUP..."
az group create --name $RESOURCE_GROUP --location $LOCATION

# 2. Create AKS Cluster
# Using --tier Free for cost savings suitable for demos/dev
Write-Host "Creating AKS Cluster: $CLUSTER_NAME (This may take several minutes)..."
az aks create --resource-group $RESOURCE_GROUP `
    --name $CLUSTER_NAME `
    --node-count 1 `
    --tier Free `
    --enable-addons monitoring `
    --generate-ssh-keys

# 3. Get Credentials
Write-Host "Getting AKS Credentials..."
az aks get-credentials --resource-group $RESOURCE_GROUP --name $CLUSTER_NAME --overwrite-existing

Write-Host "Provisioning Complete! 'kubectl' is now configured to connect to $CLUSTER_NAME." -ForegroundColor Green
kubectl get nodes
