Tech Stack
LayerTechnologyFrameworkSpring Boot 3.x, Spring SecurityDatabasePostgreSQL 15CacheRedis 7AuthJWTPaymentsStripeDevOpsDocker, Docker ComposeBuildMaven

Getting Started
Prerequisites

Java 17+, Maven 3.8+, Docker & Docker Compose

Run with Docker (recommended)
bashgit clone https://github.com/prathameshkapade/dream-shops.git
cd dream-shops
cp .env.example .env        # fill in your values
docker-compose up --build
API runs at → http://localhost:9193/api/v1
Swagger UI → http://localhost:9193/swagger-ui.html
Run locally (without Docker)
bash# Ensure PostgreSQL and Redis are running locally
mvn spring-boot:run -Dspring-boot.run.profiles=dev

API Overview
ModuleBase PathAuth/api/v1/authProducts/api/v1/productsCategories/api/v1/categoriesCart/api/v1/carts, /api/v1/cartItemsOrders/api/v1/ordersImages/api/v1/imagesPayments/api/v1/paymentsUsers/api/v1/users

Full endpoint docs at /swagger-ui.html when the app is running.


Environment Variables
bashcp .env.example .env
See .env.example for all required variables.
Never commit .env to Git.

Project Structure
src/main/
├── java/com/dreamshops/
│   ├── config/          # Security, Redis, Stripe config
│   ├── controller/      # REST controllers
│   ├── dto/             # Request / Response DTOs
│   ├── exceptions/      # Global exception handler
│   ├── model/           # JPA entities
│   ├── repository/      # Spring Data repositories
│   ├── response/        # API response wrapper
│   └── service/         # Business logic
└── resources/
├── application.yml          # Base config
├── application-dev.yml      # Local dev
└── application-prod.yml     # Production (env-var driven)

Author
Prathamesh Kapade — GitHub · LinkedIn · prathameshkapade6@gmail.com