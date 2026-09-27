# Roadmap das próximas atualizações

## Objetivo

Amadurecer a experiência de clientes, proprietários e profissionais antes da cobrança, mantendo segurança, histórico de agendamentos e uma evolução simples de operar.

## Ordem recomendada

### 1. Remarcação e cancelamento

Reformular a área **Meus agendamentos** para que cada compromisso apresente serviço, estabelecimento, profissional, data, horário, preço e status com boa leitura no celular.

Entregas:

- separar visualmente agendamentos futuros, concluídos e cancelados;
- abrir remarcação em um fluxo guiado: nova data, novo horário, revisão e confirmação;
- exibir as regras de cancelamento antes da ação;
- pedir confirmação em modal, sem usar mensagens nativas do navegador;
- mostrar feedback de sucesso e atualizar o card imediatamente;
- informar quando o prazo de cancelamento ou remarcação tiver terminado;
- manter o preço e a duração originais no histórico;
- garantir novamente no backend que o horário continua disponível no momento da confirmação.

Critério de conclusão: o cliente consegue remarcar ou cancelar pelo celular sem precisar entrar em contato com o estabelecimento e sem criar conflito de agenda.

### 2. Configurações do estabelecimento

Criar a nova opção **Configurações** na navegação administrativa.

Seções:

- **Dados do negócio:** nome, descrição, telefone, endereço e fuso horário;
- **Agendamento:** antecedência mínima, janela máxima para agendar, intervalo entre serviços e regras de cancelamento;
- **Página pública:** URL personalizada, QR Code e acesso à personalização visual;
- **Notificações e calendário:** Google Agenda e preferências futuras de aviso;
- **Equipe e permissões:** convites, acessos e profissionais arquivados;
- **Plano e cobrança:** plano atual, limites e futura assinatura;
- **Conta:** alteração de senha e encerramento da conta.

Adicionar na área de capa os botões **Trocar imagem** e **Excluir imagem**, com confirmação e atualização imediata da prévia e da página pública.

### 3. Exclusão e arquivamento de serviços e profissionais

Um profissional ou serviço que já participou de um agendamento não deve ser apagado fisicamente, pois isso quebraria histórico, faturamento e relatórios. Para o usuário, a ação continuará se chamando **Excluir**, mas internamente será um arquivamento.

Regras:

- itens excluídos não aparecem no catálogo público, seleção de agenda, filtros comuns ou listas ativas;
- a área administrativa terá um filtro **Arquivados** para consultar e restaurar;
- agendamentos antigos continuam mostrando o nome, preço e duração registrados na época;
- impedir novos vínculos e agendamentos com itens arquivados;
- permitir exclusão física apenas quando o registro nunca tiver sido usado e não possuir relacionamentos;
- substituir o atual estado “inativo permanentemente visível” por ações claras: **Pausar**, **Restaurar** e **Excluir**.

Critério de conclusão: excluir limpa as telas operacionais sem destruir dados históricos.

### 4. Acesso próprio dos profissionais

Criar um perfil `PROFISSIONAL`, separado do proprietário `ADMIN` e do cliente `USER`.

Fluxo:

- proprietário cadastra o profissional e envia um convite por link;
- profissional define sua própria senha;
- JWT identifica o profissional e o estabelecimento aos quais ele pertence;
- profissional acessa um painel próprio, sem entrar na dashboard administrativa.

Permissões iniciais:

- visualizar somente os próprios agendamentos;
- consultar agenda diária e semanal;
- visualizar os dados necessários do cliente;
- marcar atendimento como iniciado, concluído ou ausência;
- consultar sua jornada, pausas e bloqueios;
- solicitar ou registrar bloqueio, conforme permissão definida pelo proprietário;
- não alterar catálogo, equipe, faturamento geral, personalização ou assinatura.

Preparar a autorização no backend por papel e vínculo, nunca por um `profissionalId` recebido sem validação.

Critério de conclusão: um funcionário acessa apenas sua rotina e não consegue consultar ou alterar dados administrativos.

### 5. URL pública personalizada

Formato desejado:

`/agendar/{slug}`

Exemplo:

`marquify.com/agendar/barbearia-do-lucas`

Regras:

- aceitar letras minúsculas, números e hífen;
- normalizar acentos e espaços;
- garantir unicidade sem diferenciar maiúsculas de minúsculas;
- reservar termos como `admin`, `login`, `api`, `suporte`, `configuracoes` e `minha-conta`;
- validar disponibilidade enquanto o proprietário digita;
- permitir alteração com confirmação;
- manter redirecionamento do slug antigo por um período para não invalidar QR Codes já impressos;
- gerar novamente link e QR Code depois da publicação.

Critério de conclusão: o estabelecimento escolhe uma URL legível, exclusiva e segura na tela de Configurações.

### 6. Google Agenda

Dividir a integração em duas entregas.

#### 6.1. Entrega rápida, sem OAuth

- mostrar **Adicionar ao Google Agenda** após a confirmação;
- disponibilizar arquivo `.ics` compatível também com Apple Calendar e Outlook;
- preencher título, serviço, profissional, estabelecimento, início, fim e observações;
- disponibilizar a mesma ação em **Meus agendamentos**;
- usar o fuso horário do estabelecimento.

Essa versão já permite que o próprio Google envie os alertas configurados pelo cliente.

#### 6.2. Sincronização conectada

- conectar conta Google por OAuth 2.0;
- guardar tokens criptografados e permitir revogação;
- criar evento automaticamente após confirmação;
- atualizar evento ao remarcar;
- cancelar ou remover evento ao cancelar;
- evitar eventos duplicados com um identificador externo por agendamento;
- tratar expiração de autorização e falhas de sincronização;
- permitir integração tanto para clientes quanto para profissionais.

Critério de conclusão: o ciclo de criação, remarcação e cancelamento permanece consistente entre Marquify e Google Agenda.

### 7. Revisão geral de UI/UX

Executar depois dos fluxos acima para trabalhar sobre telas definitivas.

Prioridades:

- experiência mobile da página pública e área do cliente;
- estados vazios, carregamento, erro e sucesso;
- contraste automático ou aviso quando uma combinação personalizada ficar ilegível;
- confirmação visual para ações destrutivas;
- formulários com mensagens junto ao campo incorreto;
- navegação por teclado, foco visível e rótulos acessíveis;
- consistência de botões, espaçamentos, modais e cards;
- prévia de personalização fiel à página publicada.

## Estratégia de assinatura

### Recomendação

Lançar a fase beta gratuitamente e adotar um modelo **freemium** quando agenda, equipe e integrações estiverem estáveis. Cobrar de todos desde o primeiro contato aumenta a dificuldade de aquisição em um produto novo; um plano gratuito limitado permite que o estabelecimento entenda o valor antes de pagar.

### Plano Gratuito

- 1 estabelecimento;
- 1 profissional;
- até 5 serviços ativos;
- até 30 novos agendamentos por mês;
- página pública com marca Marquify;
- link público gerado automaticamente;
- gestão básica de agenda e clientes;
- botão manual para adicionar ao calendário.

### Plano Profissional — R$ 30/mês

- profissionais, serviços e agendamentos sem os limites do plano gratuito, sujeitos a uma política de uso justo;
- URL personalizada;
- personalização completa da página pública;
- acesso individual dos profissionais;
- sincronização automática com Google Agenda;
- relatórios e indicadores completos;
- bloqueios, pausas e regras avançadas;
- remoção ou redução da marca Marquify;
- suporte prioritário.

### Antes de cobrar

- medir quantos estabelecimentos chegam ao primeiro agendamento;
- medir agendamentos mensais e número médio de profissionais;
- validar se R$ 30 cobre infraestrutura, suporte, mensagens e taxas do meio de pagamento;
- definir período de teste do plano profissional, recomendado em 14 dias;
- implementar controle de assinatura no backend, sem confiar apenas no frontend;
- definir tolerância para falha de pagamento e política de downgrade sem apagar dados;
- publicar termos, política de privacidade e regras de cancelamento da assinatura.

## Sequência de execução

1. Remarcação e cancelamento.
2. Tela de Configurações e exclusão de capa.
3. Arquivamento de serviços e profissionais.
4. URL personalizada.
5. Painel e autenticação dos profissionais.
6. Exportação para calendário por link e `.ics`.
7. Sincronização Google com OAuth.
8. Revisão geral de UI/UX e acessibilidade.
9. Assinatura, limites e cobrança.

## Primeira próxima entrega

Começar pela reformulação de **Meus agendamentos**, incluindo os novos cards, detalhes do compromisso, remarcação guiada, cancelamento com regras e estados de sucesso ou erro. Essa tela será também o ponto de entrada para a primeira integração com Google Agenda.
