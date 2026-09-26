-- Esquema equivalente às entidades atuais. Não altera regras de negócio.
-- Ordinais existentes: UserRole ADMIN=0/USER=1; Status AGENDADO=0/CANCELADO=1.
CREATE TABLE clientes (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255),
    email VARCHAR(255),
    numero VARCHAR(255),
    senha VARCHAR(255) NOT NULL,
    role TINYINT NOT NULL
);

CREATE TABLE vendedor (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    nome_loja VARCHAR(255) NOT NULL,
    hora_abertura TIME(6) NOT NULL,
    hora_fechamento TIME(6) NOT NULL,
    senha VARCHAR(255) NOT NULL,
    role TINYINT NOT NULL
);

CREATE TABLE vendedor_dias_abertos (
    vendedor_id BIGINT NOT NULL,
    dia ENUM('Domingo', 'Segunda', 'Terca', 'Quarta', 'Quinta', 'Sexta', 'Sabado'),
    CONSTRAINT fk_dias_vendedor FOREIGN KEY (vendedor_id) REFERENCES vendedor(id)
);

CREATE TABLE servicos (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255),
    descricao VARCHAR(255),
    preco DOUBLE,
    tempo TIME(6),
    vendedor_id BIGINT,
    CONSTRAINT fk_servicos_vendedor FOREIGN KEY (vendedor_id) REFERENCES vendedor(id)
);

CREATE TABLE agendamentos (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    data DATE,
    hora_inicio TIME(6),
    hora_fim TIME(6),
    status TINYINT,
    cliente_id BIGINT,
    vendedor_id BIGINT,
    servico_id BIGINT,
    CONSTRAINT fk_agendamentos_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id),
    CONSTRAINT fk_agendamentos_vendedor FOREIGN KEY (vendedor_id) REFERENCES vendedor(id),
    CONSTRAINT fk_agendamentos_servico FOREIGN KEY (servico_id) REFERENCES servicos(id)
);
