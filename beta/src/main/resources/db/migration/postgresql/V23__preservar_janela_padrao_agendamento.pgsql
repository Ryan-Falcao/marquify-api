-- Antes da configuração existir, não havia limite de janela. Mantemos um ano como padrão seguro.
UPDATE estabelecimentos
SET janela_maxima_agendamento_dias = 365
WHERE janela_maxima_agendamento_dias = 90;

ALTER TABLE estabelecimentos
ALTER COLUMN janela_maxima_agendamento_dias SET DEFAULT 365;
