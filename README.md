# ArtisanVault

*[English version](README.en.md)*

Plataforma full-stack que conecta artistas a clientes. Artistas publicam portfólios e serviços; clientes descobrem talentos e acompanham pedidos — tudo em um único lugar.

---

## Estrutura do Projeto

```text
ArtisanVault/
├── backend/                     # API REST — Spring Boot + PostgreSQL
└── frontend/
    └── artisanvault-frontend/   # SPA — Next.js + Tailwind CSS
```

---

## Stack

| Camada   | Tecnologias                                    |
| -------- | ---------------------------------------------- |
| Backend  | Java 21, Spring Boot 3.3, JDBC, PostgreSQL     |
| Frontend | Next.js 16, TypeScript, Tailwind CSS v4, Axios |

---

## Funcionalidades

- Cadastro e login de **Artistas** e **Clientes**
- Portfólios com galeria de obras por artista
- Catálogo de serviços com preço
- Pedidos com status (aguardando / em andamento / entregue)
- Dashboard do artista — gerenciar perfil, portfólios, serviços e pedidos recebidos
- Dashboard do cliente — acompanhar pedidos e explorar artistas
- Busca de artistas por nome ou descrição

---

## Pré-requisitos

- Java 21+
- Maven 3.9+
- Node.js 20+
- PostgreSQL 14+

---

## Rodando o Projeto

### 1. Banco de Dados

Crie o banco no PostgreSQL:

```sql
CREATE DATABASE postgres;
```

### 2. Backend

Crie `backend/src/main/resources/application-local.properties` (gitignored) a partir do template:

```bash
cp backend/src/main/resources/application-local.properties.example backend/src/main/resources/application-local.properties
```

E preencha com suas credenciais reais:

```properties
spring.datasource.password=sua_senha
jwt.secret=um_segredo_base64_de_pelo_menos_256_bits
```

Esse arquivo nunca é commitado — os segredos ficam fora do controle de versão.

Suba a aplicação:

```bash
cd backend
mvn spring-boot:run
```

A API ficará disponível em `http://localhost:8080`.

### 3. Frontend

```bash
cd frontend/artisanvault-frontend
npm install
npm run dev
```

A aplicação ficará disponível em `http://localhost:3000`.

---

## Testes

O backend possui testes unitários (JUnit 5 + Mockito) cobrindo as regras de negócio dos services principais — login (JWT), cadastro/atualização de artistas e clientes (incluindo o hash de senha com BCrypt).

```bash
cd backend
mvn test
```

---

## Endpoints da API

### Autenticação

| Método | Rota         | Descrição                                     |
| ------ | ------------ | --------------------------------------------- |
| POST   | `/api/login` | Login unificado — retorna `{email, userType}` |

### Artistas

| Método | Rota                         | Descrição         |
| ------ | ---------------------------- | ----------------- |
| GET    | `/api/artistas`              | Listar todos      |
| GET    | `/api/artistas/{id}`         | Buscar por ID     |
| GET    | `/api/artistas/email?email=` | Buscar por email  |
| POST   | `/api/artistas`              | Criar artista     |
| PUT    | `/api/artistas/{id}`         | Atualizar artista |
| DELETE | `/api/artistas/{id}`         | Remover artista   |

### Clientes

| Método | Rota                      | Descrição      |
| ------ | ------------------------- | -------------- |
| GET    | `/api/cliente`            | Listar todos   |
| GET    | `/api/cliente/{id}`       | Buscar por ID  |
| POST   | `/api/cliente/post`       | Criar cliente  |
| DELETE | `/api/cliente/delete/{id}`| Remover cliente|

### Portfólios, Serviços, Artes e Pedidos

| Método | Rota                           | Descrição          |
| ------ | ------------------------------ | ------------------ |
| GET    | `/api/portifolio`              | Listar portfólios  |
| GET    | `/api/portifolio/{id}`         | Buscar portfólio   |
| DELETE | `/api/portifolio/delete/{id}`  | Remover portfólio  |
| GET    | `/api/servico`                 | Listar serviços    |
| DELETE | `/api/servico/delete/{id}`     | Remover serviço    |
| GET    | `/api/arte`                    | Listar obras       |
| POST   | `/api/arte/post`               | Criar obra         |
| DELETE | `/api/arte/delete/{id}`        | Remover obra       |
| GET    | `/api/pedido`                  | Listar pedidos     |
| GET    | `/api/pedido/{id}`             | Buscar pedido      |
| DELETE | `/api/pedido/delete/{id}`      | Remover pedido     |

---

## Rotas do Frontend

| Rota                  | Descrição                        |
| --------------------- | -------------------------------- |
| `/`                   | Landing page                     |
| `/artistas`           | Listagem de artistas com busca   |
| `/artistas/[id]`      | Perfil público do artista        |
| `/portifolios/[id]`   | Galeria de obras do portfólio    |
| `/login`              | Login                            |
| `/cadastro/artista`   | Cadastro de artista              |
| `/cadastro/cliente`   | Cadastro de cliente              |
| `/dashboard/artista`  | Dashboard do artista             |
| `/dashboard/cliente`  | Dashboard do cliente             |

---

## Melhorias Planejadas

- Corrigir bugs nas queries SQL do backend (portfólio, serviço, pedido)
- Migrar credenciais do banco para variáveis de ambiente
- Adicionar endpoints POST para Portfólio, Serviço e Pedido
- Ampliar cobertura de testes para os demais services (Portfólio, Serviço, Arte, Pedido) e adicionar testes de integração

---

## Licença

Projeto de portfólio demonstrando arquitetura em camadas com Spring Boot. Livre para uso e modificação.
