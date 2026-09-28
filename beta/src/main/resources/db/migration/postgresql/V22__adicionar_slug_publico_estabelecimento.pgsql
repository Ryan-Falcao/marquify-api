ALTER TABLE estabelecimentos ADD COLUMN slug_publico VARCHAR(60);

UPDATE estabelecimentos
SET slug_publico = LEFT(TRIM(BOTH '-' FROM REGEXP_REPLACE(LOWER(nome), '[^a-z0-9]+', '-', 'g')), 40) || '-' || id
WHERE slug_publico IS NULL;

UPDATE estabelecimentos SET slug_publico = 'estabelecimento-' || id WHERE slug_publico IS NULL OR slug_publico = '';
ALTER TABLE estabelecimentos ALTER COLUMN slug_publico SET NOT NULL;
ALTER TABLE estabelecimentos ADD CONSTRAINT uk_estabelecimentos_slug_publico UNIQUE (slug_publico);
