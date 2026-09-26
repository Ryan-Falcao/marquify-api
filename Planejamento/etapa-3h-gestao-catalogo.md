# Etapa 3h — Gestão completa do catálogo

## Entrega

- As rotas de catálogo foram migradas para `/estabelecimentos/{estabelecimentoId}/servicos`.
- O proprietário pode listar, criar, editar, ativar e desativar serviços do próprio estabelecimento.
- A migration `V8__adicionar_status_ao_catalogo.sql` adiciona o status `ativo` aos serviços existentes, iniciado como verdadeiro.
- Serviços desativados permanecem disponíveis para consulta de histórico, mas são excluídos da criação de novos agendamentos.
- A rota legada `DELETE /vendedor/deletarServico` passou a desativar o item para evitar a perda de dados históricos.

## Validação

- `./mvnw.cmd -o -B verify`: 33 testes aprovados, incluindo autorização entre estabelecimentos, edição e alteração de status do catálogo.

## Próxima etapa

Modelar disponibilidade dos profissionais: jornada semanal, pausas/bloqueios e prevenção de sobreposição de agendamentos.
