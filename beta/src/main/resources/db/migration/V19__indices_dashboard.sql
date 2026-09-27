CREATE INDEX idx_agendamentos_estabelecimento_periodo ON agendamentos (estabelecimento_id, data, hora_inicio, id);
CREATE INDEX idx_agendamentos_estabelecimento_cliente_periodo ON agendamentos (estabelecimento_id, cliente_id, data);
