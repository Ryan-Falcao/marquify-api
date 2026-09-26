ALTER TABLE agendamentos ADD COLUMN estabelecimento_id BIGINT;
ALTER TABLE agendamentos ADD COLUMN profissional_id BIGINT;
UPDATE agendamentos a SET estabelecimento_id = v.estabelecimento_id FROM vendedor v WHERE v.id = a.vendedor_id;
UPDATE agendamentos a SET profissional_id = v.profissional_principal_id FROM vendedor v WHERE v.id = a.vendedor_id;
ALTER TABLE agendamentos ALTER COLUMN estabelecimento_id SET NOT NULL;
ALTER TABLE agendamentos ALTER COLUMN profissional_id SET NOT NULL;
ALTER TABLE agendamentos ADD CONSTRAINT fk_agendamentos_estabelecimento FOREIGN KEY (estabelecimento_id) REFERENCES estabelecimentos(id);
ALTER TABLE agendamentos ADD CONSTRAINT fk_agendamentos_profissional FOREIGN KEY (profissional_id) REFERENCES profissionais(id);
CREATE INDEX idx_agendamentos_profissional_data ON agendamentos (profissional_id, data);
