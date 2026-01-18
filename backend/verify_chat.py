import requests
import json
import uuid

BASE_URL = "http://localhost:7070"

def test_chat():
    session_id = str(uuid.uuid4())
    print(f"Testing Chat API with Session ID: {session_id}")
    
    # 1. Send specific product question
    message = "What specific policies do you have for returns?"
    print(f"\nSending message: {message}")
    
    payload = {
        "sessionId": session_id,
        "message": message
    }
    
    try:
        response = requests.post(f"{BASE_URL}/chat/message", json=payload)
        if response.status_code == 200:
            print("Response:", json.dumps(response.json(), indent=2))
        else:
            print(f"Error: {response.status_code}")
            print(response.text)
            
    except Exception as e:
        print(f"Failed to connect: {e}")

if __name__ == "__main__":
    test_chat()
