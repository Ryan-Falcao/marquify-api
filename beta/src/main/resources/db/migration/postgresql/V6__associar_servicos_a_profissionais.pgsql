CREATE TABLE servico_profissionais (
    servico_id BIGINT NOT NULL REFERENCES servicos(id), profissional_id BIGINT NOT NULL REFERENCES profissionais(id),
    PRIMARY KEY (servico_id, profissional_id)
);
INSERT INTO servico_profissionais (servico_id, profissional_id)
SELECT s.id, v.profissional_principal_id FROM servicos s JOIN vendedor v ON v.id = s.vendedor_id;
