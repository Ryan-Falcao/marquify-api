# Etapa 3C — Profissionais do estabelecimento

Implementada em 25/09/2026.

## Escopo

- Entidade `Profissional` vinculada obrigatoriamente a um estabelecimento.
- Vários profissionais podem pertencer ao mesmo estabelecimento.
- Profissional tem nome, estado ativo e timestamps; não possui credenciais de acesso.
- Repositório para listar somente os profissionais ativos de um estabelecimento.
- Migration V4 cria a tabela e um profissional principal para cada vendedor existente, preservando a semântica do modelo atual em que o vendedor é o recurso dos agendamentos.
- `Vendedor.profissionalPrincipal` registra esse vínculo legado e é opcional para suportar proprietários que não atendem clientes no futuro.

## Limites intencionais

Agendamentos e serviços ainda usam `Vendedor` no modelo atual. O próximo incremento adicionará a associação de serviço aos profissionais, depois migrará o agendamento para ocupar um profissional específico. As rotas permanecem inalteradas nesta etapa.

## Validação

- Build e suíte aprovados com 27 testes em H2.
- Build e suíte aprovados com os mesmos 27 testes em MySQL 8.4.6.
- A migration V4 foi aplicada sobre vendedores legados já vinculados aos seus estabelecimentos no MySQL de teste. A consulta posterior confirmou que todos receberam um profissional principal pertencente ao mesmo estabelecimento.
