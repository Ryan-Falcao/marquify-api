# Etapa 3j — Disponibilidade de profissionais

## Entrega

- A migration `V9__criar_disponibilidades_profissionais.sql` cria a jornada semanal de cada profissional.
- Jornadas dos proprietários legados foram preenchidas com os dias e horários de funcionamento já cadastrados.
- A gestão pode consultar e substituir a jornada semanal por profissional.
- A rota pública de horários livres recebe estabelecimento, profissional, serviço e data; ela retorna inícios em intervalos de 15 minutos.
- O cálculo respeita a duração do serviço e descarta horários que se sobrepõem a agendamentos confirmados.
- A criação do agendamento repete a validação para impedir que dois clientes reservem o mesmo período.

## Contrato para a tela de agendamento

`GET /publico/estabelecimentos/{estabelecimentoId}/profissionais/{profissionalId}/horarios?servicoId={servicoId}&data=2027-01-04`

O retorno inclui `horarios`, com itens como `08:00:00`, `08:15:00` e `08:30:00`. O frontend escolhe um item e o envia em `horaInicio` ao criar o agendamento.

## Validação

- `./mvnw.cmd -o -B verify`: 35 testes aprovados.

## Recorte concluído — bloqueios de agenda

- Férias e ausências podem bloquear um dia ou intervalo completo de datas.
- Almoço e pausas podem ser recorrentes em todos os dias de trabalho, com data final opcional.
- Pausas pontuais bloqueiam um intervalo de horas somente na data escolhida.
- A gestão lista, cria e remove bloqueios por profissional usando rotas derivadas do JWT.
- O cálculo público mantém os horários da jornada visíveis, diferenciando reservas de bloqueios administrativos.
- A criação do agendamento repete a validação e rejeita qualquer intervalo que sobreponha um bloqueio.
