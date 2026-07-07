# ArtisanVault

*[Versão em português](README.md)*

Full-stack platform connecting artists to clients. Artists publish portfolios and services; clients discover talent, request services, and track orders all the way through to delivery — all in one place.

Beyond the product features, this project documents its own security audit and remediation process in [`docs/metodos-invasao-e-correcoes.md`](docs/metodos-invasao-e-correcoes.md) (Portuguese) — a real record of attack vectors found (IDOR, data exposure, malicious uploads, CSRF, etc.) and how each one was fixed and tested.

---

## Project Structure

```text
ArtisanVault/
├── backend/                     # REST API — Spring Boot + PostgreSQL + Flyway
├── docs/                        # Security audit and remediation history
└── frontend/
    └── artisanvault-frontend/   # SPA — Next.js + Tailwind CSS
```

---

## Stack

| Layer    | Technologies                                                                   |
| -------- | -------------------------------------------------------------------------------- |
| Backend  | Java 21, Spring Boot 3.5, Spring Security, JWT (jjwt), JDBC, PostgreSQL, Flyway  |
| Frontend | Next.js 16, React 19, TypeScript, Tailwind CSS v4, Axios                        |

---

## Features

- Sign-up and login for **Artists** and **Clients**
- Portfolios with a gallery of works per artist
- Service catalog with title, description and pricing
- Full order flow: client requests a service → artist marks it "in progress" → artist delivers the artwork (which becomes a portfolio piece, linked to both the client and the order)
- Artist dashboard — manage profile, portfolio, services and received orders
- Client dashboard — track orders and explore artists
- Search artists by name or description
- Light/dark mode

---

## Security

This project went through more than one round of security auditing (see [`docs/metodos-invasao-e-correcoes.md`](docs/metodos-invasao-e-correcoes.md) for the full history, in Portuguese). Current state:

- **Stateless JWT authentication via an `HttpOnly` cookie** — the token is never accessible to JavaScript in the browser (not in `localStorage`, not in the login response body); session identity is always revalidated against the backend (`GET /api/login/me`).
- **Explicit CSRF protection** (`CookieCsrfTokenRepository` + `X-XSRF-TOKEN` header), the standard Spring Security pattern for SPAs with cookie-based sessions.
- **Resource-owner authorization** on every write endpoint — an authenticated user can't edit, delete, or create data tied to another user (covered by `ArteControllerTest`, `PedidoControllerTest`, and verified manually end-to-end).
- **Login rate limiting** (5 attempts per email and per IP every 5 minutes), without blindly trusting `X-Forwarded-For`.
- **Image uploads validated by real content**: binary signature (magic bytes) + decoding via `ImageIO`, and the saved image is **re-encoded from the decoded pixels** (not the original bytes), discarding any payload appended to the file.
- **Database schema versioned with Flyway** (`ddl-auto=validate` in every environment) and a **least-privilege** database role (`artisanvault_app`, DML-only on the application's own tables) separate from the account used to run migrations.
- Secrets (`jwt.secret`, database password) stay out of version control and are configurable via environment variables.

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

Create `backend/src/main/resources/application-local.properties` (gitignored) from the template:

```bash
cp backend/src/main/resources/application-local.properties.example backend/src/main/resources/application-local.properties
```

And fill it in with your real credentials:

```properties
spring.datasource.password=your_password
jwt.secret=a_base64_secret_of_at_least_256_bits
```

This file is never committed — secrets stay out of version control.

The schema is versioned with Flyway (`backend/src/main/resources/db/migration`) and runs
automatically on startup. By default everything uses the same Postgres user (`postgres`).
To run the app with a minimal-privilege user instead (recommended outside local dev), run
`backend/src/main/resources/db/provision-app-role.sql` as a superuser and configure:

```properties
spring.datasource.username=artisanvault_app
spring.datasource.password=the_password_you_set_in_the_script

# migrations still run with a DDL-privileged user
spring.flyway.user=postgres
spring.flyway.password=your_postgres_password
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

The backend has 35 automated tests (JUnit 5 + Mockito), covering both business rules and security:

- **Services**: login (JWT), artist/client registration and update (including BCrypt password hashing).
- **Authorization**: owner vs. non-owner vs. missing resource for artworks and orders (`ArteControllerTest`, `PedidoControllerTest`).
- **Image upload**: rejects invalid content, unsupported `Content-Type`, empty files, and strips appended data via re-encoding (`ImageStorageServiceTest`).
- **Login rate limiting**: blocking by email and by IP, reset on success, isolation between keys (`LoginRateLimiterServiceTest`).

```bash
cd backend
mvn test
```

---

## API Endpoints

Authentication via an `HttpOnly` JWT cookie. Routes marked **owner** return `403` if the authenticated user isn't the resource owner.

### Authentication

| Method | Route               | Description                                        | Access                 |
| ------ | -------------------- | ----------------------------------------------------- | ---------------------- |
| POST   | `/api/login`         | Unified login (artist or client); sets the JWT cookie   | Public, rate-limited     |
| GET    | `/api/login/me`      | Returns the current authenticated identity              | Authenticated            |
| POST   | `/api/login/logout`  | Expires the JWT cookie                                   | Public                  |

### Artists

| Method | Route                          | Description        | Access      |
| ------ | ------------------------------- | --------------------- | ----------- |
| GET    | `/api/artistas`                 | List all               | Public      |
| GET    | `/api/artistas/{id}`            | Find by ID              | Public      |
| GET    | `/api/artistas/email?email=`    | Find by email           | Authenticated |
| POST   | `/api/artistas`                 | Create artist            | Public      |
| PUT    | `/api/artistas/{id}`            | Update profile           | Owner       |
| DELETE | `/api/artistas/{id}`            | Remove account            | Owner       |

### Clients

| Method | Route                        | Description             | Access      |
| ------ | ------------------------------ | --------------------------- | ----------- |
| GET    | `/api/cliente/me`               | Own client data              | Authenticated |
| POST   | `/api/cliente/post`             | Create client                | Public      |
| DELETE | `/api/cliente/delete/{id}`      | Remove account                | Owner       |

### Services

| Method | Route                       | Description        | Access          |
| ------ | ----------------------------- | ---------------------- | --------------- |
| GET    | `/api/servico`                 | List all                | Public          |
| GET    | `/api/servico/{id}`            | Find by ID                | Public          |
| POST   | `/api/servico`                 | Create service             | Owner (artist)  |
| PUT    | `/api/servico/{id}`            | Update service              | Owner           |
| DELETE | `/api/servico/delete/{id}`     | Remove service               | Owner           |

### Portfolio

| Method | Route                           | Description                                        | Access          |
| ------ | --------------------------------- | ------------------------------------------------------ | --------------- |
| GET    | `/api/portifolio`                  | List works (without `id_cliente`/`id_pedido`)             | Public          |
| GET    | `/api/portifolio/{id}`             | Find a work (same as above)                                | Public          |
| POST   | `/api/portifolio`                  | Publish a new work (multipart, with image)                  | Owner (artist)  |
| DELETE | `/api/portifolio/delete/{id}`      | Remove a work                                               | Owner           |

### Artwork

| Method | Route                      | Description                                    | Access         |
| ------ | ---------------------------- | --------------------------------------------------- | -------------- |
| GET    | `/api/arte`                   | List artworks                                        | Authenticated   |
| GET    | `/api/arte/{id}`              | Find artwork by ID                                    | Authenticated   |
| POST   | `/api/arte/post`              | Create artwork linked to a portfolio piece            | Owner (of the portfolio) |
| DELETE | `/api/arte/delete/{id}`       | Remove artwork                                        | Owner           |

### Orders

| Method | Route                         | Description                                                    | Access                     |
| ------ | ------------------------------- | -------------------------------------------------------------------- | -------------------------- |
| GET    | `/api/pedido/{id}`               | Find an order                                                          | Owner (client or artist)   |
| GET    | `/api/pedido/meus`                | Orders placed by the authenticated client                              | Authenticated (client)       |
| GET    | `/api/pedido/recebidos`           | Orders received by the authenticated artist                            | Authenticated (artist)        |
| POST   | `/api/pedido`                     | Client requests a service from an artist                                | Owner (client)               |
| PUT    | `/api/pedido/{id}/iniciar`        | Artist marks the order as "in progress"                                 | Owner (artist)                |
| POST   | `/api/pedido/{id}/entregar`       | Artist delivers the artwork (multipart) — creates the portfolio piece   | Owner (artist)                |
| DELETE | `/api/pedido/delete/{id}`         | Remove an order                                                          | Owner (client or artist)     |

---

## Frontend Routes

| Route                 | Description                                                       |
| ---------------------- | ----------------------------------------------------------------------- |
| `/`                    | Landing page with featured artists                                       |
| `/artistas`            | Artist listing with search                                                |
| `/artistas/[id]`       | Public artist profile (portfolio + services)                             |
| `/portifolios/[id]`    | Single portfolio work detail                                              |
| `/login`               | Login                                                                     |
| `/cadastro/artista`    | Artist sign-up                                                            |
| `/cadastro/cliente`    | Client sign-up                                                            |
| `/dashboard/artista`   | Artist dashboard — profile, portfolio, services, and received orders     |
| `/dashboard/cliente`   | Client dashboard — profile and orders                                     |

---

## Planned Improvements

The security audit in [`docs/metodos-invasao-e-correcoes.md`](docs/metodos-invasao-e-correcoes.md) (Portuguese) lists the full history. The only items genuinely still open today depend on production infrastructure this locally-run project doesn't have:

- Migrate login rate limiting to distributed storage (Redis) if the backend starts running across multiple instances.
- Serve the application over real HTTPS and enable `COOKIE_SECURE=true` against a production domain.

---

## License

Portfolio project demonstrating layered architecture with Spring Boot, robust authentication/authorization, and a real security audit process. Free to use and modify.
