import requests
import json

BASE_URL = "http://localhost:7070"

def verify_features():
    # 1. Login
    print("Attempting login...")
    login_payload = {"username": "admin", "password": "123"}
    try:
        login_resp = requests.post(f"{BASE_URL}/login", json=login_payload)
        if login_resp.status_code != 200:
            print(f"Login failed: {login_resp.status_code} {login_resp.text}")
            return
        
        token = login_resp.json().get("token")
        userId = login_resp.json().get("id")
        role = login_resp.json().get("role")
        print(f"Login successful. UserID: {userId}, Role: {role}")
        
        headers = {"Authorization": f"Bearer {token}"}
        
        # 2. Verify Products (Public)
        print("\nVerifying /products/all (Public)...")
        prod_resp = requests.get(f"{BASE_URL}/products/all")
        print(f"Products status: {prod_resp.status_code}")
        
        # 3. Verify Orders (Protected)
        print("\nVerifying /orders (Protected)...")
        # Try creating an order? Or just list if admin?
        # Admin can list all orders via /admin/orders usually, but we refactored to /orders with checks?
        # Let's check /orders/all (Admin) or just /orders (User?)
        # Base refactoring: OrderController maps to /orders
        # Endpoints: GET /all (admin?), POST / (create)
        # Let's try GET /orders/all assuming admin
        orders_resp = requests.get(f"{BASE_URL}/orders/all", headers=headers)
        print(f"Orders /all status: {orders_resp.status_code}")
        if orders_resp.status_code != 200:
             print(orders_resp.text)

        # 4. Verify Watchlist (Protected)
        print("\nVerifying /watchlist/products/all...")
        watch_resp = requests.get(f"{BASE_URL}/watchlist/products/all", headers=headers)
        print(f"Watchlist status: {watch_resp.status_code}")

        # 5. Verify Chat (Protected)
        print("\nVerifying /chat/history/123...")
        # Assuming session ID logic
        chat_resp = requests.get(f"{BASE_URL}/chat/history/test-session", headers=headers)
        print(f"Chat history status: {chat_resp.status_code}")
        
        # 6. Verify Stats (Protected)
        print("\nVerifying /stats/admin...")
        stats_resp = requests.get(f"{BASE_URL}/stats/admin", headers=headers)
        print(f"Stats /admin status: {stats_resp.status_code}")

    except Exception as e:
        print(f"Exception during verification: {e}")

if __name__ == "__main__":
    verify_features()
