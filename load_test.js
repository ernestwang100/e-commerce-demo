import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    vus: __ENV.VUS ? parseInt(__ENV.VUS) : 1,
    duration: '30s',
};

export default function () {
    const BASE_URL = 'http://localhost:7070';

    // 1. Login to get JWT
    const loginPayload = JSON.stringify({ username: 'admin', password: '123' });
    const params = {
        headers: {
            'Content-Type': 'application/json',
            'User-Agent': 'k6-load-test'
        }
    };
    const loginRes = http.post(`${BASE_URL}/login`, loginPayload, params);

    if (loginRes.status !== 200) {
        console.error('Login failed: ' + loginRes.status + ' ' + loginRes.body);
        sleep(1);
        return;
    }

    const token = loginRes.json('token');

    // 2. Use Token for Product Request
    const authParams = { headers: { 'Authorization': `Bearer ${token}` } };
    let res = http.get(`${BASE_URL}/products`, authParams);

    check(res, {
        'status is 200': (r) => r.status === 200,
        'duration < 500ms': (r) => r.timings.duration < 500,
    });
    sleep(1);
}
