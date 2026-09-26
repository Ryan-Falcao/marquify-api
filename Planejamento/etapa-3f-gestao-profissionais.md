# Etapa 3F — Gestão de profissionais

Implementada em 25/09/2026.

## Rotas

Todas exigem um token de proprietário (`ADMIN`) e usam o vendedor autenticado para definir o estabelecimento:

| Método | Rota | Ação |
|---|---|---|
| GET | `/vendedor/{vendedorId}/profissionais` | Lista profissionais, ativos e inativos |
| POST | `/vendedor/{vendedorId}/profissionais` | Cria profissional com `{ "nome": "..." }` |
| PUT | `/vendedor/{vendedorId}/profissionais/{profissionalId}` | Altera nome |
| PATCH | `/vendedor/{vendedorId}/profissionais/{profissionalId}/ativar` | Ativa |
| PATCH | `/vendedor/{vendedorId}/profissionais/{profissionalId}/desativar` | Desativa |

## Regras

- O proprietário só administra profissionais do próprio estabelecimento.
- A URL de outro vendedor retorna 403; um profissional de outro estabelecimento dentro da URL correta retorna 404.
- Desativação preserva o profissional e seu histórico. Profissionais inativos não podem ser escolhidos para novos agendamentos ou novos serviços.
- A resposta contém `id`, `nome`, `ativo`, `criadoEm` e `atualizadoEm`, sem carregar a entidade de estabelecimento.

Não houve migration nesta etapa: as tabelas e relações necessárias foram criadas nas etapas anteriores.

## Validação

Build e suíte aprovados com 32 testes em H2 e MySQL 8.4.6. A suíte cobre listagem, criação, edição, ativação, desativação, acesso por outro proprietário, acesso a profissional de outro estabelecimento e tentativa de acesso por cliente.
