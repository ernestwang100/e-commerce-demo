import requests
import json
import time

BASE_URL = "http://localhost:7070"

def test_admin_add_product_and_user_order():
    print("=" * 60)
    print("ADMIN ADD PRODUCT & USER CREATE ORDER TEST (POSTMAN COMPATIBLE)")
    print("=" * 60)

    # 1. Login as admin
    print("\n[1] ADMIN LOGIN")
    print("-" * 40)
    admin_login = {"username": "admin", "password": "123"}
    resp = requests.post(f"{BASE_URL}/login", json=admin_login)
    if resp.status_code != 200:
        print(f"Admin login failed: {resp.text}")
        return
    admin_data = resp.json()
    admin_token = admin_data.get("token")
    admin_headers = {"Authorization": f"Bearer {admin_token}"}
    print(f"Admin logged in. ID: {admin_data.get('id')}")

    # 2. Admin adds a new product
    print("\n[2] ADMIN CREATE PRODUCT")
    print("-" * 40)
    new_product = {
        "name": "Postman Align Product " + str(int(time.time())),
        "description": "Verified for Postman",
        "wholesalePrice": 10.00,
        "retailPrice": 15.00,
        "quantity": 50
    }
    resp = requests.post(f"{BASE_URL}/products", json=new_product, headers=admin_headers)
    print(f"POST /products - Status: {resp.status_code}, Response: {resp.text}")
    
    # Get ID
    resp = requests.get(f"{BASE_URL}/products/all")
    products = resp.json()
    product_id = products[-1]["id"] if products else None
    print(f"Using product ID: {product_id}")

    # 3. Create NEW user for order
    print("\n[3] CREATE/LOGIN TEST USER")
    print("-" * 40)
    unique_user = f"orderuser{int(time.time())}"
    requests.post(f"{BASE_URL}/signup", json={"username": unique_user, "password": "password", "email": f"{unique_user}@test.com"})
    resp = requests.post(f"{BASE_URL}/login", json={"username": unique_user, "password": "password"})
    user_data = resp.json()
    user_token = user_data.get("token")
    user_headers = {"Authorization": f"Bearer {user_token}"}
    print(f"User logged in. ID: {user_data.get('id')}")

    # 4. Create order (Postman format)
    print("\n[4] USER CREATE ORDER")
    print("-" * 40)
    order_payload = {
        "order": [
            {"productId": product_id, "quantity": 1}
        ]
    }
    print(f"Payload: {json.dumps(order_payload)}")
    resp = requests.post(f"{BASE_URL}/orders", json=order_payload, headers=user_headers)
    print(f"POST /orders - Status: {resp.status_code}")
    print(f"Response: {resp.text}")
    
    if resp.status_code == 200:
        order_id = resp.json().get("orderId")
        
        # 5. Cancel order using PATCH (Postman requirement)
        print("\n[5] CANCEL ORDER (PATCH)")
        print("-" * 40)
        resp = requests.patch(f"{BASE_URL}/orders/{order_id}/cancel", headers=user_headers)
        print(f"PATCH /orders/{order_id}/cancel - Status: {resp.status_code}")
        print(f"Response: {resp.text}")

    print("\n" + "=" * 60)
    print("TEST COMPLETE")
    print("=" * 60)

if __name__ == "__main__":
    test_admin_add_product_and_user_order()
