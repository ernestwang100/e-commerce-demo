# Entity Relationship Diagram (ERD)

This diagram shows the relational schema of the SuperDuperMart E-Commerce Platform.

```mermaid
erDiagram
    USER ||--o{ ORDER : places
    USER ||--o{ WATCHLIST : tracks
    USER ||--o{ CHAT_MESSAGE : "logical owner"
    ORDER ||--o{ ORDER_ITEM : contains
    PRODUCT ||--o{ ORDER_ITEM : "included in"
    PRODUCT ||--o{ WATCHLIST : "added to"

    USER {
        int id PK
        string username
        string email
        string password
        string role
        boolean is_admin
        blob profile_picture
    }

    PRODUCT {
        int id PK
        string name
        string description
        decimal wholesale_price
        decimal retail_price
        int quantity
        blob image
    }

    ORDER {
        int id PK
        int user_id FK
        datetime date_placed
        string order_status
    }

    ORDER_ITEM {
        int id PK
        int order_id FK
        int product_id FK
        int quantity
        decimal purchased_price
    }

    WATCHLIST {
        int id PK
        int user_id FK
        int product_id FK
    }

    CHAT_MESSAGE {
        long id PK
        string session_id
        int user_id
        string role
        text content
        datetime created_at
    }
```

## Schema Highlights
- **User-Order (1:N)**: Standard relational link using JPA `@ManyToOne`.
- **Order-OrderItem (1:N)**: Uses `CascadeType.ALL` and `orphanRemoval=true` to manage items as part of the order lifecycle.
- **Product-OrderItem (1:N)**: Maintains referential integrity for purchased items. Note: `purchased_price` is stored in `OrderItem` to protect against future product price changes.
- **Watchlist (M:N via Join Table)**: Implemented as a separate entity to allow for additional metadata/tracking if needed.
- **ChatMessage**: Decoupled session-based storage with a logical link to `userId`.
