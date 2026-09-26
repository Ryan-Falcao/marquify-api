CREATE TABLE servico_profissionais (
    servico_id BIGINT NOT NULL,
    profissional_id BIGINT NOT NULL,
    PRIMARY KEY (servico_id, profissional_id),
    CONSTRAINT fk_servico_profissionais_servico
        FOREIGN KEY (servico_id) REFERENCES servicos(id),
    CONSTRAINT fk_servico_profissionais_profissional
        FOREIGN KEY (profissional_id) REFERENCES profissionais(id)
);

-- Serviços legados eram executados pelo vendedor responsável por eles.
INSERT INTO servico_profissionais (servico_id, profissional_id)
SELECT s.id, v.profissional_principal_id
FROM servicos s
INNER JOIN vendedor v ON v.id = s.vendedor_id;
