# Etapa 3E — Serviços oferecidos por profissionais

Implementada em 25/09/2026.

## Escopo

- Um serviço pode ser executado por vários profissionais, e um profissional pode oferecer vários serviços.
- A migration V6 cria a tabela de associação `servico_profissionais` e vincula todo serviço legado ao profissional principal do vendedor responsável.
- A criação de serviço exige `profissionaisIds`, com pelo menos um profissional ativo do estabelecimento do proprietário.
- A resposta de serviço agora informa os profissionais associados, com `id` e `nome`.
- A criação de agendamento rejeita o profissional que não oferece o serviço escolhido.

## Transição

Os serviços continuam referenciando `Vendedor` durante a transição de modelo. A associação com profissionais é a fonte de verdade para decidir se uma reserva é possível. A próxima etapa poderá mover a propriedade do catálogo diretamente para `Estabelecimento` e criar APIs de gestão dos profissionais.

## Validação

- Build e suíte aprovados com 31 testes em H2 e MySQL 8.4.6.
- Um serviço legado foi inserido antes da V6 no MySQL de teste. A migration associou-o ao profissional principal do vendedor e a verificação posterior confirmou o vínculo.
