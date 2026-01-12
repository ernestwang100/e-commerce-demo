#!/bin/bash
# Fix Deployment script
# 1. Fix Redis password startup
# 2. Update Backend with full internal FQDNs and dummy Kafka

RESOURCE_GROUP="shopping-rg"
ENV_NAME="shopping-env"
REDIS_PASSWORD="RedisCache2026!"
DB_PASSWORD="ShoppingDB2026!"

echo "=== Fixing Redis Password Configuration ==="
az containerapp update \
  --name shopping-redis \
  --resource-group $RESOURCE_GROUP \
  --set-env-vars "REDIS_PASSWORD=$REDIS_PASSWORD" \
  --command "/bin/sh" "-c" "redis-server --requirepass $REDIS_PASSWORD"

# Get the internal FQDNs again to be sure
MYSQL_URL=$(az containerapp show --name shopping-mysql --resource-group $RESOURCE_GROUP --query properties.configuration.ingress.fqdn -o tsv)
REDIS_URL=$(az containerapp show --name shopping-redis --resource-group $RESOURCE_GROUP --query properties.configuration.ingress.fqdn -o tsv)

echo "MySQL Internal: $MYSQL_URL"
echo "Redis Internal: $REDIS_URL"

echo "=== Updating Backend Configuration ==="
# Set spring.kafka.listener.auto-startup=false to prevent startup crash if Kafka is missing
# Also ensuring all properties map correctly to Spring Boot expectations
az containerapp update \
  --name shopping-backend \
  --resource-group $RESOURCE_GROUP \
  --set-env-vars \
    "SPRING_DATASOURCE_URL=jdbc:mysql://${MYSQL_URL}:3306/shopping_app?useSSL=false&allowPublicKeyRetrieval=true" \
    "SPRING_DATASOURCE_USERNAME=shopuser" \
    "SPRING_DATASOURCE_PASSWORD=secretref:db-password" \
    "SPRING_DATA_REDIS_HOST=${REDIS_URL}" \
    "SPRING_DATA_REDIS_PORT=6379" \
    "SPRING_DATA_REDIS_PASSWORD=secretref:redis-password" \
    "spring.data.redis.ssl.enabled=false" \
    "SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092" \
    "spring.kafka.listener.auto-startup=false" \
    "ALLOWED_ORIGINS=*"

echo "=== Restarting Backend ==="
az containerapp revision restart --name shopping-backend --resource-group $RESOURCE_GROUP
