# Azure Deployment & CI/CD Guide

This guide explains how to deploy your E-Commerce application to Azure and how to set up a CI/CD pipeline.

## 1. What is CI/CD?

**CI (Continuous Integration)**: Automation that builds and tests your code every time you push to your repository. It ensures that new changes don't break the existing application.
**CD (Continuous Deployment/Delivery)**: Automation that takes your successfully built code and deploys it to your environment (like Azure).

### Why use it?
- **Speed**: No manual uploads or complex commands.
- **Reliability**: If tests fail, the deployment is blocked, preventing bugs in production.
- **Consistency**: The deployment process is identical every time.

---

## 2. Azure Deployment Strategy

We recommend using **Azure Container Apps (ACA)**. It is a fully managed serverless container service that is perfect for multi-container applications like this one.

### Prerequisites
- An [Azure Account](https://azure.microsoft.com/free/).
- [Azure CLI](https://learn.microsoft.com/en-us/cli/azure/install-azure-cli) installed.
- [Docker](https://www.docker.com/products/docker-desktop/) installed locally.

### Step-by-Step Deployment

#### A. Resource Provisioning
Run these commands locally (or use the Azure Portal):

1.  **Create a Resource Group**:
    ```bash
    az group create --name shopping-rg --location eastus
    ```
2.  **Create Azure Container Registry (ACR)**:
    ```bash
    az acr create --resource-group shopping-rg --name shoppingregistry --sku Basic
    ```
3.  **Provisions Managed Services**:
    - **Database**: Create an "Azure Database for MySQL (Flexible Server)".
    - **Cache**: Create an "Azure Cache for Redis".
    - **Messaging**: Create an "Azure Event Hubs" namespace with Kafka enabled.

#### B. Push Images to ACR
```bash
# Log in to ACR
az acr login --name shoppingregistry

# Build and Push Backend
docker build -t shoppingregistry.azurecr.io/backend:latest ./backend
docker push shoppingregistry.azurecr.io/backend:latest

# Build and Push Frontend
docker build -t shoppingregistry.azurecr.io/frontend:latest ./frontend
docker push shoppingregistry.azurecr.io/frontend:latest
```

#### C. Create Azure Container Apps
1.  **Backend App**:
    - Create a Container App named `shopping-backend`.
    - Use the image from ACR.
    - Set environment variables: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `SPRING_DATA_REDIS_HOST`, etc.
    - Enable Ingress (Port 7070).

2.  **Frontend App**:
    - Create a Container App named `shopping-frontend`.
    - Use the image from ACR.
    - Enable Ingress (Port 80).

---

## 3. Setting up CI/CD with GitHub Actions

You can use GitHub Actions to automate the steps above.

### Workflow Example (`.github/workflows/deploy.yml`)

```yaml
name: Build and Deploy

on:
  push:
    branches: [ "master" ]

jobs:
  build-and-deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Log in to Azure
        uses: azure/login@v1
        with:
          creds: ${{ secrets.AZURE_CREDENTIALS }}

      - name: Build and Push Images to ACR
        run: |
          az acr login --name shoppingregistry
          docker build -t shoppingregistry.azurecr.io/backend:${{ github.sha }} ./backend
          docker push shoppingregistry.azurecr.io/backend:${{ github.sha }}
          # ... repeat for frontend

      - name: Deploy to Azure Container Apps
        uses: azure/container-apps-deploy-action@v1
        with:
          acrName: shoppingregistry
          containerAppName: shopping-backend
          imageToDeploy: shoppingregistry.azurecr.io/backend:${{ github.sha }}
```

### Steps to activate:
1.  **Azure Service Principal**: Create one via `az ad sp create-for-rbac` to get the `AZURE_CREDENTIALS`.
2.  **GitHub Secrets**: Add `AZURE_CREDENTIALS` to your GitHub repository secrets.
3.  **Commit the file**: Add the YAML file to `.github/workflows/` and push!

---

## 4. Hardcoded "localhost" Check
We already cleaned up the code to be cloud-ready:
- **Frontend**: Uses relative API paths.
- **Backend CORS**: Uses `ALLOWED_ORIGINS` env var.
- **Infrastructure**: All connection strings support overrides via environment variables.
