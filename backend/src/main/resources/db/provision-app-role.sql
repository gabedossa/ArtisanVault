-- Provisiona um usuario Postgres com privilegio minimo para a aplicacao
-- rodar em runtime (so DML nas tabelas que ela realmente usa). Rode este
-- script uma vez, como superusuario, depois que o Flyway ja tiver criado o
-- schema (V1__baseline.sql ou versoes seguintes).
--
-- As migracoes do Flyway continuam rodando com um usuario separado (com
-- privilegio de DDL, ex.: o superusuario) -- veja spring.flyway.user/password
-- em application.properties / application-local.properties.example.
--
-- Troque a senha abaixo antes de rodar em qualquer ambiente real.

CREATE ROLE artisanvault_app WITH LOGIN PASSWORD 'troque-esta-senha' NOSUPERUSER NOCREATEDB NOCREATEROLE NOREPLICATION;

GRANT CONNECT ON DATABASE postgres TO artisanvault_app;
GRANT USAGE ON SCHEMA public TO artisanvault_app;

GRANT SELECT, INSERT, UPDATE, DELETE ON
  public.artista, public.cliente, public.servico,
  public.portfolio, public.pedido, public.arte, public.email_registro
TO artisanvault_app;

GRANT USAGE, SELECT ON
  public.artista_id_artista_seq, public.cliente_id_cliente_seq,
  public.servico_id_servico_seq, public.portfolio_id_portfolio_seq,
  public.pedido_id_pedido_seq, public.arte_id_arte_seq
TO artisanvault_app;
