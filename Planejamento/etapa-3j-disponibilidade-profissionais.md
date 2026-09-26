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

## Próximo recorte

Bloqueios pontuais de agenda, como férias, almoço e indisponibilidades específicas por data.
