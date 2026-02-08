const API_URL = 'http://localhost:7070';
const USERNAME = 'admin';
const PASSWORD = 'password';

async function testLifecycle() {
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
        console.log('Login successful. Role:', data.role);

        console.log('2. Creating Product...');
        const createRes = await fetch(`${API_URL}/products`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({
                name: 'Test Product ' + Date.now(),
                description: 'Test Desc',
                wholesalePrice: 50,
                retailPrice: 100,
                quantity: 10
            })
        });

        if (!createRes.ok) throw new Error('Create failed: ' + await createRes.text());

        // Response should now be JSON
        const createdProduct = await createRes.json();
        console.log('Product created (Status: ' + createRes.status + ') ID:', createdProduct.id);
        const productId = createdProduct.id;

        console.log('3. Uploading Image...');
        const blob = new Blob(['fake image content'], { type: 'image/jpeg' });
        const formData = new FormData();
        formData.append('file', blob, 'test.jpg');

        const uploadRes = await fetch(`${API_URL}/products/${productId}/image`, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${token}` },
            body: formData
        });

        console.log('Upload Status:', uploadRes.status);
        if (!uploadRes.ok) throw new Error('Upload failed: ' + await uploadRes.text());
        console.log('Image uploaded.');

        console.log('4. Getting Image (GET)...');
        // Test with Token
        const getImgRes = await fetch(`${API_URL}/products/${productId}/image`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        console.log('GET Image Status (Auth):', getImgRes.status);
        if (getImgRes.status === 403) console.error('GET Image 403 FORBIDDEN (Auth)!');

        // Test without Token
        const getImgNoAuth = await fetch(`${API_URL}/products/${productId}/image`);
        console.log('GET Image Status (No Auth):', getImgNoAuth.status);
        if (getImgNoAuth.status === 403) console.error('GET Image 403 FORBIDDEN (No Auth)!');

        console.log('5. Updating Product (PATCH)...');
        const patchRes = await fetch(`${API_URL}/products/${productId}`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({
                name: 'Updated Name',
                description: 'Updated Desc',
                wholesalePrice: 55,
                retailPrice: 110,
                quantity: 15
            })
        });

        console.log('PATCH Status:', patchRes.status);
        if (patchRes.status === 403) console.error('PATCH 403 FORBIDDEN!');

        const updatedProduct = await patchRes.json();
        console.log('PATCH Body (Name):', updatedProduct.name);

    } catch (e) {
        console.error('Error:', e.message);
    }
}

testLifecycle();
