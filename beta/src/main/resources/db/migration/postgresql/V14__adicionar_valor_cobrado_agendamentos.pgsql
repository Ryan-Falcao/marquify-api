ALTER TABLE agendamentos ADD COLUMN valor_cobrado NUMERIC(12, 2);

UPDATE agendamentos AS agendamento
SET valor_cobrado = servico.preco
FROM servicos AS servico
WHERE servico.id = agendamento.servico_id
  AND agendamento.valor_cobrado IS NULL;
