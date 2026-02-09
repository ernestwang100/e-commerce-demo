import requests
import json

BASE_URL = "http://localhost:7070"
ES_URL = "http://localhost:19200" # Mapped to 9200

def sync():
    print("Fetching products from backend...")
    try:
        r = requests.get(f"{BASE_URL}/products/all")
        r.raise_for_status()
        products = r.json()
        print(f"Found {len(products)} products.")
    except Exception as e:
        print(f"Error fetching products: {e}")
        return

    for p in products:
        doc = {
            "name": p["name"],
            "description": p["description"],
            "price": p["retailPrice"],
            "imageContentType": p.get("imageContentType"),
            "_class": "com.superdupermart.shopping.document.ProductDocument"
        }
        print(f"Indexing product {p['id']}: {p['name']}...")
        try:
            r_es = requests.post(f"{ES_URL}/products/_doc/{p['id']}", json=doc)
            r_es.raise_for_status()
        except Exception as e:
            print(f"Error indexing product {p['id']}: {e}")

if __name__ == "__main__":
    sync()
