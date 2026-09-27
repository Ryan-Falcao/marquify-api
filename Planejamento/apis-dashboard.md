# APIs de consulta da dashboard

## Endpoints

- `GET /vendedor/me/dashboard/agendamentos`: lista paginada.
- `GET /vendedor/me/dashboard/indicadores`: agregação de todos os registros filtrados, independente da página.
- `GET /vendedor/me/dashboard`: contrato anterior preservado, ainda usado pela tela atual.

JWT de proprietário obrigatório. O estabelecimento é determinado pelo usuário autenticado; nenhum parâmetro permite consultar outro estabelecimento. IDs de cliente/profissional sem reservas nesse estabelecimento retornam resultado vazio, sem revelar dados externos.

## Parâmetros compartilhados

| Campo | Regra |
| --- | --- |
| inicio, fim | Datas ISO `YYYY-MM-DD`, inclusivas. Informar ambas ou nenhuma. Sem período, consulta o dia atual no fuso do estabelecimento. Fim não pode anteceder início. |
| profissionalId | Opcional, inteiro positivo. |
| clienteId | Opcional, inteiro positivo. |
| status | Opcional: `AGENDADO`, `PREVISTO`, `EM_ATENDIMENTO`, `FINALIZADO`, `CANCELADO`. |
| pagina | Base zero; padrão 0. |
| tamanho | 1 a 100; padrão 20. |
| ordenarPor | `data` (padrão), `horaInicio`, `valorCobrado`, `cliente`, `profissional`, `id`. |
| direcao | `ASC` ou `DESC` (padrão). |

Filtros são combinados com AND. Indicadores aceitam os parâmetros de paginação/ordenação, mas não limitam o cálculo por eles. Parâmetros inválidos retornam 400. Ordenação tem desempate por horário e ID, para estabilidade entre páginas. Página além do último registro retorna lista vazia e mantém os totais.

Exemplo:

```http
GET /vendedor/me/dashboard/agendamentos?inicio=2027-01-01&fim=2027-01-31&profissionalId=2&status=PREVISTO&pagina=0&tamanho=20&ordenarPor=data&direcao=ASC
Authorization: Bearer <token>
```

## Lista

Retorna `periodo`, `itens`, `pagina`, `tamanho`, `totalItens`, `totalPaginas`, `ordenarPor`, `direcao`. Cada item contém ID, data, início/fim, status calculado, identificação/nome de cliente, profissional e serviço, preço contratado (`valorCobrado`) e duração contratada (`duracaoMinutos`). Não retorna senha, dados de autenticação ou a entidade completa.

`periodo` contém início, fim, fuso IANA e instante local de referência. A referência é capturada uma vez por chamada, compartilhada por filtros, cálculos e serialização. Datas dos agendamentos são datas locais do estabelecimento, não datas UTC.

## Indicadores e significado dos estados

- `totalAgendamentos`: quantidade total que corresponde aos filtros, incluindo cancelados quando não excluídos pelo filtro de status.
- `previstos`: reservas ativas cujo início está no futuro.
- `emAtendimento`: início atingido e fim ainda não atingido.
- `finalizados`: reservas não canceladas cujo fim já foi atingido. É conclusão inferida pelo horário; não comprova comparecimento nem pagamento.
- `cancelamentos`: reservas canceladas cuja **data de atendimento** pertence ao período; não é uma contagem pela data da ação de cancelar.
- `faturamentoRealizado`: soma dos preços contratados dos finalizados, conforme a regra existente do produto. Não representa conciliação financeira.
- `faturamentoPrevisto`: soma dos preços contratados dos previstos e em atendimento. Exclui cancelados e finalizados.
- `reservasSemPreco`: registros filtrados com preço nulo; esses valores não são inventados nem incluídos nas somas.

`AGENDADO` corresponde ao estado persistido e inclui previstos, em atendimento e finalizados. Os demais estados são calculados sem alterar os registros. Início exatamente igual à referência significa em atendimento; fim igual à referência significa finalizado. Somatórios vazios retornam zero.

## Implementação e validação

Paginação e agregação são executadas no banco. Campos de ordenação têm lista permitida. As consultas sempre incluem estabelecimento e período. V19 adiciona índices de estabelecimento/período e estabelecimento/cliente/período; já existe índice de profissional/data.

Testes de integração cobrem autenticação, perfil de cliente bloqueado, filtros combinados, IDs externos, paginação, contratos de preço, totais independentes da página, resultado vazio, parâmetros inválidos e virada do dia com relógio fixo. Execução local usa H2; migração PostgreSQL incluída.

Reinicie a API para aplicar V19. Tipos e funções de consulta TypeScript estão disponíveis no frontend; este recorte entrega as APIs sem substituir o layout ou os indicadores da tela anterior.
