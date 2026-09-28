CREATE EXTENSION IF NOT EXISTS pgcrypto;

ALTER TABLE estabelecimentos ADD COLUMN codigo_publico VARCHAR(36) NULL;
UPDATE estabelecimentos SET codigo_publico = gen_random_uuid()::TEXT WHERE codigo_publico IS NULL;
ALTER TABLE estabelecimentos ALTER COLUMN codigo_publico SET NOT NULL;
ALTER TABLE estabelecimentos ADD CONSTRAINT uk_estabelecimentos_codigo_publico UNIQUE (codigo_publico);
