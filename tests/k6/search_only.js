import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '30s', target: 50 },
        { duration: '1m', target: 500 },
        { duration: '30s', target: 500 },
        { duration: '1m', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<200'],
    },
};

const BASE_URL = __ENV.BASE_URL || 'http://host.docker.internal:7070';

export default function () {
    const res = http.get(`${BASE_URL}/products/search?query=phone`);
    check(res, {
        'status is 200': (r) => r.status === 200,
    });
    sleep(1);
}
