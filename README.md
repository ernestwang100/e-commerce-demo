# E-Commerce Demo Application

A full-stack e-commerce application featuring a Spring Boot backend, Angular frontend, and a modern microservices-ready infrastructure with Docker, Redis caching, and Kafka messaging.

## 🚀 Tech Stack

| Layer | Technology |
|-------|------------|
| **Frontend** | Angular 17, TypeScript, RxJS |
| **Backend** | Spring Boot 3.2, Java 17, Spring Security, Spring Data JPA |
| **Database** | MySQL 8.0 |
| **Caching** | Redis |
| **Messaging** | Apache Kafka (with Zookeeper) |
| **Containerization** | Docker, Docker Compose |
| **AI Integration** | Google Gemini API (RAG-based product chatbot) |

## ✨ Features

### User Features
- **Product Browsing**: View all in-stock products with details.
- **Shopping Cart**: Add/remove items, view cart summary.
- **Order Management**: Place orders, view order history, cancel orders.
- **AI Chatbot**: Ask questions about products using a RAG-powered assistant.
- **Watchlist**: Save products for later.
- **Transactional Emails**: Receive real-time email confirmations for orders.
- **Payment Simulation**: Mock payment processing behavior with randomized failures (Stripe-like).

### Admin Features
- **Product Management**: Add, update, view all products (including out-of-stock).
- **Order Management**: View all orders, complete/cancel orders.
- **Dashboard Stats**: Most popular products, most profitable items.

### Infrastructure
- **Redis Caching**: Product listings are cached for improved performance.
- **Kafka Events**: Order placement triggers async notifications.
- **JWT Authentication**: Secure, stateless authentication.

## 🐳 Quick Start (Docker)

**Prerequisites**: Docker and Docker Compose installed.

```bash
# Clone the repository
git clone <your-repo-url>
cd e-commerce-demo

# Start all services (MySQL, Redis, Kafka, Backend, Frontend)
docker-compose up -d --build

# Wait ~30 seconds for all services to initialize
```

**Access:**
- **Frontend**: [http://localhost:4200](http://localhost:4200)
- **Backend API**: [http://localhost:7070](http://localhost:7070)

**Default Credentials:**
| Role | Username | Password |
|------|----------|----------|
| Admin | `admin` | `123` |
| User | `user` | `123` |

## 🏗️ Project Structure

```
e-commerce-demo/
├── backend/                 # Spring Boot Application
│   ├── src/main/java/...
│   │   ├── controller/      # REST Controllers
│   │   ├── service/         # Business Logic
│   │   ├── dao/             # Data Access (Hibernate)
│   │   ├── entity/          # JPA Entities
│   │   ├── dto/             # Data Transfer Objects
│   │   └── security/        # JWT, Filters, Config
│   ├── Dockerfile
│   └── pom.xml
├── frontend/                # Angular Application
│   ├── src/app/
│   │   ├── admin/           # Admin Components
│   │   ├── user/            # User Components
│   │   ├── services/        # Angular Services
│   │   └── guards/          # Route Guards
│   ├── Dockerfile
│   └── nginx.conf
├── docker-compose.yml       # Orchestration
└── init.sql                 # Database Seed Data
```

## 🔧 Local Development

### Backend (without Docker)
```bash
cd backend
# Ensure MySQL is running on localhost:3307
mvn spring-boot:run
```

### Frontend (without Docker)
```bash
cd frontend
npm install
ng serve
```

## 📡 API Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/login` | User login | Public |
| POST | `/signup` | User registration | Public |
| GET | `/products/all` | List products | Public |
| GET | `/products/{id}` | Product detail | Public |
| POST | `/orders` | Place order | User/Admin |
| GET | `/orders/all` | List orders | User/Admin |
| PATCH | `/orders/{id}/cancel` | Cancel order | User/Admin |
| POST | `/chat` | AI Chatbot | User |

## 🔒 Environment Variables

The backend uses Jasypt for encrypted properties. Key environment variables:

| Variable | Description | Default (Docker) |
|----------|-------------|------------------|
| `SPRING_DATASOURCE_URL` | MySQL connection string | `jdbc:mysql://mysql:3306/shopping_app` |
| `JASYPT_ENCRYPTOR_PASSWORD` | Encryption key | `commerce` |
| `GEMINI_API_KEY` | Google Gemini API Key | *(optional)* |

## 📊 Architecture Diagram

```
┌─────────────┐      ┌─────────────┐      ┌─────────────┐
│   Nginx     │      │  Spring     │      │   MySQL     │
│  (Frontend) │─────▶│   Boot      │─────▶│  Database   │
│  :4200      │      │   :7070     │      │   :3306     │
└─────────────┘      └──────┬──────┘      └─────────────┘
                            │
                 ┌──────────┼──────────┐
                 │          │          │
                 ▼          ▼          ▼
            ┌────────┐ ┌────────┐ ┌────────┐
            │ Redis  │ │ Kafka  │ │Zookeeper│
            │ :6379  │ │ :9092  │ │ :2181  │
            └────────┘ └────────┘ └────────┘
```

## 📧 Testing Emails

To use real emails, update the `.env` file with your SMTP credentials:
```properties
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=your-email@gmail.com
SPRING_MAIL_PASSWORD=your-app-password
```
If not configured, emails will be simulated and logged to the backend console.

## 🧪 Testing Kafka

After placing an order, check backend logs for the Kafka consumer message:
```bash
docker logs shopping-backend --tail 20
# Look for: "Received order event: Order placed successfully..."
```

## 📝 License

MIT License
