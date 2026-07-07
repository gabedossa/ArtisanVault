-- Normaliza e-mails existentes (trim + lowercase) e torna a unicidade de
-- e-mail case-insensitive em cada tabela. artista.email nunca teve nenhuma
-- constraint de unicidade (so cliente.email tinha UNIQUE, case-sensitive);
-- a checagem de colisao *entre* cliente e artista fica a cargo do backend
-- (ArtistaService/ClienteService), que consulta as duas tabelas antes de
-- criar/atualizar - o indice case-insensitive aqui cobre duplicatas dentro
-- da mesma tabela, que o backend sozinho nao evitaria sob concorrencia.

UPDATE cliente SET email = LOWER(TRIM(email)) WHERE email IS NOT NULL;
UPDATE artista SET email = LOWER(TRIM(email)) WHERE email IS NOT NULL;

ALTER TABLE cliente DROP CONSTRAINT cliente_email_key;
CREATE UNIQUE INDEX ux_cliente_email_lower ON cliente (LOWER(email));

ALTER TABLE artista ALTER COLUMN email SET NOT NULL;
CREATE UNIQUE INDEX ux_artista_email_lower ON artista (LOWER(email));
