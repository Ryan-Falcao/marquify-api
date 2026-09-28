ALTER TABLE servicos ADD COLUMN estabelecimento_id BIGINT NULL;

UPDATE servicos
SET estabelecimento_id = (
    SELECT vendedor.estabelecimento_id
    FROM vendedor
    WHERE vendedor.id = servicos.vendedor_id
);

ALTER TABLE servicos ALTER COLUMN estabelecimento_id SET NOT NULL;

ALTER TABLE servicos
    ADD CONSTRAINT fk_servicos_estabelecimento
    FOREIGN KEY (estabelecimento_id) REFERENCES estabelecimentos(id);

CREATE INDEX idx_servicos_estabelecimento ON servicos(estabelecimento_id);
