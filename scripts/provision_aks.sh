#!/bin/bash

# Configuration
RESOURCE_GROUP="rg-ecommerce-aks"
LOCATION="eastus"
CLUSTER_NAME="aks-ecommerce"

echo -e "\033[0;36mStarting AKS Provisioning...\033[0m"

# 1. Create Resource Group
echo "Creating Resource Group: $RESOURCE_GROUP..."
az group create --name $RESOURCE_GROUP --location $LOCATION

# 2. Create AKS Cluster
# Using --tier Free for cost savings suitable for demos/dev
echo "Creating AKS Cluster: $CLUSTER_NAME (This may take several minutes)..."
az aks create --resource-group $RESOURCE_GROUP \
    --name $CLUSTER_NAME \
    --node-count 1 \
    --tier Free \
    --node-vm-size Standard_B2s \
    --enable-addons monitoring \
    --generate-ssh-keys

# 3. Get Credentials
echo "Getting AKS Credentials..."
az aks get-credentials --resource-group $RESOURCE_GROUP --name $CLUSTER_NAME --overwrite-existing

echo -e "\033[0;32mProvisioning Complete! 'kubectl' is now configured to connect to $CLUSTER_NAME.\033[0m"
kubectl get nodes
