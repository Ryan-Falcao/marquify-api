# Etapa 3D — Agendamento por profissional e estabelecimento

Implementada em 25/09/2026.

## Escopo

- Cada `Agendamento` agora referencia obrigatoriamente um `Estabelecimento` e um `Profissional`.
- A migration V5 preenche esses vínculos em agendamentos legados pelo estabelecimento e profissional principal do vendedor que já constava na reserva.
- A criação de agendamento exige `profissionalId` e só aceita profissional ativo que pertença ao mesmo estabelecimento do vendedor responsável pelo serviço.
- A resposta de agendamento inclui resumos seguros de estabelecimento e profissional.
- Índice por profissional e data preparado para as consultas e validações da agenda.

## Transição

`vendedor_id` permanece no agendamento para compatibilidade enquanto serviços e rotas ainda usam o modelo anterior. A próxima etapa associa serviços a profissionais; então a criação de agendamento poderá verificar se o profissional escolhido executa o serviço selecionado.

## Validação

- Build e suíte aprovados com 28 testes em H2 e MySQL 8.4.6.
- Antes da V5, foi inserido um agendamento legado no MySQL de teste. A migration preencheu `estabelecimento_id` e `profissional_id`; a verificação posterior confirmou ambos contra o vendedor original.
