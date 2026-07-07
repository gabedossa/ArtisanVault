# ArtisanVault - Metodos de invasao e correcoes

Atualizado em: 2026-07-07

Este documento descreve os principais caminhos de invasao identificados no projeto ArtisanVault, o status atual de cada ponto e as correcoes recomendadas. O objetivo e defensivo: registrar o que ainda precisa ser corrigido e evitar que controles de seguranca fiquem apenas no frontend.

## Status geral

O projeto melhorou desde a primeira auditoria. O `GET /**` publico foi removido, e varios endpoints de alteracao/exclusao agora verificam o dono do recurso. Mesmo assim, ainda existem riscos importantes:

- Usuarios autenticados ainda conseguem listar dados globais de clientes e pedidos.
- O frontend ainda baixa todos os pedidos e filtra localmente.
- Upload de imagens ainda confia no `Content-Type` enviado pelo cliente.
- JWT ainda fica em `localStorage`.
- Login ainda nao tem rate limit.
- Dependencias do frontend e backend ainda precisam atualizacao.
- `POST /api/arte/post` ainda permite criar arte sem validar dono/papel.
- Configuracoes de producao ainda precisam endurecimento.

Legenda:

- `[RESOLVIDO]`: o ponto principal foi tratado no codigo atual.
- `[PARCIAL]`: houve melhora, mas ainda existe risco remanescente.
- `[PENDENTE]`: o risco continua aberto.

## 1. Exposicao publica de endpoints GET

Status: `[RESOLVIDO]` para o `GET /**` global, `[PARCIAL]` para exposicao a usuarios autenticados.

### Como era explorado

Antes, qualquer pessoa sem login podia consultar endpoints `GET` como:

- `GET /api/cliente`
- `GET /api/cliente/{id}`
- `GET /api/pedido`
- `GET /api/pedido/{id}`
- `GET /api/artistas/email?email=...`

Isso permitia enumerar usuarios, pedidos, e-mails, telefones e IDs internos.

### Estado atual

O `SecurityConfig` nao libera mais `GET /**`. Agora existe uma lista explicita de GETs publicos:

```java
.requestMatchers(HttpMethod.GET, "/api/artistas/email").authenticated()
.requestMatchers(HttpMethod.GET,
        "/api/artistas", "/api/artistas/*",
        "/api/servico", "/api/servico/*",
        "/api/portifolio", "/api/portifolio/*",
        "/uploads/**"
).permitAll()
```

Isso corrige a exposicao publica ampla. Porem, endpoints como `/api/cliente` e `/api/pedido` ainda existem e retornam listas completas quando acessados por qualquer usuario autenticado.

### Risco remanescente

Um usuario autenticado, mesmo sem permissao administrativa, pode chamar:

- `GET /api/cliente`
- `GET /api/pedido`

e receber dados globais.

### Como finalizar a correcao

1. Remover ou restringir `GET /api/cliente` para admin.
2. Remover ou restringir `GET /api/pedido` para admin.
3. Criar endpoints especificos para o usuario autenticado:
   - `GET /api/pedido/meus`
   - `GET /api/pedido/recebidos`
   - `GET /api/cliente/me`
4. Nunca depender de filtro no frontend para esconder dados.

## 2. IDOR em update/delete de artista

Status: `[RESOLVIDO]` para update e delete.

### Como era explorado

Um usuario autenticado podia tentar alterar ou remover outro artista usando o ID de outra conta:

```http
PUT /api/artistas/{id_de_outro_artista}
DELETE /api/artistas/{id_de_outro_artista}
```

### Estado atual

`ArtistaController` agora recebe `Authentication`, busca o artista autenticado por e-mail e compara o ID autenticado com o ID da URL antes de editar ou excluir.

### Risco remanescente

O controle principal foi corrigido. Ainda e recomendado usar DTOs separados para update, evitando aceitar campos como `tipoUsuario` e `senha` na mesma entidade exposta pela API.

### Como fortalecer

Criar um DTO de atualizacao de perfil:

```java
public class ArtistaUpdateRequest {
    private String nome;
    private String descricao;
    private String senhaAtual;
    private String novaSenha;
}
```

Assim o backend controla explicitamente quais campos podem ser alterados.

## 3. Exclusao arbitraria por ID

Status: `[RESOLVIDO]` para os deletes principais revisados.

### Como era explorado

Um usuario autenticado podia enviar `DELETE` para IDs de recursos que nao pertenciam a ele:

- `DELETE /api/cliente/delete/{id}`
- `DELETE /api/pedido/delete/{id}`
- `DELETE /api/portifolio/delete/{id}`
- `DELETE /api/arte/delete/{id}`

### Estado atual

Foram adicionadas verificacoes de dono em:

- `ClienteController.deleteCliente`
- `PedidoController.deleteById`
- `PortifolioController.deleteById`
- `ArteController.deleteArte`
- `ArtistaController.deleteById`
- `ServicoController.deleteById`

### Risco remanescente

O fluxo de delete esta bem melhor. Ainda e recomendavel cobrir esses casos com testes automatizados, porque sao regras de autorizacao sensiveis.

### Testes recomendados

Criar testes de integracao cobrindo:

- Cliente A nao pode deletar Cliente B.
- Cliente A nao pode deletar pedido de Cliente B.
- Artista A nao pode deletar portfolio de Artista B.
- Artista A nao pode deletar arte vinculada a portfolio de Artista B.
- Usuario sem token recebe `401`.
- Usuario com token valido, mas sem propriedade, recebe `403`.

## 4. Vazamento de pedidos por filtro no frontend

Status: `[PENDENTE]`

### Metodo de invasao

O frontend ainda chama `GET /api/pedido` para obter todos os pedidos e depois filtra no navegador:

```ts
const res = await api.get<Pedido[]>('/pedido')
return all.filter((p) => p.id_cliente === idCliente)
```

Mesmo que a tela mostre apenas os pedidos do usuario, a resposta HTTP contem todos os pedidos retornados pelo backend.

### Por que ainda funciona

`PedidoController.findAlls()` ainda retorna `pedidoService.findAll()`. Nao ha endpoint backend especifico para "meus pedidos" ou "pedidos recebidos".

### Impacto

- Qualquer usuario autenticado pode obter pedidos de todos os usuarios.
- Exposicao de descricoes, status, relacoes entre cliente/artista/servico e URLs de entrega.
- Facilita mapeamento de IDs para ataques futuros.

### Como corrigir

Adicionar consultas filtradas no backend:

```java
@GetMapping("/meus")
public ResponseEntity<?> meusPedidos(Authentication authentication) {
    Optional<Cliente> cliente = clienteService.findByEmail(authentication.getName());
    if (cliente.isEmpty()) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Cliente nao encontrado.");
    }
    return ResponseEntity.ok(pedidoService.findByCliente(cliente.get().getIdCliente()));
}

@GetMapping("/recebidos")
public ResponseEntity<?> pedidosRecebidos(Authentication authentication) {
    Optional<Artista> artista = artistaService.findByEmail(authentication.getName());
    if (artista.isEmpty()) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Artista nao encontrado.");
    }
    return ResponseEntity.ok(pedidoService.findByArtista(artista.get().getIdArtista()));
}
```

Adicionar no repositorio:

```java
public List<Pedido> findByCliente(Long idCliente) {
    String sql = "SELECT * FROM pedido WHERE id_cliente = ?";
    return jdbcTemplate.query(sql, new PedidoRowMapper(), idCliente);
}

public List<Pedido> findByArtista(Long idArtista) {
    String sql = "SELECT * FROM pedido WHERE id_artista = ?";
    return jdbcTemplate.query(sql, new PedidoRowMapper(), idArtista);
}
```

Depois, atualizar o frontend para nao chamar mais `/api/pedido` em dashboards de usuario.

## 5. Listagem global de clientes

Status: `[PENDENTE]`

### Metodo de invasao

Um usuario autenticado pode consultar:

```http
GET /api/cliente
GET /api/cliente/{id}
```

e obter dados de clientes que nao pertencem a ele.

### Por que funciona

`ClienteController.getAllArtistas()` retorna `cliService.findAll()`, e `findById` retorna o cliente do ID informado sem comparar com o usuario autenticado.

### Impacto

- Exposicao de nome, e-mail e telefone de clientes.
- Enumeracao de contas.
- Apoio a phishing e tomada de alvos.

### Como corrigir

Para usuario comum, criar:

```java
@GetMapping("/me")
public ResponseEntity<?> me(Authentication authentication) {
    Optional<Cliente> cliente = cliService.findByEmail(authentication.getName());
    return cliente.<ResponseEntity<?>>map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cliente nao encontrado."));
}
```

Restringir `GET /api/cliente` e `GET /api/cliente/{id}` a administradores ou remover se nao houver papel admin no sistema.

## 6. Criacao de arte sem validacao de dono

Status: `[PENDENTE]`

### Metodo de invasao

Um usuario autenticado pode chamar:

```http
POST /api/arte/post
```

enviando um `id_portfolio` arbitrario no corpo. Como o controller nao valida se o portfolio pertence ao artista autenticado, isso permite criar arte vinculada a portfolio de terceiros.

### Por que funciona

`ArteController.createArte` recebe `Arte` diretamente e chama `artService.save(arte)` sem `Authentication`, sem checagem de papel e sem checagem de propriedade.

### Impacto

- Insercao de arte em portfolio de outro artista.
- Corrupcao de dados.
- Possivel abuso de votos/titulos/descricoes.

### Como corrigir

Receber `Authentication`, buscar o portfolio informado e validar dono:

```java
@PostMapping("/post")
public ResponseEntity<?> createArte(@RequestBody Arte arte, Authentication authentication) {
    Portifolio portfolio = portifolioService.findById(arte.getId_portfolio());
    if (portfolio == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Portfolio nao encontrado.");
    }

    Optional<Artista> artista = artistaService.findByEmail(authentication.getName());
    if (artista.isEmpty() || !portfolio.getId_artista().equals(artista.get().getIdArtista())) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Voce so pode criar artes nos seus proprios portfolios.");
    }

    int result = artService.save(arte);
    return result > 0 ? ResponseEntity.status(HttpStatus.CREATED).body("Arte criada com sucesso")
                      : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao criar arte");
}
```

## 7. Upload de arquivos baseado apenas em Content-Type

Status: `[PENDENTE]`

### Metodo de invasao

Um artista autenticado pode enviar um arquivo cujo `Content-Type` declare ser imagem, mas cujo conteudo real nao seja uma imagem valida. O arquivo e salvo e fica publico em `/uploads/**`.

### Por que funciona

`ImageStorageService` decide a extensao por `file.getContentType()` e salva com `file.transferTo(target)`. O conteudo real nao e decodificado, reprocessado ou validado por magic bytes.

### Impacto

- Armazenamento publico de conteudo inesperado.
- Risco de XSS armazenado se algum proxy ou cliente interpretar conteudo incorretamente.
- Uso do servidor como hospedagem de arquivos abusivos.
- Consumo de disco por uploads acumulados.

### Como corrigir

Aplicar validacoes em camadas:

1. Manter limite de tamanho.
2. Validar assinatura binaria/magic bytes.
3. Decodificar a imagem no backend.
4. Reprocessar e salvar uma nova imagem gerada pelo servidor.
5. Servir uploads com headers seguros, incluindo `X-Content-Type-Options: nosniff`.
6. Considerar remover GIF se animacao nao for necessaria.

Exemplo conceitual para formatos suportados pelo `ImageIO`:

```java
BufferedImage image = ImageIO.read(file.getInputStream());
if (image == null) {
    throw new IllegalArgumentException("Arquivo nao e uma imagem valida.");
}
```

## 8. JWT em localStorage

Status: `[PENDENTE]`

### Metodo de invasao

Se houver XSS no frontend, um atacante consegue ler `localStorage.getItem("artisanvault_token")` e reutilizar o token enquanto ele estiver valido.

### Estado atual

O token ainda e salvo em `localStorage`:

```ts
localStorage.setItem('artisanvault_token', res.token)
```

O cliente Axios tambem ainda usa `withCredentials: true`, embora a autenticacao esteja baseada em Bearer token.

### Impacto

- Roubo de sessao.
- Acoes autenticadas como a vitima.
- Maior impacto se o token tiver longa duracao.

### Como corrigir

Opcoes recomendadas:

1. Migrar para cookie `HttpOnly`, `Secure`, `SameSite=Lax/Strict`.
2. Reduzir o tempo do access token.
3. Implementar refresh token com rotacao.
4. Criar mecanismo de revogacao em logout ou troca de senha.
5. Adicionar CSP forte no frontend.
6. Remover `withCredentials: true` se continuar usando apenas Bearer token.

## 9. Login sem rate limit

Status: `[PENDENTE]`

### Metodo de invasao

Um atacante pode automatizar tentativas contra `POST /api/login`, usando listas de e-mails e senhas vazadas.

### Por que funciona

Nao ha controle por IP, por e-mail, por quantidade de falhas ou por janela de tempo.

### Impacto

- Credential stuffing.
- Brute force contra senhas fracas.
- Aumento de carga no backend.

### Como corrigir

Adicionar rate limit e bloqueio temporario:

- Limite por IP.
- Limite por e-mail.
- Bloqueio temporario apos falhas consecutivas.
- Logs de seguranca para tentativas suspeitas.

Bibliotecas possiveis: Bucket4j, Redis rate limiter ou filtro customizado.

## 10. Dependencias vulneraveis

Status: `[PENDENTE]`

### Estado atual

O frontend ainda declara:

- `axios` com dependencia transitiva `form-data@4.0.5`, associada ao advisory `GHSA-hmw2-7cc7-3qxx`.
- `next@16.2.6`, que traz `postcss@8.4.31`, associado ao advisory `GHSA-qx2v-qp2m-jg93`.

O backend ainda usa:

- Spring Boot `3.3.2`.
- PostgreSQL JDBC `42.5.0`.

Em validacoes anteriores com OSV, essas versoes ainda apareciam com advisories em Spring/Tomcat/PostgreSQL/Jackson.

### Impacto

Dependendo da dependencia e do caminho exploravel, os riscos incluem DoS, XSS, bypass de autorizacao, falhas de parser e problemas no servidor web embutido.

### Como corrigir

Frontend:

```bash
npm audit fix
npm audit --omit=dev
```

Se nao resolver automaticamente, atualizar `axios`, `next` e lockfile manualmente para versoes corrigidas.

Backend:

1. Atualizar o parent do Spring Boot para uma versao corrigida recente.
2. Atualizar PostgreSQL JDBC.
3. Remover versoes fixadas que conflitem com o BOM do Spring Boot.
4. Rodar testes.

```bash
./mvnw test
```

## 11. Segredos e configuracao

Status: `[PARCIAL]`

### Resolvido

Os segredos foram movidos para `application-local.properties`, e o `application.properties` importa esse arquivo local:

```properties
spring.config.import=optional:application-local.properties
```

O arquivo local esta no `.gitignore`.

### Pendente

Ainda existem configuracoes inadequadas para producao:

```properties
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.datasource.username=postgres
```

### Como corrigir

1. Rotacionar qualquer segredo que ja tenha sido commitado ou compartilhado.
2. Usar variaveis de ambiente ou secret manager em producao.
3. Criar usuario de banco com privilegios minimos, em vez de usar `postgres`.
4. Trocar em producao:

```properties
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
```

5. Usar Flyway ou Liquibase para migracoes.

## Checklist atualizado

- [x] Remover `GET /**` publico.
- [x] Criar lista explicita de endpoints GET publicos.
- [x] Validar dono em `PUT /api/artistas/{id}`.
- [x] Validar dono em deletes de artista, cliente, pedido, portfolio, servico e arte.
- [x] Mover segredos para arquivo local ignorado pelo Git.
- [ ] Restringir `GET /api/cliente` e `GET /api/cliente/{id}`.
- [ ] Restringir `GET /api/pedido` e `GET /api/pedido/{id}`.
- [ ] Criar endpoints de pedidos filtrados no backend.
- [ ] Remover filtros de seguranca feitos apenas no frontend.
- [ ] Validar dono/papel em `POST /api/arte/post`.
- [ ] Validar upload por conteudo real, nao apenas por `Content-Type`.
- [ ] Reprocessar imagens antes de salvar.
- [ ] Adicionar headers seguros para `/uploads/**`.
- [ ] Trocar JWT em `localStorage` por cookie `HttpOnly` ou reduzir risco com CSP e tokens curtos.
- [ ] Remover `withCredentials: true` se o modelo continuar usando Bearer token.
- [ ] Adicionar rate limit no login.
- [ ] Atualizar dependencias do frontend.
- [ ] Atualizar dependencias do backend.
- [ ] Rotacionar segredos antigos que ja tenham sido expostos.
- [ ] Usar usuario de banco com privilegios minimos.
- [ ] Desativar `show-sql` e `ddl-auto=update` em producao.

## Ordem de implementacao sugerida

1. Criar endpoints autorizados para pedidos e trocar o frontend para usa-los.
2. Restringir endpoints de cliente a `/api/cliente/me` ou admin.
3. Corrigir `POST /api/arte/post` com validacao de dono.
4. Atualizar dependencias e rodar testes.
5. Fortalecer upload de imagens.
6. Melhorar armazenamento de sessao/token.
7. Adicionar rate limit no login.
8. Endurecer configuracao de producao.

