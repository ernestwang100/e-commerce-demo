const API_URL = 'http://localhost:7070';
const USERNAME = 'admin';
const PASSWORD = 'password';

async function testStats() {
    try {
        console.log(`1. Login as ${USERNAME}...`);
        let loginRes = await fetch(`${API_URL}/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username: USERNAME, password: PASSWORD })
        });

        let token;
        if (!loginRes.ok) {
            console.log('Default login failed, trying password "123"...');
            loginRes = await fetch(`${API_URL}/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username: USERNAME, password: '123' })
            });
            if (!loginRes.ok) throw new Error('Login failed');
        }

        const data = await loginRes.json();
        token = data.token;
        console.log('Login successful.');

        console.log('2. Fetching Admin Stats...');
        const statsRes = await fetch(`${API_URL}/stats/admin`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        console.log('Stats Status:', statsRes.status);
        if (!statsRes.ok) {
            console.error('Stats Body:', await statsRes.text());
            throw new Error('Failed to fetch stats');
        }

        const stats = await statsRes.json();
        console.log('Total Sold Items:', stats.totalSoldItems);
        console.log('Most Popular:', stats.mostPopular);
        console.log('Most Profitable:', stats.mostProfitable);

    } catch (e) {
        console.error('Error:', e.message);
    }
}

testStats();
