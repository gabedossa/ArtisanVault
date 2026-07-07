# ArtisanVault - Metodos de invasao e correcoes

Atualizado em: 2026-07-07 (inclui rodada de teste de invasao ativo, secao 13)

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
- Rodada de teste de invasao ativo (ataques de verdade contra uma instancia rodando neste ambiente, nao so leitura de codigo) encontrou e corrigiu 3 vulnerabilidades reais: uma race condition que permitia o mesmo e-mail existir simultaneamente em `cliente` e `artista` (contornando a correcao da secao 12.1), cadastro aceitando senha vazia/de 1 caractere direto na API (a validacao so existia no HTML do frontend), e o JWT continuar valido via header `Authorization` mesmo depois de "logout". Ver secao 13 para o que foi tentado, o que resistiu e o que foi corrigido.

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
- **Atualizacao (secao 13.1): a checagem cross-table descrita acima tinha uma race condition e foi substituida por uma tabela `email_registro` com constraint real.** Ver secao 13.1 para os detalhes — o restante desta secao (12.1) descreve o que foi feito nessa rodada anterior, mas o mecanismo de fato usado hoje e o da secao 13.1.

### 12.2. Normalizacao inconsistente de e-mail

`LoginController` transformava o e-mail de login com `trim().toLowerCase()`, `ClienteRepository` buscava com `LOWER(email)`, mas `ArtistaRepository` ainda usava `email = ?` (comparacao exata). Isso permitia divergencia por maiusculas/minusculas e podia impedir login de artista cadastrado com casing diferente.

O que mudou:

- Novo utilitario `EmailNormalizer.normalize` (`trim().toLowerCase()`, null-safe) usado em `LoginController`, `ArtistaService` e `ClienteService` antes de qualquer busca, criacao ou atualizao por e-mail.
- `ArtistaRepository.findByEmail` passou a comparar com `LOWER(email) = ?`, no mesmo padrao ja usado por `ClienteRepository.findByEmail`.
- `ArtistaService.save`/`update` e `ClienteService.save`/`update` normalizam e persistem o e-mail antes de gravar (antes o valor cru do corpo da requisicao ia direto para o banco).
- Coberto por testes novos em `ArtistaServiceTest`/`ClienteServiceTest` (normalizacao antes de salvar, colisao de e-mail entre tipos de usuario).

## 13. Teste de invasao ativo

Status: `[RESOLVIDO]` para os 3 achados reais; os demais vetores tentados nao renderam vulnerabilidade.

Diferente das secoes anteriores (leitura de codigo + correcao), esta rodada subiu uma instancia real do backend neste ambiente (porta separada da instancia de desenvolvimento do usuario, que ficou intacta o tempo todo) contra o Postgres de desenvolvimento real, e atacou de verdade via `curl`/`psql`: contas de teste (cliente e artista), tokens JWT de verdade, requisicoes concorrentes de verdade. Toda linha de teste criada foi apagada ao final.

### 13.1. Race condition na unicidade de e-mail entre cliente e artista (CORRIGIDO)

A checagem "consulta as duas tabelas, depois insere" da secao 12.1 (feita na aplicacao, sem nenhuma trava compartilhada entre `ClienteService` e `ArtistaService`) tinha uma janela de corrida. Confirmado disparando 8 requisicoes concorrentes (`POST /api/cliente/post` e `POST /api/artistas` simultaneos, mesmo e-mail): **o mesmo e-mail foi aceito nas duas tabelas ao mesmo tempo**, o exato cenario que a secao 12.1 deveria impedir.

Causa raiz: Postgres nao suporta `UNIQUE` entre tabelas diferentes, e duas requisicoes concorrentes (uma criando cliente, outra criando artista) podem passar pela checagem "o e-mail existe em alguma das duas tabelas?" ao mesmo tempo, antes de qualquer uma commitar seu INSERT.

Correcao: migracao Flyway `V3__registro_email_unico.sql` cria uma tabela `email_registro (email PRIMARY KEY)` com uma linha por e-mail em uso (por qualquer tipo de conta), populada com os dados existentes. `ArtistaService`/`ClienteService.save`/`update`/`deleteById` agora sao `@Transactional` e usam `EmailRegistroRepository.tryReserve`/`release` (um `INSERT`/`DELETE` de verdade nessa tabela) dentro da mesma transacao do `INSERT`/`UPDATE`/`DELETE` em `cliente`/`artista` — o `UNIQUE` real de `email_registro` e o que fecha a janela de corrida, nao mais uma checagem "ler depois escrever" na aplicacao.

Retestado com a mesma requisicao concorrente apos a correcao: de 8 requisicoes simultaneas, exatamente 1 foi aceita (`201`) e as outras 7 (incluindo as do outro tipo de conta) receberam `409`, com `email_registro` e a tabela vencedora com exatamente 1 linha cada.

**Nota operacional encontrada durante o reteste:** como o Flyway roda com um usuario separado (com privilegio de DDL, ex. `postgres`) do usuario de runtime restrito (`artisanvault_app`, so DML), uma tabela nova criada por uma migracao **nao** fica automaticamente acessivel para `artisanvault_app` — e preciso reaplicar o `GRANT` (agora incluido em `db/provision-app-role.sql`) manualmente em qualquer banco que ja tinha o papel restrito provisionado antes desta migracao rodar (o banco de dev deste ambiente precisou disso; um banco novo que rode `provision-app-role.sql` do zero ja pega o `GRANT` atualizado). Vale lembrar disso para qualquer migracao futura que crie tabela nova.

### 13.2. Cadastro aceitava senha vazia/fraca direto na API (CORRIGIDO)

O `minLength={6}` dos formularios de cadastro (`app/cadastro/cliente`, `app/cadastro/artista`) e so validacao HTML no navegador. `ArtistaService.save`/`update` e `ClienteService.save`/`update` nunca validavam o tamanho da senha — confirmado criando uma conta com senha `"1"` e outra com senha `""` diretamente via `POST /api/cliente/post` (sem passar pelo formulario), ambas aceitas (`201`) e capazes de logar em seguida.

Correcao: `ArtistaService`/`ClienteService` agora rejeitam (`WeakPasswordException` → `400`) senha nula ou com menos de 6 caracteres, tanto na criacao quanto na troca de senha no update (o "manter senha atual se o campo vier em branco" continua funcionando sem disparar a validacao). Retestado: senha de 1 caractere e senha vazia agora retornam `400`; senha de 6+ caracteres continua funcionando normalmente.

### 13.3. JWT continuava valido apos logout (CORRIGIDO)

`POST /api/login/logout` sempre so expirou o cookie no navegador — o JWT em si e stateless e continuava criptograficamente valido ate o `exp` original (24h). Confirmado: logando, copiando o token do cookie, chamando `/api/login/logout`, e reusando o mesmo token via header `Authorization: Bearer` — a API continuava autenticando normalmente com o token "deslogado".

Correcao: `JwtService.generateToken` passou a incluir um `jti` (id unico) em cada token. Novo `TokenBlocklistService` (interface + `InMemoryTokenBlocklistService`/`RedisTokenBlocklistService`, mesmo padrao opt-in de `app.token-blocklist.store`/`TOKEN_BLOCKLIST_STORE` usado pelo rate limiter de login) registra o `jti` do token atual no logout, com TTL ate a expiracao original do token. `JwtAuthenticationFilter` passou a rejeitar qualquer token cujo `jti` esteja na blocklist. Tokens emitidos antes desta mudanca (sem `jti`) nao quebram — simplesmente nao se beneficiam da invalidacao no logout ate expirarem naturalmente.

Retestado: o mesmo token que funcionava via `Authorization: Bearer` antes do logout passou a retornar `403` depois do logout.

### 13.4. Vetores tentados que nao renderam vulnerabilidade

Para registrar o que foi testado e resistiu (nao e uma lista exaustiva de tudo que existe, mas cobre os vetores mais comuns e alguns mais elaborados):

- **SQL injection**: todo acesso a banco usa `JdbcTemplate` com `?` parametrizado (confirmado por busca no codigo, sem nenhuma concatenacao de SQL); payloads classicos (`' OR '1'='1`, `'; DROP TABLE cliente; --`) no login nao tiveram efeito.
- **Falsificacao de JWT**: token com `alg: none` sem assinatura, e token com payload alterado (`CLIENTE` → `ARTISTA`) mas assinatura antiga reaproveitada — ambos rejeitados (`Jwts.parser().verifyWith(key)` exige assinatura valida, biblioteca `jjwt` moderna nao aceita token nao assinado).
- **IDOR**: testado sistematicamente em todos os controllers (artista, cliente, servico, portfolio, arte, pedido) com uma segunda conta tentando ver/editar/excluir/entregar recursos de outra — todas as tentativas bloqueadas com `403`.
- **Mass assignment / escalada de privilegio**: setar `tipoUsuario: "ADMIN"` no corpo do `PUT` de artista e aceito e persistido na coluna (ja documentado como risco cosmetico na secao 2), mas confirmado que isso **nao** concede nenhuma autoridade real — o JWT/`ROLE_*` continuam vindo so do que o login resolveu, nao dessa coluna.
- **Upload de arquivo malicioso**: texto puro, binario `MZ` (executavel Windows) e SVG (`<svg onload=...>`) disfarcados de PNG, arquivo vazio e arquivo de 6MB — todos rejeitados pela validacao de magic bytes/tamanho/formato existente. Nome de arquivo com path traversal (`../../../../windows/win.ini`) e ignorado (o nome salvo sempre e um UUID gerado no servidor).
- **CSRF**: requisicao de mutacao sem `X-XSRF-TOKEN`, ou com o header presente mas nao correspondente ao cookie, sempre recebeu `403`.
- **Rate limit / bypass por `X-Forwarded-For`**: apos 5 tentativas de login com senha errada, a 6a (mesmo com a senha certa) recebe `429`; forjar `X-Forwarded-For` nao teve efeito com `TRUST_PROXY_HEADERS=false` (default).
- **CORS**: preflight com `Origin` arbitrario recebe `403` sem `Access-Control-Allow-Origin`; so `http://localhost:3000` (a origem configurada) recebe os headers de CORS liberando a requisicao.
- **Vazamento de informacao em erros**: JSON malformado, path variable nao numerico, corpo vazio — nenhum retornou stack trace ou detalhe interno (retornam respostas genericas; ver nota abaixo sobre o efeito colateral encontrado nisso).
- **Metodos HTTP**: `TRACE` desabilitado pelo Tomcat por padrao; verbos nao mapeados (`PATCH` em rotas sem handler) falham fechado (autenticacao exigida por padrao), nao expondo nada.

**Observacao (nao e vulnerabilidade, so uma inconsistencia de status HTTP):** `/error` nao esta na lista de rotas publicas do `SecurityConfig`, entao quando uma excecao nao tratada forca um forward interno para `/error` (ex.: JSON malformado), o Spring Security barra esse forward para quem nao esta autenticado e devolve `403` com corpo vazio em vez do `400`/`500` que seria mais correto semanticamente. Isso acaba sendo inofensivo por acidente (nenhum detalhe do erro real vaza — a resposta fica ainda mais vazia do que o padrao do Spring Boot), mas fica registrado aqui caso alguem se depare com respostas `403` inesperadas para requisicoes malformadas e queira investigar.

### 13.5. Enumeracao de e-mail via cadastro: avaliado, aceito como risco conhecido

`POST /api/cliente/post`/`POST /api/artistas` respondem `409` com a mensagem "e-mail ja em uso" sem nenhum limite de tentativas proprio (diferente do login, que tem `LoginRateLimiterService`). Isso permite a alguem descobrir se um e-mail especifico ja tem conta na plataforma testando-o direto no endpoint de cadastro. E um padrao comum (muitos sites revelam "e-mail ja cadastrado" no cadastro por motivos de UX) e de severidade baixa comparado aos 3 achados acima — decidido nao aplicar rate limit aqui nesta rodada para nao aumentar o escopo da correcao; fica registrado como risco aceito, nao como pendencia.

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
- [x] Fechar a race condition entre a checagem de e-mail duplicado de cliente/artista e o INSERT (tabela `email_registro` com `UNIQUE` real + `@Transactional`, migracao `V3`).
- [x] Validar tamanho minimo de senha no backend (nao so no HTML do formulario) em cadastro e troca de senha.
- [x] Invalidar o JWT no logout (blocklist por `jti`, opt-in memoria/Redis), nao so limpar o cookie no navegador.

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
