import requests
import json
import random
import string

BASE_URL = "http://localhost:7070"

def generate_random_string(length=8):
    letters = string.ascii_lowercase
    return ''.join(random.choice(letters) for i in range(length))

def print_response(response, description):
    print(f"\n--- {description} ---")
    print(f"Status Code: {response.status_code}")
    try:
        if response.content:
            print(f"Response: {json.dumps(response.json(), indent=2)}")
        else:
            print("Response: <empty>")
    except:
        print(f"Response: {response.text}")
    return response

# 1. Register User
username = "user_" + generate_random_string()
email = username + "@test.com"
password = "password123"

print(f"Registering user: {username} / {email}")

payload = {
    "username": username,
    "email": email,
    "password": password
}

resp = requests.post(f"{BASE_URL}/auth/register", json=payload)
print_response(resp, "Register User")

# 2. Login User
login_payload = {
    "username": username,
    "password": password
}

resp = requests.post(f"{BASE_URL}/auth/login", json=login_payload)
print_response(resp, "Login User")
user_token = resp.json().get('token')
user_headers = {"Authorization": f"Bearer {user_token}"}

# 3. Login Admin (Assuming admin exists from setup or seeded data)
# Note: Since we don't have an endpoint to creating admins, we assume 'admin' exists. 
# If not, this step might fail if the DB seed isn't there. 
# However, let's try to register one just in case using a naming convention or just proceed.
# Realistically, we can't register an admin via API based on the code I wrote (isAdmin=false always).
# So we rely on pre-existing data.
admin_payload = {
    "username": "admin", 
    "password": "123" 
}

resp = requests.post(f"{BASE_URL}/auth/login", json=admin_payload)
print_response(resp, "Login Admin")

if resp.status_code == 200:
    admin_token = resp.json().get('token')
    admin_headers = {"Authorization": f"Bearer {admin_token}"}
else:
    print("Admin login failed. Skipping Admin tests.")
    admin_headers = None


# --- Admin Tests ---
if admin_headers:
    # Create Product
    product_payload = {
        "name": "Test Product " + generate_random_string(),
        "description": "A test product",
        "wholesalePrice": 10.50,
        "retailPrice": 20.00,
        "quantity": 100
    }
    resp = requests.post(f"{BASE_URL}/admin/products", json=product_payload, headers=admin_headers)
    print_response(resp, "Admin: Create Product")
    
    # Get All Products (Admin View)
    resp = requests.get(f"{BASE_URL}/admin/products", headers=admin_headers)
    print_response(resp, "Admin: Get All Products")
    
    products = resp.json()
    if products:
        last_product_id = products[-1]['id']
        
        # Update Product
        update_payload = {
             "name": "Updated Product " + generate_random_string(),
             "description": "Updated description",
             "wholesalePrice": 12.00,
             "retailPrice": 25.00,
             "quantity": 50
        }
        resp = requests.put(f"{BASE_URL}/admin/products/{last_product_id}", json=update_payload, headers=admin_headers)
        print_response(resp, "Admin: Update Product")

# --- User Tests ---

# Get All Products (User View)
resp = requests.get(f"{BASE_URL}/user/products", headers=user_headers)
print_response(resp, "User: Get All Products")
products = resp.json()

if products:
    target_product_id = products[0]['id']
    
    # Get Product Detail
    resp = requests.get(f"{BASE_URL}/user/products/{target_product_id}", headers=user_headers)
    print_response(resp, "User: Get Product Detail")

    # Place Order
    order_payload = {
        "items": [
            {
                "productId": target_product_id,
                "quantity": 2
            }
        ]
    }
    resp = requests.post(f"{BASE_URL}/user/orders", json=order_payload, headers=user_headers)
    print_response(resp, "User: Place Order")
    
    # Get All Orders
    resp = requests.get(f"{BASE_URL}/user/orders", headers=user_headers)
    print_response(resp, "User: Get All Orders")
    
    orders = resp.json()
    if orders:
        order_id = orders[0]['orderId']
        
        # Cancel Order
        resp = requests.post(f"{BASE_URL}/user/orders/{order_id}/cancel", headers=user_headers)
        print_response(resp, "User: Cancel Order")

# Watchlist
if products:
    pid = products[0]['id']
    
    resp = requests.post(f"{BASE_URL}/user/watchlist/{pid}", headers=user_headers)
    print_response(resp, "User: Add to Watchlist")
    
    resp = requests.get(f"{BASE_URL}/user/watchlist", headers=user_headers)
    print_response(resp, "User: Get Watchlist")
    
    resp = requests.delete(f"{BASE_URL}/user/watchlist/{pid}", headers=user_headers)
    print_response(resp, "User: Remove from Watchlist")
