import requests
import json

BASE_URL = "http://localhost:7070"

def test_endpoints():
    print("=" * 60)
    print("API ENDPOINT TESTING (POSTMAN COMPATIBLE)")
    print("=" * 60)

    # 1. Login
    print("\n[1] LOGIN TEST")
    print("-" * 40)
    login_payload = {"username": "admin", "password": "123"}
    try:
        login_resp = requests.post(f"{BASE_URL}/login", json=login_payload)
        print(f"Status: {login_resp.status_code}")
        print(f"Response: {login_resp.text[:500]}")
        
        if login_resp.status_code != 200:
            print("LOGIN FAILED - Cannot proceed with authenticated tests")
            return
        
        data = login_resp.json()
        token = data.get("token")
        headers = {"Authorization": f"Bearer {token}"}
    except Exception as e:
        print(f"Exception: {e}")
        return

    # 2. Products (Public)
    print("\n[2] PRODUCTS TEST (Public)")
    print("-" * 40)
    try:
        resp = requests.get(f"{BASE_URL}/products/all")
        print(f"GET /products/all - Status: {resp.status_code}")
    except Exception as e:
        print(f"Exception: {e}")

    # 3. Orders (Protected)
    print("\n[3] ORDERS TEST (Protected)")
    print("-" * 40)
    try:
        resp = requests.get(f"{BASE_URL}/orders/all", headers=headers)
        print(f"GET /orders/all - Status: {resp.status_code}")
    except Exception as e:
        print(f"Exception: {e}")

    # 4. Watchlist (Protected)
    print("\n[4] WATCHLIST TEST (Protected)")
    print("-" * 40)
    try:
        resp = requests.get(f"{BASE_URL}/watchlist/products/all", headers=headers)
        print(f"GET /watchlist/products/all - Status: {resp.status_code}")
    except Exception as e:
        print(f"Exception: {e}")

    # 5. Stats (Protected - Multi-endpoint)
    print("\n[5] STATS TEST (Protected)")
    print("-" * 40)
    endpoints = [
        "/products/recent/3",
        "/products/frequent/3",
        "/products/popular/3",
        "/products/profit/3"
    ]
    for ep in endpoints:
        try:
            resp = requests.get(f"{BASE_URL}{ep}", headers=headers)
            print(f"GET {ep} - Status: {resp.status_code}")
        except Exception as e:
            print(f"Exception for {ep}: {e}")

    print("\n" + "=" * 60)
    print("TESTING COMPLETE")
    print("=" * 60)

if __name__ == "__main__":
    test_endpoints()
