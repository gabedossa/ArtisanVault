# ArtisanVault - Metodos de invasao e correcoes

Atualizado em: 2026-07-07

Este documento descreve os principais caminhos de invasao identificados no projeto ArtisanVault, o status atual de cada ponto e as correcoes recomendadas. O objetivo e defensivo: registrar o que ainda precisa ser corrigido e evitar que controles de seguranca fiquem apenas no frontend.

## Status geral

Todos os pontos praticaveis dentro do repositorio (sem depender de infraestrutura real de producao) foram tratados. Isso inclui os itens desta rodada:

- `GET /api/artistas/me` nao existia como tal; em vez disso, foi criado `GET /api/login/me`, que resolve a identidade autenticada (e-mail/tipo/id/nome) direto do backend a partir do JWT, cobrindo tanto artista quanto cliente. O frontend (`AuthContext.tsx`) parou de usar `localStorage` para reconstruir a identidade da sessao e passou a chamar esse endpoint a cada carregamento.
- Investigadas as entidades expostas diretamente pela API (`Artista`, `Cliente`, `Portifolio`, `Servico`, `Arte`, `Pedido`): a unica exposicao publica indevida encontrada foi `id_cliente`/`id_pedido` em `GET /api/portifolio` e `GET /api/portifolio/{id}`, que ja foi corrigida com um DTO publico dedicado. Os demais casos (`Cliente`, `Pedido`) so sao retornados para o proprio dono depois das correcoes anteriores, e `senha` ja e `@JsonProperty(WRITE_ONLY)` em `Artista`/`Cliente`. Ver secao 2/3 para o detalhamento e a justificativa de nao criar DTOs onde nao ha exposicao real.
- Removido `artistaService.login(email, senha)` do frontend: metodo morto que chamava `/api/artistas/login`, endpoint que nunca existiu no backend (nunca era invocado por nenhuma tela).
- O rate limit de login agora so confia no header `X-Forwarded-For` quando `TRUST_PROXY_HEADERS=true` estiver definido; por padrao usa o IP real da conexao TCP, evitando que um cliente falsifique o header para contornar o limite por IP.
- Adicionados testes automatizados de seguranca: autorizacao (dono/nao-dono/nao-encontrado) em `ArteController` e `PedidoController`, validacao de upload invalido/valido em `ImageStorageService`, e comportamento do rate limiter em `LoginRateLimiterService`.

Legenda:

- `[RESOLVIDO]`: o ponto principal foi tratado no codigo atual.
- `[PARCIAL]`: houve melhora, mas ainda existe risco remanescente.
- `[PENDENTE]`: o risco continua aberto.
- `[AVALIADO/NAO E PROBLEMA]`: investigado e descartado como risco real neste projeto, com justificativa.

## O que ainda falta ajustar

Itens genuinamente pendentes, todos dependentes de infraestrutura real ou de uma decisao de produto que ainda nao existe neste projeto:

1. `[PENDENTE]` Reprocessar/reencodar todas as imagens do zero antes de salvar. WEBP nao tem decoder nativo no `ImageIO` do JDK, entao hoje so tem validacao por assinatura binaria para esse formato (ver secao 7).
2. `[PENDENTE]` Adicionar protecao CSRF explicita **se** a politica de cookie mudar (`SameSite=None`, subdominios, etc.). Hoje `SameSite=Lax` ja mitiga o cenario atual de origem unica.
3. `[PENDENTE]` Migrar o rate limit para armazenamento distribuido (Redis/Bucket4j) se o backend passar a rodar em multiplas instancias atras de um load balancer.
4. `[PENDENTE]` Fechar configuracao real de producao: rotacionar segredos antigos, criar usuario de banco com privilegios minimos, definir `DDL_AUTO=validate`, `SHOW_SQL=false`, `COOKIE_SECURE=true`, `TRUST_PROXY_HEADERS=true` (se houver proxy confiavel na frente) e servir tudo por HTTPS.
5. `[PENDENTE]` Adotar migracoes versionadas (Flyway/Liquibase) antes de producao real, e manter auditoria recorrente de dependencias (`npm audit`, Maven/OSV ou equivalente em CI).

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

Status: `[RESOLVIDO]` para validacao de conteudo; `[PARCIAL]` para reprocessamento completo.

### O que mudou

`ImageStorageService.store` agora:

1. Le os bytes do arquivo em memoria.
2. Valida a assinatura binaria (magic bytes) de JPEG, PNG, GIF e WEBP contra o `Content-Type` declarado.
3. Para JPEG/PNG/GIF (formatos com decoder nativo no `ImageIO` do JDK), decodifica a imagem via `ImageIO.read` e rejeita se o resultado for nulo.
4. Salva os bytes validados em disco com nome gerado por `UUID`.

`X-Content-Type-Options: nosniff` ja e enviado por padrao pelo Spring Security em todas as respostas, incluindo `/uploads/**`, sem necessidade de configuracao adicional.

Testes automatizados em `ImageStorageServiceTest` cobrem: conteudo que nao e imagem real (rejeitado mesmo com `Content-Type` de imagem), PNG valido (aceito), `Content-Type` nao suportado (rejeitado) e arquivo vazio (rejeitado).

### Risco remanescente

- WEBP nao possui decoder nativo no `ImageIO` do JDK sem plugin adicional; para esse formato a validacao fica restrita a assinatura binaria (nao ha decodificacao completa).
- O arquivo original validado e salvo como está (sem re-encodar a imagem do zero). Reencodar todas as imagens re-processando pixels removeria metadados/payloads residuais com uma camada extra de seguranca, mas exigiria lidar com a limitacao do WEBP acima (provavelmente descartando esse formato ou adicionando uma biblioteca externa). Pode ser feito como uma melhoria futura caso uploads de terceiros nao confiaveis passem a ser um vetor mais critico.

## 8. JWT em localStorage

Status: `[RESOLVIDO]`.

### O que mudou

- `POST /api/login` agora define o JWT em um cookie `HttpOnly`, `SameSite=Lax`, `Path=/`, com `Secure` controlado pela variavel de ambiente `COOKIE_SECURE` (usar `true` atras de HTTPS em producao). O corpo da resposta (`LoginResponse`) nao inclui mais o token (`@JsonIgnore`), entao nem o proprio JavaScript da aplicacao consegue le-lo.
- `JwtAuthenticationFilter` aceita o token tanto via header `Authorization: Bearer` (compatibilidade) quanto via cookie `artisanvault_token`.
- Novo endpoint `POST /api/login/logout` expira o cookie no servidor.
- Novo endpoint `GET /api/login/me` retorna a identidade autenticada (e-mail/tipo/id/nome) resolvida no backend a partir do JWT. O frontend (`AuthContext.tsx`) usa esse endpoint como unica fonte de verdade da sessao: nao ha mais `localStorage.setItem`/`getItem` para dados de usuario em nenhum lugar do app. A cada carregamento de pagina, a identidade e buscada do backend; se o cookie for invalido/expirado, o usuario simplesmente aparece como nao autenticado.
- `withCredentials: true` foi mantido (agora e o mecanismo real de transporte do cookie).

### Risco remanescente

CSRF classico e mitigado por `SameSite=Lax` (cookies nao sao enviados em POST/PUT/DELETE disparados por outro site), mas nao ha um token CSRF explicito. Se o app crescer para múltiplos subdominios ou precisar de `SameSite=None`, adicionar protecao CSRF explicita (dupla submissao de cookie ou header customizado) deve ser revisitado.

## 9. Login sem rate limit

Status: `[RESOLVIDO]`.

`LoginRateLimiterService` mantem, em memoria, uma janela deslizante de 5 minutos por chave (`email:` e `ip:`). Apos 5 tentativas na janela, `POST /api/login` responde `429 Too Many Requests` e novas tentativas (mesmo com senha correta) sao bloqueadas ate a janela expirar. O contador e zerado no login bem-sucedido. Coberto por `LoginRateLimiterServiceTest` (bloqueio por e-mail, por IP, ausencia de bloqueio abaixo do limite, `reset` e isolamento entre chaves diferentes).

### X-Forwarded-For: corrigido

O IP usado na chave de rate limit so vem do header `X-Forwarded-For` quando `TRUST_PROXY_HEADERS=true` estiver explicitamente configurado (para ambientes atras de um proxy/load balancer confiavel que sobrescreve esse header). Por padrao (`false`), usa `request.getRemoteAddr()`, o IP real da conexao TCP — evitando que um cliente malicioso falsifique o header para contornar o limite por IP ou para forjar tentativas em nome do IP de outra pessoa.

### Risco remanescente

A implementacao e em memoria (nao distribuida). Se o backend rodar em multiplas instancias atras de um load balancer, cada instancia tera seu proprio contador. Para esse cenario, migrar para Redis (ex.: Bucket4j + Redis) seria o proximo passo natural.

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
- Suite de testes (`./mvnw test`) executada com sucesso: 34/34 testes passando (13 originais + 21 novos de seguranca).

### Observacao

Uma futura migracao para Spring Boot 4.x deve ser tratada como um projeto a parte (major upgrade, possiveis breaking changes em Spring Security/Jakarta), nao como parte de uma correcao de seguranca pontual.

## 11. Segredos e configuracao

Status: `[RESOLVIDO]` para os itens que dependem apenas de codigo/configuracao do repositorio.

### O que mudou

`application.properties` agora le de variaveis de ambiente, com defaults seguros para desenvolvimento local:

```properties
spring.datasource.username=${DB_USERNAME:postgres}
spring.jpa.hibernate.ddl-auto=${DDL_AUTO:update}
spring.jpa.show-sql=${SHOW_SQL:true}
app.cookie.secure=${COOKIE_SECURE:false}
app.trust-proxy-headers=${TRUST_PROXY_HEADERS:false}
```

Em producao, basta definir `DB_USERNAME` (usuario com privilegios minimos), `DDL_AUTO=validate`, `SHOW_SQL=false`, `COOKIE_SECURE=true` (exige HTTPS) e `TRUST_PROXY_HEADERS=true` (somente se houver um proxy confiavel na frente) sem tocar no codigo.

### Risco remanescente (fora do alcance de uma mudanca de codigo)

- Criar de fato um usuario de banco com privilegios minimos e rotacionar segredos ja expostos depende do ambiente de infraestrutura real (servidor Postgres de producao), nao apenas do repositorio.
- Adotar Flyway/Liquibase para migracoes controladas continua sendo uma melhoria arquitetural maior (exigiria escrever o historico de migracoes para o schema existente) e deve ser um projeto proprio caso o projeto va para producao de verdade.

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
- [x] Validar upload por conteudo real (magic bytes + `ImageIO` para formatos suportados).
- [ ] Reprocessar/reencodar todas as imagens do zero antes de salvar (parcial: WEBP nao tem decoder nativo no JDK).
- [x] Adicionar headers seguros para `/uploads/**` (`X-Content-Type-Options: nosniff` via Spring Security).
- [x] Trocar JWT em `localStorage` por cookie `HttpOnly`.
- [x] Manter `withCredentials: true` (agora necessario para o cookie de sessao).
- [x] Derivar `AuthUser` da resposta autenticada do backend (`GET /api/login/me`), nao do `localStorage`.
- [x] Avaliar necessidade de DTOs de entrada/saida (Portifolio publico corrigido; demais entidades avaliadas e sem exposicao real apos as correcoes de listagem).
- [x] Omitir `id_cliente`/`id_pedido` das respostas publicas de portfolio.
- [x] Remover metodo morto `artistaService.login(email, senha)` do frontend.
- [x] Adicionar rate limit no login.
- [x] So confiar em `X-Forwarded-For` atras de proxy explicitamente configurado (`TRUST_PROXY_HEADERS`).
- [ ] Migrar rate limit para armazenamento distribuido se houver multiplas instancias.
- [x] Atualizar dependencias do frontend.
- [x] Atualizar dependencias do backend.
- [ ] Rotacionar segredos antigos que ja tenham sido expostos (depende do ambiente real de producao).
- [ ] Usar usuario de banco com privilegios minimos (depende do ambiente real de producao; configuravel via `DB_USERNAME`).
- [x] Tornar `show-sql`, `ddl-auto`, `cookie.secure` e `trust-proxy-headers` configuraveis por ambiente, com defaults seguros de desenvolvimento.
- [ ] Definir `COOKIE_SECURE=true`, `DDL_AUTO=validate` e `SHOW_SQL=false` no ambiente de producao.
- [x] Adicionar testes de integracao para autorizacao dos controllers (arte, pedido), upload invalido e rate limit.

## Itens fora do escopo desta rodada (dependem de infraestrutura real ou decisao de produto)

1. Provisionar um usuario de banco de dados com privilegios minimos em um Postgres de producao real.
2. Rotacionar segredos que eventualmente ja tenham vazado antes desta auditoria.
3. Adotar Flyway/Liquibase para versionar o schema (projeto arquitetural proprio).
4. Migrar para Spring Boot 4.x (major upgrade, fora do escopo de uma correcao de seguranca).
5. Rate limit distribuido (Redis) caso o backend passe a rodar em multiplas instancias.
6. Protecao CSRF explicita, caso a politica de cookie mude para `SameSite=None`/multiplos subdominios.
7. Reencodar todas as imagens a partir dos pixels (e decidir o que fazer com WEBP), caso uploads de terceiros nao confiaveis se tornem um vetor mais critico.
