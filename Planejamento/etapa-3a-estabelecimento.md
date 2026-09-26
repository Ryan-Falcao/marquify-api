# Etapa 3A — Entidade Estabelecimento

Implementada em 25/09/2026 como primeiro incremento do novo modelo de negócio.

## Escopo

- Entidade `Estabelecimento` com identificador, nome, fuso horário, estado ativo e timestamps de criação/atualização.
- Fuso validado como identificador IANA, por exemplo `America/Sao_Paulo`.
- Nome normalizado e validado entre 1 e 160 caracteres.
- Ativação e desativação explícitas para preservar o histórico do negócio.
- Repositório Spring Data para persistência.
- Migration Flyway V2 para criação da tabela `estabelecimentos`.
- Testes de persistência, valores iniciais, validações e auditoria.

## Limites intencionais

Esta etapa não liga o estabelecimento ao `Vendedor`, aos serviços ou aos agendamentos. Também não cria endpoints. Esses vínculos serão adicionados progressivamente para que a migração dos dados atuais seja explícita e verificável.

O próximo incremento recomendado é modelar o vínculo de propriedade entre a conta do vendedor e o estabelecimento, migrando `nomeLoja` e as configurações do negócio sem remover os campos antigos antes da transição.
