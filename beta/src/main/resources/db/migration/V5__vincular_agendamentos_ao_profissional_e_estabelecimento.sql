-- Todo agendamento legado era atendido pelo vendedor associado a ele.
-- A migration preserva esse contexto antes da futura retirada das referências legadas.
ALTER TABLE agendamentos ADD COLUMN estabelecimento_id BIGINT NULL;
ALTER TABLE agendamentos ADD COLUMN profissional_id BIGINT NULL;

UPDATE agendamentos
SET estabelecimento_id = (
    SELECT v.estabelecimento_id
    FROM vendedor v
    WHERE v.id = agendamentos.vendedor_id
);

UPDATE agendamentos
SET profissional_id = (
    SELECT v.profissional_principal_id
    FROM vendedor v
    WHERE v.id = agendamentos.vendedor_id
);

ALTER TABLE agendamentos MODIFY COLUMN estabelecimento_id BIGINT NOT NULL;
ALTER TABLE agendamentos MODIFY COLUMN profissional_id BIGINT NOT NULL;
ALTER TABLE agendamentos ADD CONSTRAINT fk_agendamentos_estabelecimento
    FOREIGN KEY (estabelecimento_id) REFERENCES estabelecimentos(id);
ALTER TABLE agendamentos ADD CONSTRAINT fk_agendamentos_profissional
    FOREIGN KEY (profissional_id) REFERENCES profissionais(id);
CREATE INDEX idx_agendamentos_profissional_data ON agendamentos (profissional_id, data);
