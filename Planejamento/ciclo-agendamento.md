# Ciclo do agendamento

## Regras implementadas

- Cliente altera apenas sua própria reserva; proprietário apenas reservas vinculadas à sua conta. IDs de terceiros retornam 404.
- Cancelamento e remarcação são permitidos somente antes do início no fuso do estabelecimento. Não há antecedência mínima adicional neste recorte.
- Cancelamento exige confirmação na interface e libera o intervalo; repetir o cancelamento é idempotente. Reserva cancelada não pode ser remarcada.
- Remarcação altera somente data e início. Mantém cliente, estabelecimento, profissional, serviço, preço e duração contratados.
- A nova data respeita jornada, pausas, ausências, férias, reservas existentes e horário atual. A própria reserva é excluída da verificação de sobreposição.
- Em conflito, a API retorna 409 e mantém a reserva original. Intervalos adjacentes são permitidos; sobreposição parcial também é impedida. Atendimentos atravessando meia-noite não são suportados.

## Histórico do contrato

`valorCobrado` é o preço copiado na criação da reserva. `duracaoMinutos` é calculada a partir do início/fim registrados, nunca da duração atual do catálogo. Remarcações preservam ambos. O objeto `servico` continua representando o catálogo atual; consumidores devem usar os campos do agendamento para mostrar o contrato. Reservas legadas sem valor são apresentadas como preço não registrado, sem inventar um valor histórico.

Este recorte conserva preço/duração por reserva; não cria um extrato de todas as edições do catálogo ou de todas as datas anteriores de remarcação.

## API

- `PUT /agendamento/{id}/remarcar`: `{ "data": "2027-01-04", "horaInicio": "14:00" }`.
- `GET /agendamento/{id}/horarios-remarcacao?data=2027-01-04`: horários livres considerando a duração contratada.
- `PUT /agendamento/cancelar`: `{ "agendamentoId": 123 }`.

Todas exigem JWT e validação de propriedade. A interface de cliente em ambas as rotas de meus agendamentos oferece remarcação, cancelamento com confirmação e os valores contratados.

## Concorrência e validação

Criação e remarcação usam bloqueio pessimista na linha do profissional até o commit, antes de consultar ocupação e salvar. Alterações da mesma reserva também bloqueiam sua linha e recarregam seu estado. O banco serializa as operações, inclusive entre instâncias da API. Escritas SQL externas que contornem esses serviços não estão cobertas por esse protocolo.

Testes de integração usam transações independentes e threads concorrentes no H2, cobrindo sobreposição entre reservas, disputa entre remarcações, propriedade, conflitos, cancelamento idempotente, preservação do contrato após edição de catálogo e horários passados. Ainda é recomendada validação de carga no PostgreSQL antes da produção.

Não há migração nova: preço e início/fim já eram persistidos. Reiniciar a API é necessário para carregar os novos endpoints e regras.
