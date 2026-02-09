import requests
import json
import time

BASE_URL = "http://34.171.180.77:7070"
# BASE_URL = "http://localhost:7070"

def verify():
    print(f"Targeting: {BASE_URL}")
    print("Logging in as ADMIN...")
    try:
        r = requests.post(f"{BASE_URL}/login", json={"username":"admin","password":"123"})
        r.raise_for_status()
        token = r.json()["token"]
        print("Login successful.")
    except Exception as e:
        print(f"Login failed: {e}")
        return

    headers = {'Authorization': f'Bearer {token}'}

    print("\n--- Checking Health ---")
    try:
        r = requests.get(f"{BASE_URL}/api/system/health", headers=headers)
        print(f"Status: {r.status_code}")
        if r.status_code == 200:
            print(json.dumps(r.json(), indent=2))
        else:
             print(r.text)
    except Exception as e:
        print(f"Health check failed: {e}")

    print("\n--- Fetching Logs ---")
    try:
        r = requests.get(f"{BASE_URL}/api/system/logs?lines=5", headers=headers)
        print(f"Status: {r.status_code}")
        if r.status_code == 200:
            logs = r.json()
            print(f"Received {len(logs)} log lines.")
            for log in logs:
                print(log)
        else:
             print(r.text)
    except Exception as e:
        print(f"Log fetch failed: {e}")

    print("\n--- Triggering Sync ---")
    try:
        r = requests.post(f"{BASE_URL}/api/system/sync-products", headers=headers)
        print(f"Status: {r.status_code}")
        print(r.text)
    except Exception as e:
        print(f"Sync failed: {e}")

if __name__ == "__main__":
    verify()
