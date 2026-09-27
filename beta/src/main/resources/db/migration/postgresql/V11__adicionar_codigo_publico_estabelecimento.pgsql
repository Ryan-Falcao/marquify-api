ALTER TABLE estabelecimentos ADD COLUMN codigo_publico VARCHAR(36);
UPDATE estabelecimentos SET codigo_publico = md5(random()::text || clock_timestamp()::text || id::text) WHERE codigo_publico IS NULL;
ALTER TABLE estabelecimentos ALTER COLUMN codigo_publico SET NOT NULL;
ALTER TABLE estabelecimentos ADD CONSTRAINT uk_estabelecimentos_codigo_publico UNIQUE (codigo_publico);
