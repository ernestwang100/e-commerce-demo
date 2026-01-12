#!/bin/bash
# Deploy MySQL and Redis as Container Apps
# Usage: bash ./deploy_containerized_db.sh

set -e

RESOURCE_GROUP="shopping-rg"
LOCATION="eastus"
ENVIRONMENT_NAME="shopping-env"
BACKEND_APP_NAME="shopping-backend"

# Database credentials
DB_PASSWORD="ShoppingDB2026!"
REDIS_PASSWORD="RedisCache2026!"

echo "=== Deploying Containerized Database & Cache ==="

# 1. Deploy MySQL Container App
echo "Deploying MySQL Container App..."
az containerapp create \
    --name shopping-mysql \
    --resource-group $RESOURCE_GROUP \
    --environment $ENVIRONMENT_NAME \
    --image mysql:8.0 \
    --target-port 3306 \
    --ingress internal \
    --min-replicas 1 \
    --max-replicas 1 \
    --env-vars \
        "MYSQL_ROOT_PASSWORD=secretref:mysql-root-password" \
        "MYSQL_DATABASE=shopping_app" \
        "MYSQL_USER=shopuser" \
        "MYSQL_PASSWORD=secretref:mysql-password" \
    --secrets \
        "mysql-root-password=$DB_PASSWORD" \
        "mysql-password=$DB_PASSWORD"

MYSQL_URL=$(az containerapp show --name shopping-mysql --resource-group $RESOURCE_GROUP --query properties.configuration.ingress.fqdn --output tsv)
echo "MySQL deployed at: $MYSQL_URL:3306"

# 2. Deploy Redis Container App
echo "Deploying Redis Container App..."
az containerapp create \
    --name shopping-redis \
    --resource-group $RESOURCE_GROUP \
    --environment $ENVIRONMENT_NAME \
    --image redis:alpine \
    --target-port 6379 \
    --ingress internal \
    --min-replicas 1 \
    --max-replicas 1 \
    --command "/bin/sh" "-c" "redis-server --requirepass $REDIS_PASSWORD" \
    --secrets "redis-password=$REDIS_PASSWORD"

REDIS_URL=$(az containerapp show --name shopping-redis --resource-group $RESOURCE_GROUP --query properties.configuration.ingress.fqdn --output tsv)
echo "Redis deployed at: $REDIS_URL:6379"

# 3. Update Backend Configuration
echo "Configuring Backend to connect to MySQL and Redis..."

# Add/update secrets
az containerapp secret set \
    --name $BACKEND_APP_NAME \
    --resource-group $RESOURCE_GROUP \
    --secrets \
        "db-password=$DB_PASSWORD" \
        "redis-password=$REDIS_PASSWORD"

# Update environment variables with internal URLs
az containerapp update \
    --name $BACKEND_APP_NAME \
    --resource-group $RESOURCE_GROUP \
    --set-env-vars \
        "SPRING_DATASOURCE_URL=jdbc:mysql://${MYSQL_URL}:3306/shopping_app?useSSL=false&allowPublicKeyRetrieval=true" \
        "SPRING_DATASOURCE_USERNAME=shopuser" \
        "SPRING_DATASOURCE_PASSWORD=secretref:db-password" \
        "SPRING_DATA_REDIS_HOST=${REDIS_URL}" \
        "SPRING_DATA_REDIS_PORT=6379" \
        "SPRING_DATA_REDIS_PASSWORD=secretref:redis-password" \
        "spring.data.redis.ssl.enabled=false"

echo ""
echo "=== Deployment Complete ==="
echo "MySQL: $MYSQL_URL:3306"
echo "  Database: shopping_app"
echo "  User: shopuser"
echo ""
echo "Redis: $REDIS_URL:6379"
echo ""
echo "Backend has been configured to use these services."
echo "The backend container will restart automatically to apply changes."
echo ""
echo "Note: Data is ephemeral - will be lost if containers restart."
