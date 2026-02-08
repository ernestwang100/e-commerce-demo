import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    vus: 1,
    iterations: 1,
};

const BASE_URL = __ENV.BASE_URL || 'http://host.docker.internal:7070';

export default function () {
    // 1. Login
    const loginPayload = JSON.stringify({
        username: 'admin',
        password: '123',
    });

    const loginHeaders = { 'Content-Type': 'application/json' };
    const loginRes = http.post(`${BASE_URL}/login`, loginPayload, { headers: loginHeaders });

    check(loginRes, {
        'logged in successfully': (r) => r.status === 200,
    });

    if (loginRes.status !== 200) {
        console.error(`Login failed: ${loginRes.body}`);
        return;
    }

    const token = loginRes.json('token');
    const authHeaders = {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
    };

    // 2. Add Product
    const uniqueId = Date.now();
    const productName = `ElasticTest-${uniqueId}`;
    const productPayload = JSON.stringify({
        name: productName,
        description: 'A product to test Elasticsearch sync',
        wholesalePrice: 10.0,
        retailPrice: 20.0,
        quantity: 100,
    });

    // Start with default mapping then adjust if needed. Controller maps POST /products to addProduct
    const addRes = http.post(`${BASE_URL}/products`, productPayload, { headers: authHeaders });

    check(addRes, {
        'product added successfully': (r) => r.status === 200,
    });

    if (addRes.status !== 200) {
        console.error(`Add product failed: ${addRes.body}`);
    }

    // 3. Wait for Sync
    sleep(2);

    // 4. Search Product
    const searchRes = http.get(`${BASE_URL}/products/search?query=${productName}`, { headers: authHeaders });

    check(searchRes, {
        'search successful': (r) => r.status === 200,
        'product found in search': (r) => {
            const body = r.json();
            return Array.isArray(body) && body.length > 0 && body[0].name === productName;
        },
    });

    if (searchRes.status === 200) {
        const body = searchRes.json();
        if (Array.isArray(body) && body.length > 0) {
            console.log(`SUCCESS: Found product ${body[0].name} in search results.`);
        } else {
            console.error(`FAILURE: Product ${productName} NOT found in search results: ${JSON.stringify(body)}`);
        }
    } else {
        console.error(`Search failed: ${searchRes.body}`);
    }
}
