# Etapa 3B — Vínculo entre proprietário e estabelecimento

Implementada em 25/09/2026.

## Escopo

- `Vendedor` passa a apontar obrigatoriamente para um `Estabelecimento`.
- A relação é um-para-um: uma conta atual de vendedor representa o proprietário de um estabelecimento.
- A migration V3 cria um estabelecimento para cada vendedor já existente e registra a chave estrangeira `vendedor.estabelecimento_id`.
- Os campos atuais `nomeLoja`, horários e dias abertos continuam intactos, para uma migração gradual e verificável das configurações do negócio.
- O identificador de negócio legado é armazenado apenas durante a migration, removido antes de sua conclusão e nunca exposto pela aplicação.
- Testes criam e persistem a relação proprietário/estabelecimento.

## Limites intencionais

O relacionamento não representa profissionais ou funcionários. A próxima entidade será `Profissional`, vinculada ao estabelecimento, com agenda própria. Isso permite vários profissionais no mesmo negócio sem transformar a conta do proprietário em recurso de agenda.

Ainda não há endpoints de criação ou gestão de estabelecimentos. As rotas atuais continuam usando `Vendedor` durante esta transição; elas serão adaptadas após a criação do modelo de profissionais e da migração dos serviços/agendamentos.

## Validação

- Build e suíte aprovados com 23 testes em H2.
- Build e suíte aprovados com os mesmos 23 testes em MySQL 8.4.6.
- Antes da execução da migration V3 em MySQL, foi inserido um vendedor no esquema V2. A migration criou seu estabelecimento a partir de `nome_loja`, atribuiu `vendedor.estabelecimento_id` e a verificação posterior confirmou o vínculo.
