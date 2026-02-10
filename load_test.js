import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    vus: __ENV.VUS ? parseInt(__ENV.VUS) : 10,
    duration: '30s',
};

export default function () {
    const BASE_URL = 'http://localhost:7070';

    // 1. Login to get JWT
    const loginPayload = JSON.stringify({ username: 'admin', password: '123' });
    const params = {
        headers: {
            'Content-Type': 'application/json',
            'User-Agent': 'k6-load-test',
            'Origin': 'http://localhost:4200'
        }
    };
    const loginRes = http.post(`${BASE_URL}/api/auth/login`, loginPayload, params);

    const token = loginRes.json('token');

    // 2. Use Token for Product Request
    const authParams = { headers: { 'Authorization': `Bearer ${token}` } };
    let res = http.get(`${BASE_URL}/api/products`, authParams);

    check(res, {
        'status is 200': (r) => r.status === 200,
        'duration < 500ms': (r) => r.timings.duration < 500,
    });
    sleep(1);
}
