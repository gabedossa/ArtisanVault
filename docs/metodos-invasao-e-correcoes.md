# ArtisanVault - Metodos de invasao e correcoes

Atualizado em: 2026-07-07

Este documento descreve os principais caminhos de invasao identificados no projeto ArtisanVault, o status atual de cada ponto e as correcoes recomendadas. O objetivo e defensivo: registrar o que ainda precisa ser corrigido e evitar que controles de seguranca fiquem apenas no frontend.

## Status geral

A maior parte dos pontos praticaveis foi tratada, incluindo provisionar de verdade um usuario de banco com privilegio minimo neste ambiente (nao apenas documentar a ideia). A releitura mais recente encontrou 2 erros de seguranca/logica de identidade (colisao de e-mail entre tipos de usuario e normalizacao inconsistente de e-mail no fluxo de login/cadastro) — ambos corrigidos nesta rodada, ver secao 12.

- Imagens de portfolio agora sao reencodadas a partir dos pixels decodificados (`ImageIO.write`) em vez de salvar os bytes originais, descartando qualquer metadado/payload residual anexado ao arquivo. WEBP foi removido da lista de formatos aceitos (backend e frontend), ja que o `ImageIO` do JDK nao tem encoder para esse formato e nao daria para reencodar com seguranca.
- Protecao CSRF explicita adicionada via `CookieCsrfTokenRepository` (cookie `XSRF-TOKEN` legivel por JS + header `X-XSRF-TOKEN`), o padrao recomendado pelo proprio Spring Security para SPAs com cookie de sessao. `POST /api/login` e `POST /api/login/logout` ficam de fora da checagem (nao ha autoridade ambiente para um CSRF abusar antes do login existir). O frontend (axios) foi configurado para enviar o header automaticamente.
- Adotado Flyway com uma migracao baseline (`V1__baseline.sql`) que reflete o schema real atual. `spring.jpa.hibernate.ddl-auto` passou a ser `validate` por padrao em todos os ambientes — mudancas de schema agora devem ser feitas criando uma nova migracao versionada, nao deixando o Hibernate alterar tabelas silenciosamente.
- Criado o papel Postgres `artisanvault_app` com privilegio minimo (`db/provision-app-role.sql`), com Flyway rodando via um usuario separado (com privilegio de DDL) atraves de `spring.flyway.user`/`password`. Testado ponta a ponta neste ambiente: login, CRUD via API e bloqueio de `CREATE TABLE`/acesso a tabelas nao relacionadas para o papel restrito.
- A rotacao de segredos ja expostos (`jwt.secret`, senha do Postgres) tinha sido feita anteriormente (commit `a2573c3`) — o checklist estava desatualizado, nao o codigo.
- Corrigida colisao/normalizacao de e-mail entre `cliente` e `artista`: `ArtistaService`/`ClienteService` agora bloqueiam e-mail duplicado entre os dois tipos de usuario antes de salvar (`409 Conflict`), e a busca de artista por e-mail passou a ser case-insensitive, no mesmo padrao ja usado por cliente. Migracao Flyway `V2` normaliza os dados existentes e adiciona indices unicos case-insensitive por tabela. Ver secao 12.
- Restam 2 itens que dependem de infraestrutura de producao que genuinamente nao existe neste projeto (nenhum Redis rodando, nenhum dominio real com HTTPS). O suporte de codigo/configuracao para os dois ja existe e e opcional (desligado por padrao) — falta apenas a infraestrutura real para ativa-lo e validar ponta a ponta. Ver secao abaixo.

Legenda:

- `[RESOLVIDO]`: o ponto principal foi tratado no codigo atual.
- `[PARCIAL]`: houve melhora, mas ainda existe risco remanescente.
- `[PENDENTE]`: o risco continua aberto.
- `[AVALIADO/NAO E PROBLEMA]`: investigado e descartado como risco real neste projeto, com justificativa.

## O que ainda falta ajustar

Pendencias de infraestrutura/producao que continuam abertas. Os itens abaixo dependem de infraestrutura que nao existe neste ambiente (nao ha como provisionar um Redis ou um dominio com HTTPS real dentro do repositorio). Em ambos os casos o suporte no codigo ja foi implementado como opt-in (desligado por padrao), faltando so a infraestrutura real para liga-lo:

1. `[PENDENTE]` Migrar o rate limit para armazenamento distribuido (Redis) se o backend passar a rodar em multiplas instancias atras de um load balancer. Ja existe `RedisLoginRateLimiterService` (`app.rate-limit.store=redis` / `RATE_LIMIT_STORE=redis`), ao lado da implementacao em memoria que continua sendo o default. Nao testado contra um Redis real neste ambiente (nenhum Redis disponivel aqui) — so validado que a aplicacao sobe normalmente com a dependencia `spring-boot-starter-data-redis` no classpath e o default `memory` ativo.
2. `[PENDENTE]` Servir a aplicacao por HTTPS real e definir `COOKIE_SECURE=true` contra esse dominio de producao. `app.cookie.secure`/`app.trust-proxy-headers` ja sao configuraveis via env (secao 11); o que faltava era um exemplo concreto de infraestrutura para isso, agora em `deploy/docker-compose.prod.yml` (Caddy como proxy reverso com HTTPS automatico via Let's Encrypt, na frente do backend, com `COOKIE_SECURE=true`, `TRUST_PROXY_HEADERS=true` e `RATE_LIMIT_STORE=redis`). Nao foi implantado nem testado contra um dominio real neste ambiente.

## 1. Exposicao publica de endpoints GET

Status: `[RESOLVIDO]`.

O `SecurityConfig` mantem a lista explicita de GETs publicos (artistas, servico, portifolio, uploads) e tudo mais exige autenticacao. Veja tambem a secao 4.

## 2. IDOR em update/delete de artista

Status: `[RESOLVIDO]`.

`ArtistaController` compara o artista autenticado (por e-mail) com o ID da URL antes de editar ou excluir.

### DTO de atualizacao: avaliado, nao aplicado

Cheguei a considerar um `ArtistaUpdateRequest` para nao aceitar `tipoUsuario` no corpo do PUT. Na pratica, `tipoUsuario` nao e usado para elevar privilegio (o JWT ja carrega o `userType` real, gerado no login, e nao le esse campo da entidade em nenhuma verificacao de autorizacao) — entao aceitar esse campo no corpo do update e um risco cosmetico, nao uma escalada real de privilegio. Mantive a entidade direta para nao aumentar o escopo da mudanca sem um ganho de seguranca correspondente.

## 3. Exclusao arbitraria por ID

Status: `[RESOLVIDO]`.

Verificacoes de dono continuam presentes em `ClienteController`, `PedidoController`, `PortifolioController`, `ArteController`, `ArtistaController` e `ServicoController`.

### Testes automatizados

Adicionados `ArteControllerTest` e `PedidoControllerTest` (JUnit 5 + Mockito, no mesmo estilo das suites de service ja existentes), cobrindo dono, nao-dono e recurso inexistente para os fluxos de criacao/exclusao de arte e para leitura/exclusao/listagem de pedidos.

## 4. Vazamento de pedidos por filtro no frontend

Status: `[RESOLVIDO]`.

`PedidoController` agora expoe:

- `GET /api/pedido/meus`: pedidos do cliente autenticado (resolvido por e-mail, nao por ID enviado pelo cliente).
- `GET /api/pedido/recebidos`: pedidos recebidos pelo artista autenticado.
- `GET /api/pedido/{id}`: retorna 403 se o usuario autenticado nao for o cliente nem o artista do pedido.

O endpoint antigo `GET /api/pedido` (listagem completa) foi removido. O frontend (`pedido.service.ts`, dashboards de cliente e artista) foi atualizado para consumir os novos endpoints em vez de baixar tudo e filtrar no navegador.

## 5. Listagem global de clientes

Status: `[RESOLVIDO]`.

`GET /api/cliente` e `GET /api/cliente/{id}` (listagem/leitura arbitraria) foram removidos. `ClienteController` agora expoe apenas `GET /api/cliente/me`, que resolve o cliente pelo e-mail autenticado. O frontend (`cliente.service.ts`, dashboard do cliente) foi atualizado para usar `me()`.

## 6. Criacao de arte sem validacao de dono

Status: `[RESOLVIDO]`.

`ArteController.createArte` agora recebe `Authentication`, busca o portfolio informado no corpo da requisicao e retorna 403 se o portfolio nao pertencer ao artista autenticado (mesmo padrao ja usado em `PortifolioController`/`PedidoController`). Coberto por `ArteControllerTest`.

## 7. Upload de arquivos baseado apenas em Content-Type

Status: `[RESOLVIDO]`.

### O que mudou

`ImageStorageService.store` agora:

1. Le os bytes do arquivo em memoria.
2. Valida a assinatura binaria (magic bytes) de JPEG, PNG e GIF contra o `Content-Type` declarado.
3. Decodifica a imagem via `ImageIO.read` e rejeita se o resultado for nulo.
4. **Reencoda a imagem a partir dos pixels decodificados** (`ImageIO.write`) e salva esse resultado em disco — nao os bytes originais enviados pelo cliente. Isso descarta qualquer metadado ou payload residual anexado ao arquivo (testado explicitamente: um PNG valido com dados arbitrarios anexados apos o fim da imagem tem esses dados removidos no arquivo salvo).

`X-Content-Type-Options: nosniff` ja e enviado por padrao pelo Spring Security em todas as respostas, incluindo `/uploads/**`, sem necessidade de configuracao adicional.

Testes automatizados em `ImageStorageServiceTest` cobrem: conteudo que nao e imagem real (rejeitado mesmo com `Content-Type` de imagem), PNG valido (aceito), `Content-Type` nao suportado (rejeitado), arquivo vazio (rejeitado) e remocao de dados anexados via reencodificacao.

### WEBP removido

WEBP foi removido da lista de formatos aceitos (`ImageStorageService` no backend; `accept` dos inputs de arquivo no frontend). O `ImageIO` do JDK nao tem encoder nativo para WEBP, entao nao daria para cumprir a mesma garantia de reencodificacao para esse formato sem adicionar uma biblioteca externa — e melhor recusar o formato do que aceita-lo com uma garantia de seguranca mais fraca que os demais.

### Risco remanescente

GIFs animados sao reduzidos a um unico frame ao reencodar, ja que `BufferedImage` nao preserva animacao. Aceitavel para o caso de uso (portfolio de arte), mas vale documentar caso animacao vire um requisito no futuro.

## 8. JWT em localStorage

Status: `[RESOLVIDO]`.

### O que mudou

- `POST /api/login` agora define o JWT em um cookie `HttpOnly`, `SameSite=Lax`, `Path=/`, com `Secure` controlado pela variavel de ambiente `COOKIE_SECURE` (usar `true` atras de HTTPS em producao). O corpo da resposta (`LoginResponse`) nao inclui mais o token (`@JsonIgnore`), entao nem o proprio JavaScript da aplicacao consegue le-lo.
- `JwtAuthenticationFilter` aceita o token tanto via header `Authorization: Bearer` (compatibilidade) quanto via cookie `artisanvault_token`.
- Novo endpoint `POST /api/login/logout` expira o cookie no servidor.
- Novo endpoint `GET /api/login/me` retorna a identidade autenticada (e-mail/tipo/id/nome) resolvida no backend a partir do JWT. O frontend (`AuthContext.tsx`) usa esse endpoint como fonte de verdade da sessao e nao grava mais dados de usuario no navegador. `login()` retorna o `AuthUser` resolvido pelo backend, e `app/login/page.tsx` usa esse retorno para decidir o redirecionamento pos-login, sem ler `localStorage` em nenhum ponto do fluxo.
- `withCredentials: true` foi mantido (agora e o mecanismo real de transporte do cookie).

### CSRF: resolvido

`SecurityConfig` habilita `CookieCsrfTokenRepository.withHttpOnlyFalse()` (cookie `XSRF-TOKEN`, `Secure` alinhado com `COOKIE_SECURE`, `SameSite=Lax`) com `CsrfTokenRequestAttributeHandler`, mais um `CsrfCookieFilter` (`OncePerRequestFilter`) que forca a resolucao preguicosa do token em toda requisicao — o padrao oficial do Spring Security para SPAs stateless. `POST /api/login` e `POST /api/login/logout` ficam fora da checagem, ja que nao operam sobre uma sessao ja autenticada (nao ha autoridade ambiente para um CSRF abusar). O frontend (`api.ts`) habilita `withXSRFToken: true` no axios, necessario porque backend e frontend rodam em origens diferentes (portas distintas) e o axios so anexa o header `X-XSRF-TOKEN` automaticamente para requisicoes cross-origin quando isso e habilitado explicitamente.

Testado manualmente ponta a ponta: requisicao de mutacao sem o header `X-XSRF-TOKEN` recebe `403`; com o header correspondente ao cookie, é aceita normalmente (login continua funcionando sem o header, por estar na lista de ignorados).

## 9. Login sem rate limit

Status: `[RESOLVIDO]`.

`LoginRateLimiterService` mantem, em memoria, uma janela deslizante de 5 minutos por chave (`email:` e `ip:`). Apos 5 tentativas na janela, `POST /api/login` responde `429 Too Many Requests` e novas tentativas (mesmo com senha correta) sao bloqueadas ate a janela expirar. O contador e zerado no login bem-sucedido. Coberto por `LoginRateLimiterServiceTest` (bloqueio por e-mail, por IP, ausencia de bloqueio abaixo do limite, `reset` e isolamento entre chaves diferentes).

### X-Forwarded-For: corrigido

O IP usado na chave de rate limit so vem do header `X-Forwarded-For` quando `TRUST_PROXY_HEADERS=true` estiver explicitamente configurado (para ambientes atras de um proxy/load balancer confiavel que sobrescreve esse header). Por padrao (`false`), usa `request.getRemoteAddr()`, o IP real da conexao TCP — evitando que um cliente malicioso falsifique o header para contornar o limite por IP ou para forjar tentativas em nome do IP de outra pessoa.

### Risco remanescente

O default continua sendo a implementacao em memoria (`InMemoryLoginRateLimiterService`, nao distribuida) — se o backend rodar em multiplas instancias atras de um load balancer, cada instancia tera seu proprio contador. Existe agora uma implementacao alternativa, `RedisLoginRateLimiterService`, que usa um ZSET por chave (`email:`/`ip:`) com timestamp como score para a janela deslizante; e ativada trocando `app.rate-limit.store` (env `RATE_LIMIT_STORE`) de `memory` para `redis`, com o host/porta/senha do Redis em `spring.data.redis.*` (env `REDIS_HOST`/`REDIS_PORT`/`REDIS_PASSWORD`). Depende de um Redis real no ambiente de deploy, que nao existe neste projeto hoje — por isso nao foi testada ponta a ponta aqui (so `LoginRateLimiterServiceTest`, contra a implementacao em memoria).

## 10. Dependencias vulneraveis

Status: `[RESOLVIDO]` para as vulnerabilidades conhecidas no momento desta auditoria.

### O que mudou

Frontend (`package.json`):

- `axios` atualizado para `^1.18.1`.
- `next` atualizado para `16.2.10` (ultima estavel na linha 16.2.x).
- Adicionado bloco `overrides` fixando `postcss@^8.5.10` (corrige `GHSA-qx2v-qp2m-jg93`, que o proprio Next.js ainda traz internamente em `8.4.31` mesmo na versao estavel mais recente), `form-data@^4.0.6` (corrige `GHSA-hmw2-7cc7-3qxx`) e `js-yaml@^4.3.0` (corrige `GHSA-h67p-54hq-rp68`, trazido transitivamente pelo ESLint).
- `npm audit` reporta **0 vulnerabilidades** apos as mudancas (antes: 1 high + 1 moderate, e mais 1 moderate do Next.js/postcss).
- `npm run build` executado com sucesso apos a atualizacao.

Backend (`pom.xml`):

- `spring-boot-starter-parent` atualizado de `3.3.2` para `3.5.16` (ultima versao estavel da linha 3.5.x; evitamos o salto para a major 4.x para nao introduzir mudancas de API/arquitetura fora do escopo desta auditoria).
- Removidas as versoes fixadas de `org.postgresql:postgresql` e `org.hibernate.orm:hibernate-core`, que agora seguem o BOM do Spring Boot (Postgres JDBC `42.7.11`, Hibernate `6.6.53.Final`), evitando divergencia futura do BOM.
- Adicionado `flyway-core` + `flyway-database-postgresql` (ver secao 11).
- Suite de testes (`./mvnw test`) executada com sucesso: 35/35 testes passando.

### Observacao

Uma futura migracao para Spring Boot 4.x deve ser tratada como um projeto a parte (major upgrade, possiveis breaking changes em Spring Security/Jakarta), nao como parte de uma correcao de seguranca pontual.

## 11. Segredos e configuracao

Status: `[RESOLVIDO]` para os itens que dependem apenas de codigo/configuracao do repositorio.

### O que mudou

`application.properties` agora le de variaveis de ambiente, com defaults seguros para desenvolvimento local:

```properties
spring.datasource.username=${DB_USERNAME:postgres}
spring.jpa.hibernate.ddl-auto=${DDL_AUTO:validate}
spring.jpa.show-sql=${SHOW_SQL:true}
app.cookie.secure=${COOKIE_SECURE:false}
app.trust-proxy-headers=${TRUST_PROXY_HEADERS:false}
```

Em producao, basta definir `DB_USERNAME` (usuario com privilegios minimos), `SHOW_SQL=false`, `COOKIE_SECURE=true` (exige HTTPS) e `TRUST_PROXY_HEADERS=true` (somente se houver um proxy confiavel na frente) sem tocar no codigo.

### Flyway adotado

O schema deixou de ser gerenciado implicitamente pelo Hibernate (`ddl-auto=update`) e passou a ser versionado pelo Flyway:

- `db/migration/V1__baseline.sql` recria fielmente o schema atual das 6 tabelas usadas pelas entidades JPA (`artista`, `cliente`, `servico`, `portfolio`, `pedido`, `arte`), incluindo colunas legadas da entidade `Cliente` (`cpf`, `cidade`, `data_cadastro`) que existem no banco mas nao sao mapeadas pelo codigo atual — mantidas no baseline para refletir a realidade do schema, nao para serem usadas.
- `spring.flyway.baseline-on-migrate=true` + `baseline-version=1` fazem o banco de desenvolvimento existente ser tratado como "ja na versao 1" sem reexecutar o baseline nele; um banco novo/vazio (CI, ambiente de outro desenvolvedor) executa o script e chega ao mesmo schema.
- `spring.jpa.hibernate.ddl-auto` passou a ser `validate` por padrao em **todos** os ambientes (antes era `update` em dev). Mudancas de schema agora exigem criar uma nova migracao versionada em `db/migration`, em vez de deixar o Hibernate alterar tabelas automaticamente ao subir a aplicacao.
- Validado nos dois cenarios possiveis: contra o banco de desenvolvimento existente (baseline aplicado, Hibernate `validate` passou sem erros) e contra um banco novo/vazio criado do zero (Flyway executou o `V1` e criou as tabelas, Hibernate `validate` tambem passou).

Tabelas legadas nao relacionadas a nenhuma entidade atual (`item_nota`, `nota_fiscal`, `produto`, de uma versao anterior do projeto) foram deixadas de fora do Flyway propositalmente — nao pertencem ao schema que a aplicacao atual gerencia.

### Usuario de banco com privilegio minimo: resolvido

`backend/src/main/resources/db/provision-app-role.sql` cria o papel `artisanvault_app` (`NOSUPERUSER`, `NOCREATEDB`, `NOCREATEROLE`) com apenas `SELECT`/`INSERT`/`UPDATE`/`DELETE` nas 6 tabelas que a aplicacao usa (e `USAGE`/`SELECT` nas sequences correspondentes) — sem privilegio de DDL e sem acesso as tabelas legadas nao relacionadas (`produto`, `nota_fiscal`, `item_nota`). Como o Flyway precisa de privilegio de DDL para aplicar migracoes, `spring.flyway.user`/`spring.flyway.password` permitem configurar um usuario separado (com DDL) so para as migracoes, enquanto `spring.datasource.username`/`password` (usado pela aplicacao em runtime) fica com o usuario restrito. Testado ponta a ponta neste ambiente: `CREATE TABLE` e leitura de `produto` sao negados para `artisanvault_app`, enquanto `SELECT`/`INSERT`/`UPDATE`/`DELETE` nas tabelas da aplicacao funcionam, e a aplicacao sobe normalmente com o Flyway rodando via `postgres` e o restante via `artisanvault_app`.

### Rotacao de segredos: ja tinha sido feita

O commit `a2573c3` ("Move segredos... para fora do git") ja rotacionou tanto `jwt.secret` quanto a senha do Postgres ao mesmo tempo em que os moveu para `application-local.properties` (gitignored) — nao era so uma realocacao dos mesmos valores. Este item estava marcado como pendente no checklist por engano; na pratica ja estava resolvido antes desta rodada.

### Risco remanescente (fora do alcance de uma mudanca de codigo)

Servir a aplicacao por HTTPS real e ativar `COOKIE_SECURE=true` contra um dominio de producao de verdade depende de ter um ambiente de deploy real, que nao existe neste projeto (so roda em `localhost` ate agora). Existe um exemplo pronto de como isso ficaria em `deploy/docker-compose.prod.yml` (`backend/Dockerfile` + Caddy como proxy reverso com HTTPS automatico via Let's Encrypt + Redis para o rate limit distribuido), com `COOKIE_SECURE=true`/`TRUST_PROXY_HEADERS=true`/`RATE_LIMIT_STORE=redis` ja configurados nele — falta apontar um dominio real (`DOMAIN` em `deploy/.env.prod`) e subir isso em um servidor de verdade para validar.

## 12. Novos achados da releitura: e-mail e identidade

Status: `[RESOLVIDO]`.

### 12.1. Colisao de e-mail entre cliente e artista

`cliente.email` era unico dentro da tabela `cliente`, mas `artista.email` nao tinha `UNIQUE` nem `NOT NULL`, e nao existia garantia global de que o mesmo e-mail nao pudesse existir nas duas tabelas. Como `LoginService` procura primeiro em `ClienteRepository` e so depois em `ArtistaRepository`, uma colisao de e-mail podia causar bloqueio de login, confusao de identidade ou comportamento imprevisivel.

O que mudou:

- `ArtistaService.save`/`update` e `ClienteService.save`/`update` agora consultam as duas tabelas (`ArtistaRepository`/`ClienteRepository`) antes de criar ou atualizar e lancam `EmailAlreadyInUseException` se o e-mail normalizado ja existir em qualquer uma delas. `ArtistaController`/`ClienteController` traduzem essa excecao para `409 Conflict`.
- Migracao Flyway `V2__email_normalizacao_unicidade.sql` normaliza os e-mails existentes (`LOWER(TRIM(email))`) e cria indices unicos case-insensitive por tabela (`ux_cliente_email_lower`, `ux_artista_email_lower`; a antiga constraint `UNIQUE` case-sensitive de `cliente.email` foi substituida pelo indice). `artista.email` passou a ser `NOT NULL`. Isso cobre duplicatas dentro da mesma tabela sob concorrencia, que a checagem do backend sozinha nao evitaria; a checagem cross-table (cliente vs. artista) continua sendo responsabilidade do backend, ja que Postgres nao suporta constraint de unicidade entre tabelas diferentes.
- Testado ponta a ponta neste ambiente (Flyway aplicou a migracao no banco de dev real, schema foi para a versao 2): criar cliente com e-mail ja usado por um artista existente retorna `409`; criar artista com e-mail ja usado por um cliente existente retorna `409`; cadastro com e-mail novo continua funcionando (`201`).
- A solucao mais robusta no longo prazo continua sendo centralizar autenticacao em uma tabela unica de usuario com perfil de cliente/artista separado — fora do escopo desta correcao pontual (mudaria o schema e varios fluxos de autorizacao).

### 12.2. Normalizacao inconsistente de e-mail

`LoginController` transformava o e-mail de login com `trim().toLowerCase()`, `ClienteRepository` buscava com `LOWER(email)`, mas `ArtistaRepository` ainda usava `email = ?` (comparacao exata). Isso permitia divergencia por maiusculas/minusculas e podia impedir login de artista cadastrado com casing diferente.

O que mudou:

- Novo utilitario `EmailNormalizer.normalize` (`trim().toLowerCase()`, null-safe) usado em `LoginController`, `ArtistaService` e `ClienteService` antes de qualquer busca, criacao ou atualizao por e-mail.
- `ArtistaRepository.findByEmail` passou a comparar com `LOWER(email) = ?`, no mesmo padrao ja usado por `ClienteRepository.findByEmail`.
- `ArtistaService.save`/`update` e `ClienteService.save`/`update` normalizam e persistem o e-mail antes de gravar (antes o valor cru do corpo da requisicao ia direto para o banco).
- Coberto por testes novos em `ArtistaServiceTest`/`ClienteServiceTest` (normalizacao antes de salvar, colisao de e-mail entre tipos de usuario).

## Checklist atualizado

- [x] Remover `GET /**` publico.
- [x] Criar lista explicita de endpoints GET publicos.
- [x] Validar dono em `PUT /api/artistas/{id}`.
- [x] Validar dono em deletes de artista, cliente, pedido, portfolio, servico e arte.
- [x] Mover segredos para arquivo local ignorado pelo Git.
- [x] Restringir `GET /api/cliente` e `GET /api/cliente/{id}` (removidos; substituidos por `/api/cliente/me`).
- [x] Restringir `GET /api/pedido` e `GET /api/pedido/{id}` (listagem global removida; `/{id}` exige ser dono).
- [x] Criar endpoints de pedidos filtrados no backend (`/meus`, `/recebidos`).
- [x] Remover filtros de seguranca feitos apenas no frontend.
- [x] Validar dono/papel em `POST /api/arte/post`.
- [x] Validar upload por conteudo real (magic bytes + `ImageIO`).
- [x] Reencodar imagens a partir dos pixels decodificados antes de salvar (WEBP removido por nao ter encoder nativo no JDK).
- [x] Adicionar headers seguros para `/uploads/**` (`X-Content-Type-Options: nosniff` via Spring Security).
- [x] Trocar JWT em `localStorage` por cookie `HttpOnly`.
- [x] Manter `withCredentials: true` (agora necessario para o cookie de sessao).
- [x] Derivar `AuthUser` no `AuthContext` a partir da resposta autenticada do backend (`GET /api/login/me`), nao do `localStorage`.
- [x] Remover leitura residual de `artisanvault_user` em `app/login/page.tsx`; redirecionamento pos-login agora usa o `AuthUser` retornado por `login()`.
- [x] Impedir colisao de e-mail entre `cliente` e `artista` (checagem cross-table no backend antes de salvar + indices unicos case-insensitive por tabela via Flyway; unificar em uma tabela de autenticacao continua sendo a melhoria de longo prazo, fora de escopo).
- [x] Normalizar e-mail de forma consistente em cadastro, update e busca (`EmailNormalizer.normalize` + `ArtistaRepository`/`ClienteRepository` ambos com `LOWER(email) = ?`).
- [x] Avaliar necessidade de DTOs de entrada/saida (Portifolio publico corrigido; demais entidades avaliadas e sem exposicao real apos as correcoes de listagem).
- [x] Omitir `id_cliente`/`id_pedido` das respostas publicas de portfolio.
- [x] Remover metodo morto `artistaService.login(email, senha)` do frontend.
- [x] Adicionar rate limit no login.
- [x] So confiar em `X-Forwarded-For` atras de proxy explicitamente configurado (`TRUST_PROXY_HEADERS`).
- [ ] Migrar rate limit para armazenamento distribuido se houver multiplas instancias (suporte opt-in ja implementado em `RedisLoginRateLimiterService`/`RATE_LIMIT_STORE=redis`; nao testado contra um Redis real).
- [x] Adicionar protecao CSRF explicita (cookie `XSRF-TOKEN` + header `X-XSRF-TOKEN`).
- [x] Atualizar dependencias do frontend.
- [x] Atualizar dependencias do backend.
- [x] Adotar migracoes versionadas (Flyway) com baseline refletindo o schema real.
- [x] Tornar `ddl-auto=validate` o padrao em todos os ambientes, com mudancas de schema feitas via migracao.
- [x] Rotacionar segredos antigos que ja tenham sido expostos (feito no commit `a2573c3`, antes desta rodada).
- [x] Usar usuario de banco com privilegios minimos (`artisanvault_app`, provisionado via `db/provision-app-role.sql`; Flyway roda com usuario separado com privilegio de DDL).
- [x] Tornar `show-sql`, `cookie.secure` e `trust-proxy-headers` configuraveis por ambiente, com defaults seguros de desenvolvimento.
- [ ] Definir `COOKIE_SECURE=true` no ambiente de producao (exige HTTPS e um dominio real de deploy; exemplo pronto em `deploy/docker-compose.prod.yml`, nao implantado).
- [x] Adicionar testes de integracao para autorizacao dos controllers (arte, pedido), upload invalido/reencodificacao e rate limit.

## Itens fora do escopo (dependem de infraestrutura real de producao)

1. Migrar para Spring Boot 4.x (major upgrade, fora do escopo de uma correcao de seguranca).
2. Rate limit distribuido (Redis) caso o backend passe a rodar em multiplas instancias — codigo pronto e opt-in (`RedisLoginRateLimiterService`), mas requer um Redis de verdade para ativar e validar, que nao existe neste projeto.
3. Servir a aplicacao por HTTPS com `COOKIE_SECURE=true` em producao — template de deploy pronto (`deploy/docker-compose.prod.yml`, Caddy + Redis + backend), mas requer um dominio/certificado real para implantar e validar, que nao existe neste projeto.

## Deploy de producao (opt-in, nao implantado)

`deploy/` contem um exemplo completo de como ativar os dois itens pendentes acima quando houver um servidor/dominio de producao real:

- `backend/Dockerfile`: build multi-stage do backend (`eclipse-temurin` JDK para build, JRE para runtime, usuario nao-root).
- `deploy/docker-compose.prod.yml`: sobe `backend` + `redis` (`RATE_LIMIT_STORE=redis`) + `caddy` (proxy reverso com HTTPS automatico via Let's Encrypt, `COOKIE_SECURE=true`, `TRUST_PROXY_HEADERS=true`). O Postgres de producao e assumido como gerenciado externamente (RDS, Supabase etc.), nao faz parte deste compose.
- `deploy/Caddyfile`: site block minimo, le o dominio de `$DOMAIN`.
- `deploy/.env.prod.example`: template das variaveis reais necessarias (`DOMAIN`, credenciais de banco, `JWT_SECRET`). Copiar para `.env.prod` (ja no `.gitignore` do diretorio) e preencher antes de subir.

Nada disso e usado em desenvolvimento local ou CI — e roda apenas se alguem explicitamente subir esse compose contra um servidor com um dominio real apontado para ele. Nao foi implantado nem testado ponta a ponta neste ambiente (sem servidor/dominio disponivel aqui).
