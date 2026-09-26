-- Profissionais são recursos de agenda do estabelecimento e podem existir sem conta de acesso.
CREATE TABLE profissionais (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    estabelecimento_id BIGINT NOT NULL,
    nome VARCHAR(160) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em DATETIME(6) NOT NULL,
    atualizado_em DATETIME(6) NOT NULL,
    legado_vendedor_id BIGINT NULL,
    CONSTRAINT fk_profissional_estabelecimento
        FOREIGN KEY (estabelecimento_id) REFERENCES estabelecimentos(id)
);

-- O vendedor atual era também o recurso usado pelos agendamentos legados.
INSERT INTO profissionais (estabelecimento_id, nome, ativo, criado_em, atualizado_em, legado_vendedor_id)
SELECT estabelecimento_id, nome, TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), id
FROM vendedor;

ALTER TABLE vendedor ADD COLUMN profissional_principal_id BIGINT NULL;

UPDATE vendedor
SET profissional_principal_id = (
    SELECT p.id
    FROM profissionais p
    WHERE p.legado_vendedor_id = vendedor.id
);

ALTER TABLE vendedor ADD CONSTRAINT uk_vendedor_profissional_principal UNIQUE (profissional_principal_id);
ALTER TABLE vendedor ADD CONSTRAINT fk_vendedor_profissional_principal
    FOREIGN KEY (profissional_principal_id) REFERENCES profissionais(id);

ALTER TABLE profissionais DROP COLUMN legado_vendedor_id;
CREATE INDEX idx_profissionais_estabelecimento_ativo ON profissionais (estabelecimento_id, ativo);
