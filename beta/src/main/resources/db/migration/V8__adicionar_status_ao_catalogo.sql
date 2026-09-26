ALTER TABLE servicos ADD COLUMN ativo BOOLEAN NOT NULL DEFAULT TRUE;

CREATE INDEX idx_servicos_estabelecimento_ativo ON servicos(estabelecimento_id, ativo);
