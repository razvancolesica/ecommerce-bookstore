# eCommerce Bookstore — Capstone Project

An AI-assisted full-stack eCommerce platform for a bookstore, built using **IBM Bob** (Agentic IDE) as part of the AI Specialist Cloud FullStack Capstone.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17, Spring Boot 3.x, Maven |
| Database | PostgreSQL 16 |
| Frontend | React 18, Vite, React Router |
| API Spec | OpenAPI 3.0 (openapi.yaml) |
| API Testing | Insomnia collection |

## Project Structure

```
ecommerce-bookstore/
├── backend/                  # Spring Boot application
│   └── src/main/java/com/bookstore/
│       ├── controller/       # REST controllers
│       ├── service/          # Business logic
│       ├── repository/       # Spring Data JPA repos
│       ├── entity/           # JPA entities
│       ├── dto/              # Request/Response DTOs
│       ├── config/           # Security, CORS, OpenAPI config
│       └── exception/        # Global error handling
├── frontend/                 # React + Vite SPA
│   └── src/
│       ├── pages/            # Route-level page components
│       ├── components/       # Shared UI components
│       ├── services/         # Axios API clients
│       └── context/          # React context (Cart, Auth)
├── docs/
│   └── openapi.yaml          # OpenAPI 3.0 specification
├── insomnia-collection.json  # API test collection
└── README.md
```

## Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL 16 running locally
- Node.js 18+

## Database Setup

```sql
-- Already created via psql:
CREATE DATABASE bookstore_db;
CREATE USER postgres WITH PASSWORD 'postgres';
GRANT ALL PRIVILEGES ON DATABASE bookstore_db TO postgres;
```

## Running the Backend

```bash
cd backend
./mvnw clean install
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`  
Swagger UI: `http://localhost:8080/swagger-ui.html`

## Running the Frontend

```bash
cd frontend
npm install
npm run dev
```

The app will be available at `http://localhost:5173`

## API Overview

See [`docs/openapi.yaml`](docs/openapi.yaml) for the full OpenAPI specification.

Key endpoints:

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Login and receive JWT |
| GET | `/api/products` | List products (with filters) |
| GET | `/api/products/{id}` | Get product details |
| GET | `/api/categories` | List all categories |
| GET | `/api/cart` | Get current user's cart |
| POST | `/api/cart/items` | Add item to cart |
| DELETE | `/api/cart/items/{id}` | Remove cart item |
| POST | `/api/orders` | Place an order |
| GET | `/api/orders` | Get order history |
| POST | `/api/orders/{id}/cancel` | Cancel order (within 48 hrs) |

## Development Workflow (Agentic IDE)

This project was designed and developed using **IBM Bob** (Agentic IDE):

1. Wireframes analyzed to extract data entities and API contracts
2. OpenAPI spec generated via Bob
3. Spring Boot skeleton scaffolded via Bob
4. React frontend pages generated via Bob
5. API test collection generated via Bob

## Git Workflow

```bash
git checkout -b feature/api-implementation
git add .
git commit -m "Implement Spring Boot API for e-commerce bookstore"
git push origin feature/api-implementation
# → Open Pull Request on GitHub
```
