ALTER TABLE estabelecimentos ADD COLUMN legado_vendedor_id BIGINT;
INSERT INTO estabelecimentos (nome, fuso_horario, ativo, criado_em, atualizado_em, legado_vendedor_id)
SELECT nome_loja, 'America/Sao_Paulo', TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), id FROM vendedor;
ALTER TABLE vendedor ADD COLUMN estabelecimento_id BIGINT;
UPDATE vendedor v SET estabelecimento_id = e.id FROM estabelecimentos e WHERE e.legado_vendedor_id = v.id;
ALTER TABLE vendedor ALTER COLUMN estabelecimento_id SET NOT NULL;
ALTER TABLE vendedor ADD CONSTRAINT uk_vendedor_estabelecimento UNIQUE (estabelecimento_id);
ALTER TABLE vendedor ADD CONSTRAINT fk_vendedor_estabelecimento FOREIGN KEY (estabelecimento_id) REFERENCES estabelecimentos(id);
ALTER TABLE estabelecimentos DROP COLUMN legado_vendedor_id;
