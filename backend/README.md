# Online Shopping Application - Backend

Spring Boot 3.2 RESTful API for the Online Shopping Application.

## Prerequisites
- Java 17
- Maven
- Docker (for MySQL)

## Database Setup
```bash
cd backend
docker-compose up -d
```
This starts MySQL 8.0 on port 3307.

### Connection Details
- **Host:** `localhost`
- **Port:** `3307`
- **Database:** `shopping_app`
- **Username:** `shopuser`
- **Password:** `shoppass`
- **Root Password:** `root`

### Admin User
- **Username:** `admin`
- **Password:** `123`
- **Role:** `ADMIN`

## Running the Application

**Important:** The application uses Jasypt for password encryption. You must provide the encryption password.

### Environment Variables
Create a `.env` file (already exists) with your Gemini API key and SMTP credentials:
```properties
GEMINI_API_KEY=your_api_key_here

# SMTP Configuration (Optional - for real emails)
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=your-email@gmail.com
SPRING_MAIL_PASSWORD=your-app-password
```

### Start Command (PowerShell)
```powershell
# Load .env and run
Get-Content .env | ForEach-Object { if ($_ -match "^([^=]+)=(.*)$") { [Environment]::SetEnvironmentVariable($matches[1], $matches[2]) } }
mvn spring-boot:run "-Djasypt.encryptor.password=commerce"
```

### Start Command (Bash/Linux/Mac)
```bash
export $(cat .env | xargs)
mvn spring-boot:run -Djasypt.encryptor.password=commerce
```

### Quick Start (with inline env var)
```powershell
$env:GEMINI_API_KEY="YOUR_API_KEY"; mvn spring-boot:run "-Djasypt.encryptor.password=commerce"
```

The application runs on **http://localhost:7070**

## API Endpoints

### Authentication
- `POST /auth/register` - Register new user
- `POST /auth/login` - Login and get JWT token

### User Endpoints (requires USER role)
- `GET /user/products` - List all in-stock products
- `GET /user/products/{id}` - Get product details
- `GET /products/search` - Search products (query, minPrice, maxPrice)
- `POST /user/orders` - Place an order (triggers Payment Simulation, 10% failure chance)
- `GET /user/orders` - Get user's order history

### Admin Endpoints (requires ADMIN role)
- `GET /admin/products` - List all products (with wholesale price)
- `POST /admin/products` - Add new product
- `PUT /admin/products/{id}` - Update product
- `GET /admin/orders` - List all orders
- `PATCH /admin/orders/{id}/complete` - Complete an order
- `PATCH /admin/orders/{id}/cancel` - Cancel an order

## Re-encrypting Secrets

If you need to change the Jasypt password or re-encrypt values:

1. Update `JasyptEncryptionTest.java` with new password
2. Run: `mvn test -Dtest=JasyptEncryptionTest`
3. Copy encrypted values from `target/encrypted_values.txt`
4. Update `application.properties` with new `ENC()` values
