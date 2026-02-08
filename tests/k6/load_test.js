import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '30s', target: 50 },  // Ramp to 50 users
        { duration: '1m', target: 500 },  // Ramp to 500 users
        { duration: '30s', target: 500 }, // Stay at 500
        { duration: '1m', target: 0 },    // Scale down
    ],
    thresholds: {
        http_req_duration: ['p(95)<500'], // 95% of requests must complete below 500ms
    },
};

const BASE_URL = __ENV.BASE_URL || 'http://host.docker.internal:7070';

export default function () {
    // 1. Visit Home Page (List Products)
    const res = http.get(`${BASE_URL}/products/all`);

    check(res, {
        'status is 200': (r) => r.status === 200,
        'protocol is HTTP/1.1': (r) => r.proto === 'HTTP/1.1',
    });

    // 2. View a specific product
    if (res.status === 200) {
        // Pick a random ID from 1 to 20
        const id = Math.floor(Math.random() * 20) + 1;
        http.get(`${BASE_URL}/products/${id}`);
    }

    // 3. Search for a product
    http.get(`${BASE_URL}/products/search?query=phone`);

    sleep(1);
}
