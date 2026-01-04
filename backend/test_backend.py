import requests
import json

BASE_URL = "http://localhost:7070"

def test_auth_register():
    print("=== Testing Registration ===")
    url = f"{BASE_URL}/auth/register"
    data = {
        "username": "testuser",
        "email": "test@example.com",
        "password": "password123"
    }
    try:
        response = requests.post(url, json=data)
        print(f"Status: {response.status_code}")
        print(f"Response: {response.text}")
        return response.status_code == 200
    except requests.exceptions.ConnectionError:
        print("ERROR: Cannot connect to backend. Is it running?")
        return False

def test_auth_login():
    print("\n=== Testing Login ===")
    url = f"{BASE_URL}/auth/login"
    data = {
        "username": "testuser",
        "password": "password123"
    }
    try:
        response = requests.post(url, json=data)
        print(f"Status: {response.status_code}")
        print(f"Response: {response.text}")
        if response.status_code == 200:
            return response.json().get("token")
        return None
    except requests.exceptions.ConnectionError:
        print("ERROR: Cannot connect to backend. Is it running?")
        return None

def test_get_products(token):
    print("\n=== Testing Get Products ===")
    url = f"{BASE_URL}/user/products"
    headers = {"Authorization": f"Bearer {token}"} if token else {}
    try:
        response = requests.get(url, headers=headers)
        print(f"Status: {response.status_code}")
        print(f"Response: {response.text[:200]}")
        return response.status_code == 200
    except requests.exceptions.ConnectionError:
        print("ERROR: Cannot connect to backend. Is it running?")
        return False

def test_health():
    print("=== Testing Backend Health ===")
    try:
        response = requests.get(f"{BASE_URL}/user/products", timeout=5)
        print(f"Backend is UP! Status: {response.status_code}")
        return True
    except requests.exceptions.ConnectionError:
        print("ERROR: Backend is not responding on port 7070")
        return False

if __name__ == "__main__":
    if not test_health():
        print("\nPlease start the backend first!")
        exit(1)
    
    test_auth_register()
    token = test_auth_login()
    test_get_products(token)
    
    print("\n=== Tests Complete ===")
