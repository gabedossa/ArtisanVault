-- A verificacao de colisao de e-mail entre cliente e artista (V2) era feita na
-- aplicacao como "consulta as duas tabelas, depois insere" - sem nenhuma trava
-- em comum entre as duas tabelas, duas requisicoes concorrentes (uma criando
-- cliente, outra criando artista) podem passar pela checagem ao mesmo tempo e
-- inserir o mesmo e-mail nas duas tabelas (race condition confirmada manualmente
-- neste ambiente). Postgres nao suporta constraint UNIQUE entre tabelas
-- diferentes, entao a correcao e ter uma terceira tabela, com uma linha por
-- e-mail em uso (por qualquer um dos dois tipos de conta), com UNIQUE de
-- verdade - cliente/artista passam a reservar o e-mail nela dentro da mesma
-- transacao do INSERT/UPDATE/DELETE, o que fecha a janela de corrida.

CREATE TABLE email_registro (
    email character varying(255) PRIMARY KEY
);

INSERT INTO email_registro (email)
SELECT email FROM cliente WHERE email IS NOT NULL
UNION
SELECT email FROM artista WHERE email IS NOT NULL;
