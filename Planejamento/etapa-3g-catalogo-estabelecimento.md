# Etapa 3g — Catálogo do estabelecimento

## Objetivo

Fazer do estabelecimento o proprietário canônico dos serviços, preparando o catálogo para ser compartilhado pelos profissionais da mesma unidade.

## Entrega

- A migration `V7__vincular_servicos_ao_estabelecimento.sql` adiciona `servicos.estabelecimento_id`, preenche o campo a partir do vendedor dos registros legados e torna o vínculo obrigatório.
- `Servicos` passou a referenciar `Estabelecimento` diretamente. A referência a `Vendedor` permanece temporariamente para compatibilidade das rotas atuais.
- Criação, exclusão e busca de serviço usam o estabelecimento do vendedor autenticado como escopo de autorização.
- O agendamento valida o serviço e o profissional no estabelecimento associado ao serviço.
- `ServicoResponse` expõe `estabelecimentoId`; `vendedorId` continua disponível durante a transição.

## Próximo recorte

Migrar as rotas de catálogo para um recurso aninhado em estabelecimento e incluir listagem, edição e inativação de serviços.
