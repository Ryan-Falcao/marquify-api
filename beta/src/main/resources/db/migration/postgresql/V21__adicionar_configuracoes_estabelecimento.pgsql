ALTER TABLE estabelecimentos ADD COLUMN telefone VARCHAR(30);
ALTER TABLE estabelecimentos ADD COLUMN endereco VARCHAR(255);
ALTER TABLE estabelecimentos ADD COLUMN antecedencia_minima_minutos INTEGER NOT NULL DEFAULT 0;
ALTER TABLE estabelecimentos ADD COLUMN janela_maxima_agendamento_dias INTEGER NOT NULL DEFAULT 90;
ALTER TABLE estabelecimentos ADD COLUMN intervalo_entre_servicos_minutos INTEGER NOT NULL DEFAULT 0;
ALTER TABLE estabelecimentos ADD COLUMN antecedencia_cancelamento_minutos INTEGER NOT NULL DEFAULT 0;
