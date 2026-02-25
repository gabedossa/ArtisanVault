# ArtisanVault

**ArtisanVault** é um sistema full‑stack para cadastro e reserva de artesanato.  O projeto é composto por dois repositórios: o backend em Java (este diretório) e o frontend em React (submódulo chamado *Artsian_front*).  O objetivo é aproximar artesãos e clientes, permitindo que artistas publiquem suas peças e que clientes reservem ou encomendem trabalhos.

## 🧱 Arquitetura

- **Spring Boot** – estrutura o backend em camadas de `controller`, `service`, `repository` e `entity` utilizando JPA e REST.
- **DTOs** – Data Transfer Objects evitam a exposição direta das entidades e facilitam a serialização/deserialização.
- **Mapper** – classes `RowMapper` convertem registros do banco em objetos de domínio.
- **Front‑end React** – localizado no submódulo [`Artsian_front`](../frontend/Artsian_front), utiliza React Router e componentes reutilizáveis.

## 🔧 Tecnologias

- Java 17
- Spring Boot 3
- Spring Data JPA e JDBC
- PostgreSQL (ou outro banco compatível)
- React 18 (no submódulo frontend)

## 🚀 Como executar o backend

1. **Pré‑requisitos**: JDK 17+, Maven e um banco de dados PostgreSQL em execução.
2. Clone este repositório e o submódulo do frontend:

   ```bash
   git clone https://github.com/gabedossa/ArtisanVault.git
   cd ArtisanVault
   git submodule update --init --recursive
   ```
3. Configure o banco de dados editando o arquivo `application.properties` em `backend/src/main/resources/` com as credenciais corretas.  É recomendável utilizar variáveis de ambiente para senhas.
4. Execute a aplicação:

   ```bash
   cd backend
   mvn spring-boot:run
   ```
5. A API estará disponível em `http://localhost:8080`.  Endpoints para cadastro de artistas, obras e reservas podem ser encontrados nos controladores.

## 🚀 Como executar o frontend

1. Após clonar o repositório e atualizar o submódulo, acesse o diretório do frontend:

   ```bash
   cd frontend/Artsian_front/meu-projeto
   npm install
   npm run start
   ```
2. A aplicação React estará acessível em `http://localhost:3000`.

## ✅ Próximos passos

- Adicionar testes unitários e de integração (JUnit, Mockito para o backend e React Testing Library para o frontend).
- Implementar autenticação/autorizacão (JWT) para proteger rotas sensíveis.
- Utilizar ferramentas de migração de banco de dados como **Flyway** ou **Liquibase**.
- Melhorar a responsividade e acessibilidade da UI.

## 💄 Licença

Projeto criado para fins acadêmicos.  Sinta‑se livre para reutilizar e modificar o código conforme necessário.
