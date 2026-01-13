#!/bin/bash
# Seed Azure MySQL database via backend API
# This creates initial users and products for demo purposes

BACKEND_URL="https://shopping-backend.greengrass-56c2de65.eastus.azurecontainerapps.io"

echo "=== Seeding Azure Database via Backend API ==="
echo "Backend URL: $BACKEND_URL"
echo ""

# 1. Create Admin User
echo "Creating admin user..."
ADMIN_RESPONSE=$(curl -s -X POST "${BACKEND_URL}/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "hasauthadmin",
    "password": "admin123",
    "email": "hasauthadmin@shop.com",
    "role": "ADMIN"
  }')
echo "Admin created: $ADMIN_RESPONSE"

# 2. Create Regular User
echo "Creating regular user..."
USER_RESPONSE=$(curl -s -X POST "${BACKEND_URL}/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "ernest",
    "password": "123",
    "email": "ernest@shop.com",
    "role": "USER"
  }')
echo "User created: $USER_RESPONSE"

# 3. Login as Admin to get JWT token
echo ""
echo "Logging in as admin..."
LOGIN_RESPONSE=$(curl -s -X POST "${BACKEND_URL}/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "hasauthadmin",
    "password": "admin123"
  }')

# Extract JWT token (assuming response format: {"token":"xxx"})
JWT_TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ -z "$JWT_TOKEN" ]; then
    echo "Failed to get JWT token. Response: ${LOGIN_RESPONSE}"
    exit 1
fi

echo "JWT Token obtained"

# 4. Create Sample Products (Admin only)
echo ""
echo "Creating sample products..."

curl -v -X POST "${BACKEND_URL}/products" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "name": "Laptop Pro 15",
    "description": "High-performance laptop with 16GB RAM",
    "retailPrice": 1299.99,
    "wholesalePrice": 899.99,
    "quantity": 50
  }'

curl -v -X POST "${BACKEND_URL}/products" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "name": "Wireless Mouse",
    "description": "Ergonomic wireless mouse",
    "retailPrice": 29.99,
    "wholesalePrice": 15.99,
    "quantity": 200
  }'

curl -v -X POST "${BACKEND_URL}/products" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "name": "USB-C Hub",
    "description": "7-in-1 USB-C multiport adapter",
    "retailPrice": 49.99,
    "wholesalePrice": 25.99,
    "quantity": 150
  }'

curl -v -X POST "${BACKEND_URL}/products" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "name": "Mechanical Keyboard",
    "description": "RGB backlit mechanical gaming keyboard",
    "retailPrice": 89.99,
    "wholesalePrice": 49.99,
    "quantity": 75
  }'

curl -v -X POST "${BACKEND_URL}/products" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "name": "HD Webcam",
    "description": "1080p webcam with built-in microphone",
    "retailPrice": 69.99,
    "wholesalePrice": 39.99,
    "quantity": 100
  }'

echo ""
echo "=== Seeding Complete ==="
echo "Users created:"
echo "  - admin / admin123 (ADMIN)"
echo "  - ernest / 123 (USER)"
echo ""
echo "5 products created"
echo ""
echo "You can now access the frontend at:"
echo "https://shopping-frontend.greengrass-56c2de65.eastus.azurecontainerapps.io"
