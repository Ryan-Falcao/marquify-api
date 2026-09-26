ALTER TABLE servicos ADD COLUMN estabelecimento_id BIGINT;
UPDATE servicos s SET estabelecimento_id = v.estabelecimento_id FROM vendedor v WHERE v.id = s.vendedor_id;
ALTER TABLE servicos ALTER COLUMN estabelecimento_id SET NOT NULL;
ALTER TABLE servicos ADD CONSTRAINT fk_servicos_estabelecimento FOREIGN KEY (estabelecimento_id) REFERENCES estabelecimentos(id);
CREATE INDEX idx_servicos_estabelecimento ON servicos(estabelecimento_id);
