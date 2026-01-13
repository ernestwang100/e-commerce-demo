#!/bin/bash
BACKEND_URL="https://shopping-backend.greengrass-56c2de65.eastus.azurecontainerapps.io"

echo "Creating realadmin/123..."
curl -s -X POST "${BACKEND_URL}/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "realadmin",
    "password": "123",
    "email": "realadmin@shop.com",
    "role": "ADMIN"
  }'
echo ""
