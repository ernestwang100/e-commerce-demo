# Frontend Requirements

Online Shopping Application frontend built with Angular 14 and Angular Material.

## Tech Stack

| Technology | Version |
|------------|---------|
| Angular | 14.2.x |
| Angular Material | 14.2.7 |
| Angular CDK | 14.2.7 |
| RxJS | 7.5.x |
| TypeScript | 4.7.x |
| Node.js | 16+ (LTS) |
| npm | 8+ |

## Project Structure

```
frontend/
├── src/
│   ├── app/
│   │   ├── admin/         # Admin dashboard & management
│   │   ├── auth/          # Login & registration
│   │   ├── user/          # User shopping experience
│   │   ├── services/      # API services
│   │   ├── models/        # TypeScript interfaces
│   │   ├── interceptors/  # HTTP interceptors
│   │   └── shared/        # Shared components
│   ├── environments/      # Environment configs
│   └── styles.css         # Global styles
├── angular.json
├── package.json
└── tsconfig.json
```

## Features

### Authentication Module (`/auth`)
- **Login** - JWT-based authentication
- **Register** - User registration with role assignment

### User Module (`/user`)
- **Home** - Dashboard with top items
- **Product List** - Browse all products
- **Product Detail** - View product details, add to cart/watchlist
- **Cart** - View/edit cart, place orders
- **Order History** - View past orders
- **Order Detail** - View order details, cancel processing orders
- **Watchlist** - Saved products
- **Top Items** - Frequently/recently purchased products

### Admin Module (`/admin`)
- **Home** - Dashboard with statistics
- **Products** - Product CRUD operations
- **Product Form** - Create/edit products
- **Orders** - View/manage all orders (paginated)
- **Order Detail** - Complete/cancel orders
- **Statistics**:
  - Most Profitable Products
  - Most Popular Products
  - Total Sold Products

## Data Models

### User
```typescript
interface User {
  id?: number;
  username: string;
  email: string;
  role: string;          // "USER" | "ADMIN"
  password?: string;
}
```

### Product
```typescript
interface Product {
  id: number;
  name: string;
  description: string;
  retailPrice: number;
  wholesalePrice?: number;  // Admin only
  quantity?: number;        // Stock
}
```

### Order
```typescript
interface Order {
  id: number;
  datePlaced: Date;
  status: string;           // "Processing" | "Completed" | "Canceled"
  orderItems: OrderItem[];
  userId: number;
  username?: string;
  totalPrice: number;
}

interface OrderItem {
  id: number;
  product: Product;
  quantity: number;
  purchasedPrice: number;
}
```

## API Endpoints Required

Base URL: `http://localhost:8080` (configurable via `environment.ts`)

### Auth
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/login` | Login, returns JWT + user |
| POST | `/signup` | Register new user |

### Products
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/products/all` | Get all products |
| GET | `/products/{id}` | Get product by ID |
| GET | `/products/frequent/{limit}` | User's frequently purchased |
| GET | `/products/recent/{limit}` | User's recently purchased |
| POST | `/products` | Create product (admin) |
| PATCH | `/products/{id}` | Update product (admin) |
| GET | `/products/profit/{limit}` | Most profitable (admin) |
| GET | `/products/popular/{limit}` | Most popular (admin) |
| GET | `/products/total-sold` | Total sold count (admin) |

### Orders
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/orders/all` | Get user's orders (user) or all orders paginated (admin) |
| GET | `/orders/{id}` | Get order details |
| POST | `/orders` | Place new order |
| PATCH | `/orders/{id}/cancel` | Cancel order |
| PATCH | `/orders/{id}/complete` | Complete order (admin) |

### Watchlist
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/watchlist/products/all` | Get watchlist products |
| POST | `/watchlist/product/{id}` | Add to watchlist |
| DELETE | `/watchlist/product/{id}` | Remove from watchlist |

## Authentication

- JWT tokens stored in `localStorage`
- HTTP interceptor injects `Authorization: Bearer <token>` header
- User session persisted via `localStorage`

## Storage

| Key | Description |
|-----|-------------|
| `token` | JWT access token |
| `user` | User object (JSON) |
| `shopping_cart` | Cart items (JSON) |

## Setup Instructions

1. **Install dependencies**
   ```bash
   npm install
   ```

2. **Configure API URL** (optional)
   Edit `src/environments/environment.ts`:
   ```typescript
   export const environment = {
     production: false,
     apiUrl: 'http://localhost:8080'
   };
   ```

3. **Run development server**
   ```bash
   npm start
   # or
   ng serve
   ```
   App runs at `http://localhost:4200`

4. **Build for production**
   ```bash
   npm run build
   ```
   Output: `dist/frontend/`

## Styling

- Angular Material `indigo-pink` prebuilt theme
- Custom global styles in `src/styles.css`
- Roboto font family

## Testing

```bash
npm test  # Runs Karma tests
```

- Test framework: Jasmine 4.3
- Test runner: Karma 6.4
- Browser: Chrome (via karma-chrome-launcher)
