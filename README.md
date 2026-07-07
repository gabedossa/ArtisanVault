# ArtisanVault

[![CI](https://github.com/gabedossa/ArtisanVault/actions/workflows/ci.yml/badge.svg)](https://github.com/gabedossa/ArtisanVault/actions/workflows/ci.yml)

*[English version](README.en.md)*

Plataforma full-stack que conecta artistas a clientes. Artistas publicam portfólios e serviços; clientes descobrem talentos, solicitam serviços e acompanham pedidos até a entrega da arte — tudo em um único lugar.

Além das funcionalidades de produto, o projeto documenta seu próprio processo de auditoria e correção de segurança em [`docs/metodos-invasao-e-correcoes.md`](docs/metodos-invasao-e-correcoes.md) — um registro real de vetores de ataque identificados (IDOR, exposição de dados, upload malicioso, CSRF, etc.) e como cada um foi corrigido e testado.

---

## Estrutura do Projeto

```text
ArtisanVault/
├── backend/                     # API REST — Spring Boot + PostgreSQL + Flyway
├── docs/                        # Auditoria de segurança e histórico de correções
└── frontend/
    └── artisanvault-frontend/   # SPA — Next.js + Tailwind CSS
```

---

## Stack

| Camada   | Tecnologias                                                                    |
| -------- | ------------------------------------------------------------------------------ |
| Backend  | Java 21, Spring Boot 3.5, Spring Security, JWT (jjwt), JDBC, PostgreSQL, Flyway |
| Frontend | Next.js 16, React 19, TypeScript, Tailwind CSS v4, Axios                       |

---

## Funcionalidades

- Cadastro e login de **Artistas** e **Clientes**
- Portfólios com galeria de obras por artista
- Catálogo de serviços com título, descrição e preço
- Fluxo completo de pedido: cliente solicita um serviço → artista marca como "em andamento" → artista entrega a arte (que vira um trabalho no portfólio, vinculado ao cliente e ao pedido)
- Dashboard do artista — gerenciar perfil, portfólio, serviços e pedidos recebidos
- Dashboard do cliente — acompanhar pedidos e explorar artistas
- Busca de artistas por nome ou descrição
- Modo claro/escuro

---

## Segurança

Este projeto passou por mais de uma rodada de auditoria de segurança (ver [`docs/metodos-invasao-e-correcoes.md`](docs/metodos-invasao-e-correcoes.md) para o histórico completo). Estado atual:

- **Autenticação stateless via JWT em cookie `HttpOnly`** — o token nunca fica acessível a JavaScript no navegador (nem em `localStorage`, nem no corpo da resposta de login); a identidade da sessão é sempre revalidada contra o backend (`GET /api/login/me`).
- **Proteção CSRF explícita** (`CookieCsrfTokenRepository` + header `X-XSRF-TOKEN`), o padrão do Spring Security para SPAs com cookie de sessão.
- **Autorização por dono do recurso** em todos os endpoints de escrita — um usuário autenticado não consegue editar, excluir ou criar dados vinculados a outro usuário (testado com `ArteControllerTest`, `PedidoControllerTest` e verificações manuais ponta a ponta).
- **Rate limit no login** (5 tentativas por e-mail e por IP a cada 5 minutos), sem confiar cegamente em `X-Forwarded-For`.
- **Upload de imagem validado por conteúdo real**: assinatura binária (magic bytes) + decodificação via `ImageIO`, e a imagem salva é **reencodada a partir dos pixels decodificados** (não os bytes originais), descartando qualquer payload anexado ao arquivo.
- **Schema de banco versionado com Flyway** (`ddl-auto=validate` em todos os ambientes) e um papel de banco com **privilégio mínimo** (`artisanvault_app`, só DML nas tabelas da aplicação) separado do usuário usado para rodar migrações.
- Segredos (`jwt.secret`, senha do banco) ficam fora do controle de versão e são configuráveis por variável de ambiente.

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

O schema é versionado pelo Flyway (`backend/src/main/resources/db/migration`) e roda
automaticamente ao subir a aplicação. Por padrão, tudo usa o mesmo usuário do Postgres
(`postgres`). Para rodar a aplicação com um usuário de privilégio mínimo (recomendado
fora do ambiente de desenvolvimento), rode
`backend/src/main/resources/db/provision-app-role.sql` como superusuário e configure:

```properties
spring.datasource.username=artisanvault_app
spring.datasource.password=a_senha_que_voce_definiu_no_script

# migrações continuam rodando com um usuário com privilégio de DDL
spring.flyway.user=postgres
spring.flyway.password=sua_senha_do_postgres
```

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

O backend tem 35 testes automatizados (JUnit 5 + Mockito), cobrindo tanto regras de negócio quanto segurança:

- **Services**: login (JWT), cadastro/atualização de artistas e clientes (incluindo hash de senha com BCrypt).
- **Autorização**: dono vs. não-dono vs. recurso inexistente em arte e pedidos (`ArteControllerTest`, `PedidoControllerTest`).
- **Upload de imagem**: rejeição de conteúdo inválido, `Content-Type` não suportado, arquivo vazio, e remoção de dados anexados via reencodificação (`ImageStorageServiceTest`).
- **Rate limit de login**: bloqueio por e-mail e por IP, reset após sucesso, isolamento entre chaves (`LoginRateLimiterServiceTest`).

```bash
cd backend
mvn test
```

---

## Endpoints da API

Autenticação via cookie `HttpOnly` (JWT). Rotas marcadas como **dono** retornam `403` se o usuário autenticado não for o dono do recurso.

### Autenticação

| Método | Rota                | Descrição                                                  | Acesso           |
| ------ | ------------------- | ------------------------------------------------------------ | ---------------- |
| POST   | `/api/login`        | Login unificado (artista ou cliente); define o cookie JWT     | Público, com rate limit |
| GET    | `/api/login/me`     | Retorna a identidade autenticada atual                        | Autenticado      |
| POST   | `/api/login/logout` | Expira o cookie JWT                                           | Público          |

### Artistas

| Método | Rota                          | Descrição              | Acesso      |
| ------ | ----------------------------- | ------------------------ | ----------- |
| GET    | `/api/artistas`               | Listar todos             | Público     |
| GET    | `/api/artistas/{id}`          | Buscar por ID             | Público     |
| GET    | `/api/artistas/email?email=`  | Buscar por e-mail         | Autenticado |
| POST   | `/api/artistas`               | Criar artista             | Público     |
| PUT    | `/api/artistas/{id}`          | Atualizar perfil          | Dono        |
| DELETE | `/api/artistas/{id}`          | Remover conta             | Dono        |

### Clientes

| Método | Rota                        | Descrição                | Acesso      |
| ------ | ---------------------------- | -------------------------- | ----------- |
| GET    | `/api/cliente/me`            | Dados do próprio cliente    | Autenticado |
| POST   | `/api/cliente/post`          | Criar cliente               | Público     |
| DELETE | `/api/cliente/delete/{id}`   | Remover conta               | Dono        |

### Serviços

| Método | Rota                       | Descrição               | Acesso      |
| ------ | --------------------------- | -------------------------- | ----------- |
| GET    | `/api/servico`              | Listar todos                | Público     |
| GET    | `/api/servico/{id}`         | Buscar por ID                | Público     |
| POST   | `/api/servico`              | Criar serviço                | Dono (artista) |
| PUT    | `/api/servico/{id}`         | Atualizar serviço             | Dono        |
| DELETE | `/api/servico/delete/{id}`  | Remover serviço               | Dono        |

### Portfólio

| Método | Rota                          | Descrição                                      | Acesso         |
| ------ | ------------------------------ | ------------------------------------------------- | -------------- |
| GET    | `/api/portifolio`              | Listar trabalhos (sem `id_cliente`/`id_pedido`)     | Público        |
| GET    | `/api/portifolio/{id}`         | Buscar trabalho (idem)                              | Público        |
| POST   | `/api/portifolio`              | Publicar novo trabalho (multipart, com imagem)       | Dono (artista) |
| DELETE | `/api/portifolio/delete/{id}`  | Remover trabalho                                    | Dono           |

### Arte

| Método | Rota                     | Descrição                                       | Acesso      |
| ------ | ------------------------- | -------------------------------------------------- | ----------- |
| GET    | `/api/arte`                | Listar obras                                        | Autenticado |
| GET    | `/api/arte/{id}`           | Buscar obra por ID                                  | Autenticado |
| POST   | `/api/arte/post`           | Criar obra vinculada a um portfólio                 | Dono (do portfólio) |
| DELETE | `/api/arte/delete/{id}`    | Remover obra                                        | Dono        |

### Pedidos

| Método | Rota                         | Descrição                                                  | Acesso         |
| ------ | ----------------------------- | -------------------------------------------------------------- | -------------- |
| GET    | `/api/pedido/{id}`            | Buscar pedido                                                    | Dono (cliente ou artista) |
| GET    | `/api/pedido/meus`             | Pedidos feitos pelo cliente autenticado                          | Autenticado (cliente) |
| GET    | `/api/pedido/recebidos`        | Pedidos recebidos pelo artista autenticado                        | Autenticado (artista) |
| POST   | `/api/pedido`                  | Cliente solicita um serviço a um artista                          | Dono (cliente) |
| PUT    | `/api/pedido/{id}/iniciar`     | Artista marca o pedido como "em andamento"                        | Dono (artista) |
| POST   | `/api/pedido/{id}/entregar`    | Artista entrega a arte (multipart) — cria o trabalho no portfólio | Dono (artista) |
| DELETE | `/api/pedido/delete/{id}`      | Remover pedido                                                     | Dono (cliente ou artista) |

---

## Rotas do Frontend

| Rota                  | Descrição                                                         |
| --------------------- | -------------------------------------------------------------------- |
| `/`                   | Landing page com destaques de artistas                                |
| `/artistas`           | Listagem de artistas com busca                                        |
| `/artistas/[id]`      | Perfil público do artista (portfólio + serviços)                     |
| `/portifolios/[id]`   | Detalhe de um trabalho do portfólio                                  |
| `/login`              | Login                                                                 |
| `/cadastro/artista`   | Cadastro de artista                                                   |
| `/cadastro/cliente`   | Cadastro de cliente                                                   |
| `/dashboard/artista`  | Dashboard do artista — perfil, portfólio, serviços e pedidos recebidos |
| `/dashboard/cliente`  | Dashboard do cliente — perfil e pedidos                               |

---

## Melhorias Planejadas

A auditoria de segurança em [`docs/metodos-invasao-e-correcoes.md`](docs/metodos-invasao-e-correcoes.md) lista o histórico completo. Os únicos itens genuinamente em aberto hoje dependem de infraestrutura de produção que este projeto (rodando localmente) não tem:

- Migrar o rate limit de login para armazenamento distribuído (Redis) caso o backend passe a rodar em múltiplas instâncias.
- Servir a aplicação por HTTPS real e ativar `COOKIE_SECURE=true` contra um domínio de produção.

---

## Licença

Projeto de portfólio demonstrando arquitetura em camadas com Spring Boot, autenticação/autorização robusta e um processo real de auditoria de segurança. Livre para uso e modificação.
