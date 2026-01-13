#!/bin/bash
BACKEND_URL="https://shopping-backend.greengrass-56c2de65.eastus.azurecontainerapps.io"

echo "Checking admin/123..."
curl -s -X POST "${BACKEND_URL}/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "123"}'
echo ""

echo "Checking admin/admin123..."
curl -s -X POST "${BACKEND_URL}/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
echo ""

echo "Creating superadmin/123..."
curl -s -X POST "${BACKEND_URL}/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "superadmin",
    "password": "123",
    "email": "superadmin@shop.com",
    "role": "ADMIN"
  }'
echo ""
