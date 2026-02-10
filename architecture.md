# System Architecture & User Flow

This diagram illustrates the high-level architecture and data flow of the SuperDuperMart E-Commerce Platform.

```mermaid
graph TD
    User((User)) -->|Browser| Frontend[Angular 17 Frontend]
    
    subgraph "Backend Layer (Docker)"
        Frontend -->|REST API + JWT| Backend[Spring Boot 3.2 Backend]
        Backend -->|AOP Logging| Backend
        Backend -->|Security Filter| Backend
    end

    subgraph "Persistence & Performance"
        Backend -->|JPA/ACID| MySQL[(MySQL 8.0 <br/>Orders/Users)]
        Backend -->|Caching/PubSub| Redis[(Redis Stack <br/>Cache & Vector DB)]
        Backend -->|Search Queries| ES[(Elasticsearch 8.11 <br/>Search Index)]
    end

    subgraph "Messaging & AI"
        Backend -->|Producer| Kafka{Kafka <br/>Messaging}
        Kafka -->|Consumer| Notification[Notification Service]
        Backend -->|RAG Context| Gemini[Google Gemini API]
        Redis -.->|Similarity Search| Gemini
    end

    style Frontend fill:#dd0031,stroke:#333,stroke-width:2px,color:#fff
    style Backend fill:#6db33f,stroke:#333,stroke-width:2px,color:#fff
    style MySQL fill:#4479a1,stroke:#333,color:#fff
    style Redis fill:#d82c20,stroke:#333,color:#fff
    style ES fill:#005571,stroke:#333,color:#fff
    style Kafka fill:#231f20,stroke:#333,color:#fff
    style Gemini fill:#4285f4,stroke:#333,color:#fff
```

## Key Technical highlights
- **Angular 17**: Modular, lazy-loaded frontend.
- **Spring Boot 3.2**: Stateless REST API with JWT security.
- **RAG (Retrieval-Augmented Generation)**: Using Google Gemini and Redis Vector Store.
- **Elasticsearch**: High-performance search indexing.
- **Kafka**: Asynchronous event-driven order notifications.
- **Redis Stack**: Distributed caching and vector calculations.
