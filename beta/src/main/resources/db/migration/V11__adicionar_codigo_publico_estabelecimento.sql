ALTER TABLE estabelecimentos ADD COLUMN codigo_publico VARCHAR(36) NULL;
UPDATE estabelecimentos SET codigo_publico = UUID() WHERE codigo_publico IS NULL;
ALTER TABLE estabelecimentos MODIFY COLUMN codigo_publico VARCHAR(36) NOT NULL;
ALTER TABLE estabelecimentos ADD CONSTRAINT uk_estabelecimentos_codigo_publico UNIQUE (codigo_publico);
