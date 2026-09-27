CREATE TABLE bloqueios_agenda (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    profissional_id BIGINT NOT NULL,
    tipo VARCHAR(16) NOT NULL,
    data_inicio DATE NOT NULL,
    data_fim DATE NOT NULL,
    hora_inicio TIME(6),
    hora_fim TIME(6),
    motivo VARCHAR(180),
    criado_em TIMESTAMP NOT NULL,
    CONSTRAINT fk_bloqueio_profissional FOREIGN KEY (profissional_id) REFERENCES profissionais(id)
);

CREATE INDEX idx_bloqueio_profissional_periodo
    ON bloqueios_agenda(profissional_id, data_inicio, data_fim);
