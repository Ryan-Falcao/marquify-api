-- Preserva os campos legados de vendedor nesta etapa. Para cada conta existente,
-- cria um estabelecimento e registra o vínculo um-para-um de propriedade.
ALTER TABLE estabelecimentos ADD COLUMN legado_vendedor_id BIGINT NULL;

INSERT INTO estabelecimentos (nome, fuso_horario, ativo, criado_em, atualizado_em, legado_vendedor_id)
SELECT nome_loja, 'America/Sao_Paulo', TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), id
FROM vendedor;

ALTER TABLE vendedor ADD COLUMN estabelecimento_id BIGINT NULL;

UPDATE vendedor
SET estabelecimento_id = (
    SELECT e.id
    FROM estabelecimentos e
    WHERE e.legado_vendedor_id = vendedor.id
);

ALTER TABLE vendedor MODIFY COLUMN estabelecimento_id BIGINT NOT NULL;
ALTER TABLE vendedor ADD CONSTRAINT uk_vendedor_estabelecimento UNIQUE (estabelecimento_id);
ALTER TABLE vendedor ADD CONSTRAINT fk_vendedor_estabelecimento
    FOREIGN KEY (estabelecimento_id) REFERENCES estabelecimentos(id);

ALTER TABLE estabelecimentos DROP COLUMN legado_vendedor_id;
