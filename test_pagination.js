// const fetch = require('node-fetch'); // Native fetch in Node 18+

async function testPagination() {
    console.log('1. Login as admin...');
    const loginResponse = await fetch('http://localhost:7070/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username: 'admin', password: 'password' }) // Try default
    });

    let token;
    if (loginResponse.status === 401) {
        console.log('Default login failed, trying password "123"...');
        const retry = await fetch('http://localhost:7070/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username: 'admin', password: '123' })
        });
        if (!retry.ok) {
            console.error('Login failed');
            return;
        }
        const data = await retry.json();
        token = data.token;
    } else {
        const data = await loginResponse.json();
        token = data.token;
    }

    console.log('Login successful.');

    // Test Products Pagination
    console.log('2. Fetching Products Page 1 (Size 2)...');
    const productsRes = await fetch('http://localhost:7070/products?page=1&size=2', {
        headers: { 'Authorization': `Bearer ${token}` }
    });

    if (productsRes.ok) {
        const page = await productsRes.json();
        console.log('Products Page Response:', JSON.stringify(page, null, 2));
    } else {
        console.error('Products Pagination Failed:', productsRes.status);
        console.log(await productsRes.text());
    }

    // Test Orders Pagination
    console.log('3. Fetching Orders Page 1 (Size 2)...');
    const ordersRes = await fetch('http://localhost:7070/orders/all?page=1&size=2', {
        headers: { 'Authorization': `Bearer ${token}` }
    });

    if (ordersRes.ok) {
        const page = await ordersRes.json();
        console.log('Orders Page Response:', JSON.stringify(page, null, 2));
    } else {
        console.error('Orders Pagination Failed:', ordersRes.status);
        console.log(await ordersRes.text());
    }
}

testPagination();
