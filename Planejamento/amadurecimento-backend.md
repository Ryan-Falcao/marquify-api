# Planejamento de amadurecimento do backend — Marquify

Data da análise: 25/09/2026.

## 1. Objetivo e contexto

O Marquify é uma aplicação web de agendamento para barbearias, clínicas e outros negócios que atendem com horário marcado. O vendedor representa o profissional ou estabelecimento e terá uma dashboard para acompanhar agendamentos, clientes e seu catálogo de produtos/serviços.

Neste planejamento, “produto” significa serviço agendável, com preço e duração. Estoque, venda de mercadorias, prontuário clínico e pagamentos não fazem parte do escopo inicial confirmado.

O objetivo é transformar a base atual em um backend confiável para um primeiro lançamento: com isolamento entre estabelecimentos, agenda consistente, contratos adequados ao frontend e operação verificável.

Este documento resulta da leitura dos controllers, services, entidades, repositories, DTOs, segurança, configuração Maven, teste existente e README. A aplicação e os testes não foram executados; problemas de execução e compatibilidade de dependências ainda precisam ser verificados. Nenhuma funcionalidade foi implementada nesta etapa.

## 2. Diagnóstico do estado atual

### Base existente

- Projeto Maven na pasta `beta`, configurado para Java 21 e Spring Boot 4.1.0.
- Spring MVC, Spring Data JPA, MySQL, Spring Security, JWT e Lombok.
- Camadas de controllers, services, repositories, entidades e objetos de requisição/resposta.
- Entidades `Cliente`, `Vendedor`, `Servicos` e `Agendamento`.
- Cadastro de cliente, login de cliente/vendedor, BCrypt e JWT com validade fixa de duas horas.
- Proteção das rotas por perfis `ADMIN` e `USER`.
- Criação e cancelamento de agendamentos; consulta de agendamentos do vendedor.
- Edição de dados e funcionamento do vendedor; criação e exclusão de serviços.

### Lacunas observadas

| Área | Evidência no código | Consequência | Prioridade |
|---|---|---|---|
| Isolamento dos dados | Services recebem IDs do corpo ou da URL sem compará-los ao usuário autenticado | Um usuário com o perfil exigido pode operar sobre registros de outra pessoa ou estabelecimento | P0 |
| Saída de dados | Controllers devolvem entidades; `Cliente` e `Vendedor` expõem getters de senha e `getPassword()` | Risco de serialização de hashes e de dados além do necessário | P0 |
| Integridade da agenda | `agendar` calcula o fim e salva sem verificar sobreposição, funcionamento ou vínculo serviço/vendedor | Reservas conflitantes e combinações inválidas | P0 |
| Identidade | Login procura primeiro vendedor e depois cliente; cadastro verifica duplicidade apenas entre clientes | Possibilidade de ambiguidade quando o mesmo e-mail existe nas duas tabelas | P0 |
| Configuração | Não foi encontrada pasta de recursos com configuração da aplicação | Execução local depende de configuração ainda não documentada corretamente | P0 |
| Cadastro do negócio | Não há endpoint para cadastrar vendedor/estabelecimento | Jornada de entrada do vendedor incompleta | P1 |
| Dashboard | Apenas uma lista de agendamentos por vendedor, sem paginação ou filtros | Não atende à navegação diária da agenda nem aos indicadores | P1 |
| Clientes | Há entidade e autenticação, mas não há gestão da carteira do estabelecimento | Dashboard sem consulta de clientes e histórico | P1 |
| Catálogo | Criação/exclusão de serviço, sem listagem, edição ou inativação | Gestão incompleta e exclusão problemática para registros referenciados | P1 |
| Validação e erros | Requests sem restrições de campos; uso de `RuntimeException` genérica | Respostas pouco previsíveis para o frontend | P1 |
| Modelagem | Preço em `Double`, duração em `LocalTime`, status/roles sem mapeamento explícito de enum como texto | Imprecisão monetária, semântica inadequada de duração e persistência sensível à ordem dos enums | P1 |
| Testes | Somente `contextLoads` | Regras de negócio e permissões sem cobertura específica | P1 |
| Documentação | README diverge em versão Java, rotas e propriedades JWT | Integração e execução propensas a erro | P1 |

P0: fundação necessária antes de disponibilizar acesso real. P1: necessário para o MVP utilizável. P2: evolução após estabilização. Prioridades indicam ordem e risco, não uma estimativa de prazo.

## 3. Decisões de produto a fechar

As decisões abaixo devem ser registradas antes das fases que dependem delas. As propostas são pontos de partida, não requisitos já confirmados.

| Decisão | Proposta inicial | Impacto |
|---|---|---|
| Estabelecimento e profissional | Separar conceitualmente negócio, usuário e recurso que recebe reservas; começar com um profissional/recurso por negócio se isso atender ao piloto | Define isolamento e unidade de conflito da agenda |
| Quantidade de negócios por vendedor | Começar com um negócio por proprietário; evitar usar o papel global `ADMIN` como prova de propriedade | Define associação usuário/negócio |
| Identidade do cliente | Uma identidade de acesso e vínculos próprios com os negócios atendidos | Permite carteira isolada sem duplicar credenciais |
| Cadastro manual de clientes | Permitir cliente da carteira sem conta de acesso, caso necessário para atendimento por telefone/balcão | Separa cadastro comercial de autenticação |
| Forma de reserva | Confirmar se cliente agenda por conta própria, vendedor agenda por ele, ou ambos | Define endpoints e permissões |
| Disponibilidade | Horários semanais por dia, intervalos, bloqueios e fuso do negócio | Define cálculo dos horários disponíveis |
| Cancelamento e remarcação | Definir antecedência mínima e permissões de cada ator | Define transições e conflitos |
| Status | Avaliar `AGENDADO`, `CONCLUIDO`, `CANCELADO` e `NAO_COMPARECEU`; adicionar confirmação apenas se existir essa etapa | Define agenda, histórico e métricas |
| Indicadores financeiros | Exibir valor previsto dos agendamentos; não chamar isso de receita recebida sem controle de pagamento | Evita métricas enganosas |
| Dados de clínicas | Limitar o MVP aos dados necessários ao agendamento | Mantém prontuário e informações clínicas fora do modelo inicial |

## 4. Direção de arquitetura

Manter um monólito Spring Boot com banco relacional. O tamanho atual não justifica microsserviços. Evoluir por domínios de negócio, preservando a separação entre HTTP, regras e persistência.

Domínios propostos: autenticação, estabelecimentos, clientes, serviços, agenda e dashboard. A reorganização de pacotes deve acompanhar mudanças funcionais; renomear toda a base não é pré-requisito para corrigir os riscos.

Princípios:

- Obter o usuário a partir da autenticação e resolver os negócios aos quais ele tem acesso no servidor.
- Restringir consultas e alterações pelo estabelecimento autorizado, inclusive ao acessar um registro por ID.
- Validar também relações: serviço, cliente da carteira e recurso devem pertencer ao contexto permitido.
- Usar DTOs específicos para entrada e saída; não devolver entidades JPA nas APIs.
- Colocar transações nos casos de uso que precisam de atomicidade.
- Versionar mudanças de banco com migrations e testar sua aplicação.
- Manter contratos de erro, paginação e datas consistentes.

### Modelo de dados proposto

| Conceito | Responsabilidade |
|---|---|
| Usuário | Credenciais, estado da conta e identidade de acesso |
| Estabelecimento | Nome, fuso e configurações do negócio |
| Vínculo de acesso | Relação usuário/estabelecimento e permissão local, inicialmente proprietário |
| Cliente do estabelecimento | Dados comerciais e contato, com vínculo opcional a usuário autenticado |
| Serviço | Estabelecimento, nome, descrição, preço decimal, duração em minutos e estado ativo |
| Recurso/profissional | Unidade cuja capacidade é reservada; modelo depende da decisão de equipe |
| Agendamento | Negócio, cliente, serviço, recurso, início, fim, status e valores históricos |
| Disponibilidade/bloqueio | Expediente por dia, intervalos, folgas e exceções |

Salvar no agendamento o preço e a duração aplicados no momento da reserva, para que alterações do catálogo não modifiquem o histórico. Usar `BigDecimal` e coluna decimal para dinheiro; duração inteira em minutos; enums persistidos explicitamente como texto. Definir constraints, chaves estrangeiras e índices conforme as consultas reais.

Se já houver dados fora deste checkout, inventariá-los antes de migrar: detectar e-mails duplicados, relacionamentos inconsistentes e enums persistidos por ordinal. A migração precisa de backup, transformação explícita e verificação; não recriar tabelas com perda de dados.

## 5. Plano de execução

### Fase 0 — Execução reproduzível e diagnóstico técnico (P0)

1. Confirmar build com Java 21 e Maven Wrapper a partir de `beta`; verificar a resolução das dependências declaradas.
2. Criar configuração de exemplo sem segredos e separar configuração local, de testes e de produção.
3. Documentar banco e variáveis necessárias, inclusive a propriedade real `api.security.token.secret`.
4. Externalizar segredo e validade do JWT, validando configurações obrigatórias na inicialização.
5. Adicionar migrations para o esquema e dados mínimos de desenvolvimento, sem credenciais reais.
6. Corrigir README: pasta de execução, versão Java, rotas existentes e comandos para Windows.

Critério de conclusão: um checkout limpo compila, executa os testes e inicia seguindo a documentação, com banco preparado por migrations e sem segredo versionado.

### Fase 1 — Identidade, autorização e respostas seguras (P0)

1. Resolver a estratégia de identidade entre cliente e vendedor e a unicidade de login.
2. Implementar cadastro do proprietário/estabelecimento com atribuição de permissões feita no servidor.
3. Criar DTOs de resposta para usuário, vendedor, cliente, serviço e agendamento; remover senha/hash de toda saída.
4. Aplicar escopo do usuário/estabelecimento às consultas, atualizações, exclusões e cancelamentos.
5. Validar propriedade do serviço antes de alterá-lo ou excluí-lo.
6. Padronizar respostas de autenticação inválida, token expirado/malformado, acesso negado e recurso inexistente.
7. Configurar CORS para as origens do frontend por ambiente.
8. Definir ciclo de sessão: expiração, renovação e logout. Implementar refresh/revogação somente se a experiência exigir, com testes do ciclo escolhido.

Critério de conclusão: testes com dois estabelecimentos e dois clientes demonstram que não há leitura ou alteração cruzada, inclusive trocando IDs no corpo e na URL; nenhuma resposta contém credenciais ou hashes.

### Fase 2 — Consistência e concorrência da agenda (P0)

1. Exigir campos válidos, serviço ativo e relações coerentes com o estabelecimento.
2. Rejeitar reservas no passado e fora do expediente, incluindo intervalos e bloqueios.
3. Calcular início/fim respeitando duração e fuso; definir se reservas atravessando a meia-noite serão permitidas.
4. Detectar conflito por recurso usando intervalos semiabertos: há sobreposição quando `inicioExistente < novoFim` e `fimExistente > novoInicio`. Isso permite horários consecutivos.
5. Garantir atomicidade da verificação e gravação. Uma consulta antes de `save` não impede duas requisições simultâneas de reservar o mesmo horário.
6. Escolher e testar estratégia de concorrência no MySQL. Uma opção inicial é bloquear uma linha estável do recurso/agenda dentro da transação antes de consultar conflitos; bloquear apenas reservas existentes não protege um horário ainda vazio.
7. Centralizar criação e remarcação no mesmo mecanismo de reserva; definir transições de status e tornar o cancelamento repetido previsível.
8. Criar consulta de disponibilidade usando as mesmas regras da gravação; revalidar na confirmação porque a disponibilidade pode mudar.
9. Evitar duplicação por reenvio: definir chave de idempotência ou regra equivalente para criação, vinculada ao ator e ao conteúdo da requisição.

Critério de conclusão: reservas incompatíveis retornam conflito sem gravar dados; duas requisições concorrentes para um recurso de capacidade um resultam em apenas uma reserva ativa; cancelamento libera o horário e remarcação é atômica.

### Fase 3 — Gestão do negócio, catálogo e clientes (P1)

1. Completar consulta e edição do estabelecimento e expediente por dia da semana.
2. Implementar listagem, detalhe, criação, edição e inativação de serviços.
3. Preservar histórico ao inativar serviço; definir política para reservas futuras já feitas.
4. Implementar carteira de clientes por estabelecimento: busca, paginação, detalhe, edição e histórico de atendimentos.
5. Se confirmado no produto, permitir cadastro manual de cliente e reserva feita pelo vendedor.
6. Aplicar validação de tamanhos, preço, duração e dados de contato, com limites de paginação.

Critério de conclusão: o vendedor administra seu próprio negócio, catálogo e carteira; serviços inativos não recebem novas reservas; alterações cadastrais não corrompem o histórico.

### Fase 4 — APIs para a dashboard e jornada do cliente (P1)

1. Agenda com filtros por período, status e cliente, ordenação determinística e paginação quando apropriada.
2. Visões diária/semanal e próximos atendimentos, com limites de intervalo para evitar consultas ilimitadas.
3. Indicadores definidos explicitamente: agendamentos do dia, próximos atendimentos, cancelamentos e clientes atendidos no período.
4. Valor previsto calculado a partir dos preços históricos e dos status escolhidos na definição da métrica.
5. Para a jornada de autoagendamento, se confirmada: catálogo público com campos limitados, disponibilidade, criação, consulta das próprias reservas e cancelamento/remarcação.
6. Documentar contratos para o frontend, exemplos de sucesso/erro, formatos de datas e autenticação.

Critério de conclusão: a dashboard consegue carregar as informações necessárias sem buscar todo o banco nem calcular regras de negócio no navegador; métricas batem com cenários conhecidos e respeitam o fuso do negócio.

### Fase 5 — Qualidade e operação do primeiro lançamento (P1)

1. Automatizar build e testes no CI em cada alteração proposta.
2. Rodar testes de integração em banco isolado compatível com o MySQL utilizado, incluindo migrations e concorrência.
3. Configurar logs estruturados com identificador de requisição, sem tokens, senhas ou dados pessoais desnecessários.
4. Disponibilizar health checks e métricas operacionais com exposição controlada.
5. Definir backup, testar restauração e preparar procedimento de deploy e recuperação compatível com migrations.
6. Proteger autenticação contra tentativas abusivas com limites configuráveis e respostas previsíveis.
7. Definir retenção, acesso e exclusão/anonimização de dados pessoais conforme os requisitos do negócio; registrar alterações críticas com ator e data.
8. Medir consultas com volume representativo, corrigir N+1 e adicionar índices para agenda por negócio/recurso/período e carteira de clientes.

Critério de conclusão: ambiente de homologação validado com dados fictícios, logs suficientes para diagnóstico, restauração comprovada e fluxo principal aprovado antes do piloto.

## 6. Contrato HTTP proposto

Rotas abaixo são sugestões para implementação futura, não endpoints já existentes. Definir a convenção e atualizar o frontend/documentação em conjunto; se houver consumidores externos, planejar compatibilidade antes de substituir rotas.

| Área | Exemplos de rotas | Regra principal |
|---|---|---|
| Autenticação | `POST /auth/register`, `POST /auth/login`, `GET /me` | Cadastro não aceita elevação arbitrária de permissão |
| Negócio | `GET/PATCH /estabelecimentos/{id}` | Usuário precisa de vínculo autorizado |
| Serviços | `GET/POST /estabelecimentos/{id}/servicos`, `PATCH /estabelecimentos/{id}/servicos/{servicoId}` | Serviço deve pertencer ao negócio |
| Clientes | `GET/POST /estabelecimentos/{id}/clientes`, `GET/PATCH /estabelecimentos/{id}/clientes/{clienteId}` | Carteira isolada por negócio |
| Agenda | `GET/POST /estabelecimentos/{id}/agendamentos` | Consulta por período; criação valida capacidade e relações |
| Ações da agenda | `POST /estabelecimentos/{id}/agendamentos/{agendamentoId}/cancelamento` e `/remarcacao` | Transição autorizada e transacional |
| Dashboard | `GET /estabelecimentos/{id}/dashboard?inicio=...&fim=...` | Agregação no backend com semântica documentada |
| Cliente autenticado | `GET /me/agendamentos` | Identidade vem do token |

Evitar corpo em GET: o endpoint atual `/vendedor/agendamentos` deve evoluir para parâmetros de consulta e escopo autenticado. Usar um formato uniforme de erro com código estável, mensagem, erros de campos quando aplicável e identificador de requisição; não retornar stack traces. Distinguir entrada inválida, falta de autenticação, acesso negado, ausência de recurso e conflito de agenda.

## 7. Testes prioritários

Os testes devem acompanhar as fases, não ficar para depois da implementação.

| Grupo | Cenários essenciais |
|---|---|
| Identidade | Cadastro válido, e-mail duplicado, senha incorreta, token expirado/malformado e colisão entre perfis |
| Autorização | Cliente acessando reserva alheia; proprietário tentando ler/editar negócio, cliente, serviço ou reserva de outro negócio |
| Serialização | Respostas de vendedor, serviço e agendamento sem senha/hash, inclusive nos objetos relacionados |
| Reserva | Serviço inativo, serviço de outro vendedor, passado, dia fechado, intervalo, bloqueio e limite do expediente |
| Intervalos | Sobreposição parcial/total, horários consecutivos e reserva cancelada |
| Concorrência | Criações simultâneas e remarcações disputando a mesma disponibilidade no MySQL |
| Histórico | Alterar preço/duração do serviço não altera reservas anteriores |
| Dashboard | Filtros, paginação, totais, períodos vazios e mudança de data no fuso do estabelecimento |
| Operação | Banco vazio recebe migrations; atualização de esquema preserva dados; backup pode ser restaurado |

## 8. Sequência recomendada de entregas

Dividir cada fase em mudanças pequenas e verificáveis. Ordem inicial:

1. **Configuração e documentação executáveis:** build, banco e configuração de exemplo.
2. **Respostas seguras e autorização:** DTOs, escopo por usuário/negócio e testes de acesso cruzado.
3. **Identidade e entrada do vendedor:** resolver modelo de conta/negócio e cadastrar estabelecimento.
4. **Agenda confiável:** regras, transação, concorrência, cancelamento e remarcação.
5. **Catálogo e carteira:** funcionalidades que alimentam a dashboard.
6. **Dashboard e jornada de reserva:** consultas, indicadores e integração com o frontend.
7. **Homologação e piloto:** testes integrados, observabilidade, backup e recuperação.

As correções imediatas de exposição de dados e acesso cruzado podem ocorrer sobre o modelo atual. A modelagem definitiva de estabelecimento/recurso deve estar definida antes da conclusão da agenda concorrente e das consultas da dashboard.

Não estimar datas até fechar as decisões da seção 3, verificar o build e conhecer a disponibilidade de desenvolvimento. O progresso deve ser acompanhado pelos critérios de conclusão, não apenas pela quantidade de endpoints.

## 9. Evoluções posteriores (P2)

- Equipe com vários profissionais, permissões locais e capacidade por recurso, caso fique fora do MVP.
- Lembretes e notificações, com processamento assíncrono e controle de reenvio.
- Pagamentos, sinal e política de reembolso, se confirmados como parte do produto.
- Lista de espera, recorrência e pacotes de serviços.
- Relatórios avançados e exportação de dados.
- Estoque ou produtos físicos somente se houver uma necessidade de negócio distinta do catálogo de serviços.

## 10. Condição para considerar o backend pronto para o MVP

- [ ] Execução reproduzível e documentação alinhada ao código.
- [ ] Vendedor consegue entrar e configurar seu estabelecimento.
- [ ] Credenciais não aparecem nas respostas e dados estão isolados por negócio/usuário.
- [ ] Catálogo, carteira de clientes e agenda atendem à dashboard.
- [ ] Agenda impede conflitos inclusive sob concorrência.
- [ ] Cancelamento, remarcação e histórico têm regras verificadas.
- [ ] Contratos e erros permitem integração previsível com o frontend.
- [ ] Testes das regras críticas e CI passam.
- [ ] Homologação, monitoramento, backup e recuperação estão validados.

## 11. Arquivos que fundamentam a análise

- `beta/pom.xml`: stack e versões declaradas.
- `beta/src/main/java/com/marquify/beta/controllers/`: rotas atuais e respostas.
- `beta/src/main/java/com/marquify/beta/service/Agendamentoservice.java`: criação/cancelamento e ausência das regras de disponibilidade.
- `beta/src/main/java/com/marquify/beta/service/VendedorService.java`: gestão do vendedor e serviços por IDs recebidos.
- `beta/src/main/java/com/marquify/beta/service/AuthorizationService.java`: ordem de busca de identidades.
- `beta/src/main/java/com/marquify/beta/entity/`: modelo, credenciais, preço, duração e relacionamentos.
- `beta/src/main/java/com/marquify/beta/infra/security/`: JWT e autorização por perfil.
- `beta/src/main/java/com/marquify/beta/request/`: contratos de entrada existentes.
- `beta/src/test/java/com/marquify/beta/BetaApplicationTests.java`: único teste encontrado.
- `README.md` e `beta/.gitignore`: instruções de execução e tratamento atual da configuração.
