# Contrato inicial para o frontend

## Descoberta pública

Sem token, a tela do cliente pode carregar:

| Método | Rota | Uso |
| --- | --- | --- |
| GET | `/publico/estabelecimentos/{estabelecimentoId}` | Cabeçalho do estabelecimento (`id`, `nome`, `fusoHorario`) |
| GET | `/publico/estabelecimentos/{estabelecimentoId}/servicos` | Serviços ativos, preço, duração e profissionais habilitados |
| GET | `/publico/estabelecimentos/{estabelecimentoId}/profissionais` | Profissionais ativos (`id`, `nome`) |
| GET | `/publico/estabelecimentos/{estabelecimentoId}/profissionais/{profissionalId}/horarios?servicoId={id}&data=YYYY-MM-DD` | Inícios de horários livres para o serviço e dia escolhidos |

## Fluxo autenticado do cliente

1. Autenticar em `/auth/login` e guardar o token retornado.
2. Enviar `Authorization: Bearer {token}` ao criar ou cancelar agendamentos.
3. Para criar, enviar `clienteId` opcional, `vendedorId`, `servicoId`, `profissionalId`, `data` e `horaInicio` para `POST /agendamento`.

O catálogo informa `vendedorId` durante a transição de modelo; o frontend pode usá-lo no agendamento atual. A rota de disponibilidade alimenta a escolha de data e hora com horários livres.

O retorno de horários contém `horarios`, uma lista de valores `HH:mm:ss`. Exiba apenas essa lista depois que a pessoa selecionar serviço, profissional e data; ao confirmar, envie o horário selecionado como `horaInicio` em `POST /agendamento`.

## CORS

Durante desenvolvimento, `http://localhost:3000` e `http://localhost:5173` são aceitos. Defina `CORS_ALLOWED_ORIGINS` para incluir a URL publicada do frontend antes do deploy.
