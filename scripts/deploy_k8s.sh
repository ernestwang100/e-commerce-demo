#!/bin/bash

# Load env vars from backend/.env if it exists
if [ -f "backend/.env" ]; then
    echo "Loading secrets from backend/.env..."
    set -a
    source backend/.env
    set +a
fi

# Ensure required environment variables are set
if [ -z "$GEMINI_API_KEY" ]; then
    echo "Error: GEMINI_API_KEY is not set. Please export it first."
    exit 1
fi

echo "Creating Kubernetes Secrets..."
kubectl create secret generic app-secrets \
    --from-literal=mysql-root-password='password' \
    --from-literal=mysql-password='password' \
    --from-literal=redis-password='password' \
    --from-literal=gemini-api-key="$GEMINI_API_KEY" \
    --dry-run=client -o yaml | kubectl apply -f -

echo "Deploying Services..."
kubectl apply -f k8s/

echo "Deployment submitted! Check status with: kubectl get pods"
