-- Hibernate grava @Enumerated(EnumType.STRING) como VARCHAR. Converte os dados
-- existentes do enum legado sem alterar nenhuma migration já aplicada.
ALTER TABLE vendedor_dias_abertos
    ALTER COLUMN dia TYPE VARCHAR(20)
    USING dia::text;

-- O tipo legado só é removido se não houver nenhuma dependência além das
-- dependências internas do próprio PostgreSQL (por exemplo, o tipo de array).
DO $$
DECLARE
    tipo_dia_oid OID := to_regtype('public.vendedor_dia_aberto');
BEGIN
    IF tipo_dia_oid IS NOT NULL
       AND NOT EXISTS (
           SELECT 1
           FROM pg_catalog.pg_depend dependencia
           WHERE dependencia.refclassid = 'pg_type'::regclass
             AND dependencia.refobjid = tipo_dia_oid
             AND dependencia.deptype NOT IN ('i', 'a')
       )
    THEN
        DROP TYPE public.vendedor_dia_aberto;
    END IF;
END;
$$;
