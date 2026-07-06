# ArtisanVault

*[Versão em português](README.md)*

Full-stack platform connecting artists to clients. Artists publish portfolios and services; clients discover talent and track orders — all in one place.

---

## Project Structure

```text
ArtisanVault/
├── backend/                     # REST API — Spring Boot + PostgreSQL
└── frontend/
    └── artisanvault-frontend/   # SPA — Next.js + Tailwind CSS
```

---

## Stack

| Layer    | Technologies                                   |
| -------- | ----------------------------------------------- |
| Backend  | Java 21, Spring Boot 3.3, JDBC, PostgreSQL      |
| Frontend | Next.js 16, TypeScript, Tailwind CSS v4, Axios  |

---

## Features

- Sign-up and login for **Artists** and **Clients**
- Portfolios with a gallery of works per artist
- Service catalog with pricing
- Orders with status tracking (pending / in progress / delivered)
- Artist dashboard — manage profile, portfolios, services and received orders
- Client dashboard — track orders and explore artists
- Search artists by name or description

---

## Prerequisites

- Java 21+
- Maven 3.9+
- Node.js 20+
- PostgreSQL 14+

---

## Running the Project

### 1. Database

Create the database in PostgreSQL:

```sql
CREATE DATABASE postgres;
```

### 2. Backend

Configure the credentials in `backend/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
spring.datasource.username=your_username
spring.datasource.password=your_password
```

Start the application:

```bash
cd backend
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`.

### 3. Frontend

```bash
cd frontend/artisanvault-frontend
npm install
npm run dev
```

The application will be available at `http://localhost:3000`.

---

## Tests

The backend has unit tests (JUnit 5 + Mockito) covering the business rules of the main services — login (JWT), and artist/client registration and update (including BCrypt password hashing).

```bash
cd backend
mvn test
```

---

## API Endpoints

### Authentication

| Method | Route        | Description                                |
| ------ | ------------ | ------------------------------------------- |
| POST   | `/api/login` | Unified login — returns `{email, userType}` |

### Artists

| Method | Route                        | Description       |
| ------ | ---------------------------- | ------------------ |
| GET    | `/api/artistas`              | List all           |
| GET    | `/api/artistas/{id}`         | Find by ID          |
| GET    | `/api/artistas/email?email=` | Find by email        |
| POST   | `/api/artistas`              | Create artist       |
| PUT    | `/api/artistas/{id}`         | Update artist       |
| DELETE | `/api/artistas/{id}`         | Remove artist       |

### Clients

| Method | Route                       | Description       |
| ------ | ---------------------------- | ------------------ |
| GET    | `/api/cliente`               | List all           |
| GET    | `/api/cliente/{id}`          | Find by ID          |
| POST   | `/api/cliente/post`          | Create client       |
| DELETE | `/api/cliente/delete/{id}`   | Remove client       |

### Portfolios, Services, Artworks and Orders

| Method | Route                           | Description         |
| ------ | -------------------------------- | -------------------- |
| GET    | `/api/portifolio`                | List portfolios       |
| GET    | `/api/portifolio/{id}`           | Find portfolio        |
| DELETE | `/api/portifolio/delete/{id}`    | Remove portfolio      |
| GET    | `/api/servico`                   | List services         |
| DELETE | `/api/servico/delete/{id}`       | Remove service         |
| GET    | `/api/arte`                      | List artworks          |
| POST   | `/api/arte/post`                 | Create artwork         |
| DELETE | `/api/arte/delete/{id}`          | Remove artwork          |
| GET    | `/api/pedido`                    | List orders             |
| GET    | `/api/pedido/{id}`               | Find order              |
| DELETE | `/api/pedido/delete/{id}`        | Remove order              |

---

## Frontend Routes

| Route                 | Description                       |
| ---------------------- | ---------------------------------- |
| `/`                    | Landing page                       |
| `/artistas`            | Artist listing with search          |
| `/artistas/[id]`       | Public artist profile               |
| `/portifolios/[id]`    | Portfolio artwork gallery            |
| `/login`               | Login                               |
| `/cadastro/artista`    | Artist sign-up                       |
| `/cadastro/cliente`    | Client sign-up                       |
| `/dashboard/artista`   | Artist dashboard                     |
| `/dashboard/cliente`   | Client dashboard                     |

---

## Planned Improvements

- Fix SQL query bugs in the backend (portfolio, service, order)
- Move database credentials to environment variables
- Add POST endpoints for Portfolio, Service and Order
- Expand test coverage to the remaining services (Portfolio, Service, Artwork, Order) and add integration tests

---

## License

Portfolio project demonstrating layered architecture with Spring Boot. Free to use and modify.
