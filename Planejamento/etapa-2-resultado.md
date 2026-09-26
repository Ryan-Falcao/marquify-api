# Etapa 2 — Autorização e respostas seguras

Implementada em 25/09/2026, sobre o modelo atual. O escopo corresponde à segunda etapa da sequência acordada: isolamento dos registros e DTOs seguros. Não representa a conclusão de todos os itens da fase de identidade/onboarding do planejamento amplo.

## Mudanças

- `CurrentUser` resolve a identidade autenticada e exige tipo de conta, perfil e ID corretos para as operações do vendedor.
- Consulta e edição de perfil, listagem de agenda, criação e exclusão de serviços verificam o dono autenticado.
- Exclusão de serviço consulta conjuntamente ID do serviço e ID do vendedor autorizado; não basta enviar um vendedor válido junto de um serviço alheio.
- Criação de agendamento obtém o cliente pelo token e rejeita `clienteId` divergente. Consulta o serviço pelo ID e pelo vendedor, impedindo combinações incompatíveis.
- Cancelamento consulta o agendamento no escopo do cliente autenticado ou do vendedor ADMIN responsável pela agenda.
- Vendedor não pode criar reservas em nome de um cliente nesta etapa. Essa permissão será definida junto da carteira e do modelo de negócio.
- Controllers retornam DTOs. Agendamentos contêm resumos mínimos dos envolvidos e serviços não carregam a entidade do vendedor na resposta.
- Campos/getters de senha das entidades também foram excluídos da serialização como proteção adicional.
- JWT passou de e-mail para uma identidade tipada e estável (`cliente:<id>`/`vendedor:<id>`). Reutilizar um e-mail nas duas tabelas não transforma o token do cliente em token do vendedor.
- Login recusa identidades ambíguas e cadastro verifica ambas as tabelas antes de criar um cliente.
- Tokens inválidos, expirados, sem assinatura, antigos ou de conta removida são recusados pelo filtro com 401; falta de permissão retorna 403.
- Tratamento de erros de autenticação, autorização, campos básicos e conflitos com respostas JSON previsíveis.

## Compatibilidade

As rotas existentes foram mantidas, inclusive o GET de agenda com corpo, para limitar mudanças nesta etapa. IDs antes aceitos sem verificar propriedade agora podem retornar 403/404.

O frontend deve usar os campos dos DTOs documentados no README. Todos os usuários com tokens emitidos antes desta mudança precisam fazer login novamente. Nenhuma migration de banco foi necessária.

## Testes

A suíte tem 19 testes: os quatro da preparação do ambiente/JWT e 15 de integração de segurança com Spring Security, MockMvc e banco real da aplicação (H2 ou MySQL, conforme o perfil configurado).

Resultado: `mvnw -o -B verify` aprovado com Java 21 em H2 e em MySQL 8.4.6, com 19 testes, zero falhas, zero erros e nenhum teste ignorado em cada execução. O MySQL portátil foi utilizado apenas para o banco de teste e encerrado ao final. O build gerou o JAR atualizado.

Os cenários incluem operações legítimas, todas as alterações de perfil com vendedor alheio, consulta de agenda, criação/exclusão de serviço, cliente falsificado na reserva, vínculo serviço/vendedor incompatível, cancelamento cruzado, respostas sem credenciais, tokens inválidos, colisão de identidades por e-mail, cadastro duplicado e login correto/incorreto. As gravações dos testes de segurança são transacionais e revertidas.

## Próximas decisões

- Definir usuário, estabelecimento, profissionais e vínculo dos clientes antes de ampliar permissões ou criar onboarding do vendedor.
- Definir unicidade global no armazenamento: a consulta prévia no cadastro não substitui uma garantia transacional contra cadastros simultâneos. Duplicidade legada é recusada no login, mas não é corrigida automaticamente.
- Definir origens do frontend e a estratégia de sessão/renovação ao integrar a aplicação web.
- Implementar conflitos, expediente, bloqueios e concorrência da agenda na etapa correspondente.
