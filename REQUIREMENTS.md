# Online Shopping Application Requirements

## Technology Stack
- **Backend**: Spring Boot
- **ORM**: Hibernate (HQL, Criteria) - **NO Native SQL, JDBC, or Spring Data JPA**
- **Database**: MySQL 8.0
- **Security**: Spring Security + JWT
- **Other**: Spring AOP, Spring Validation, DTOs, Global Exception Handling, Logging

## User Features
- **Registration**: Username, email, password. Prevent duplicates. Users only.
- **Login**: Custom `InvalidCredentialsException` for failed attempts.
- **Home Page**:
    - View all in-stock products.
    - View product details (description, retail price). **No actual quantity shown.**
- **Purchasing**:
    - Place orders for multiple items.
    - Deduct stock upon order.
    - `NotEnoughInventoryException` if stock is insufficient.
- **Order Management**:
    - View all orders and order details.
    - Cancel "Processing" orders (reverts stock).
    - Statuses: `Processing`, `Completed`, `Canceled`.
- **Watchlist**: Add/remove products and view in-stock items.
- **Statistics**: Top X most recently and frequently purchased items.

## Admin (Seller) Features
- **Dashboard**:
    - View all orders with status and placement time.
    - Pagination: 5 orders per page.
- **Product Management**:
    - Add new products (description, wholesale price, retail price, quantity).
    - Modify existing products.
- **Order Management**:
    - Complete "Processing" orders.
    - Cancel orders (reverts stock).
- **Statistics**:
    - Total successfully sold items.
    - Top 3 most popular/sold products.
    - Top 3 most profitable products (Profit = retail - wholesale).

## Critical Constraints
- **Price Resilience**: Statistics must reflect the price at the time of purchase, even if the product's current wholesale/retail price is updated later.
- **Criteria API**: At least one DAO method must be implemented using Hibernate Criteria.
