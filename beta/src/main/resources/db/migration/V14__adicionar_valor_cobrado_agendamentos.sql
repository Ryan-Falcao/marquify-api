ALTER TABLE agendamentos ADD COLUMN valor_cobrado DECIMAL(12, 2);

UPDATE agendamentos
SET valor_cobrado = (SELECT preco FROM servicos WHERE servicos.id = agendamentos.servico_id)
WHERE valor_cobrado IS NULL AND servico_id IS NOT NULL;
