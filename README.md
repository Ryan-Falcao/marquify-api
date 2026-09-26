# Marquify API

Backend de agendamento para barbearias, clínicas e outros prestadores, com gestão de agenda, clientes e serviços. Consulte o [planejamento](Planejamento/amadurecimento-backend.md) para o diagnóstico e as próximas etapas.

## Requisitos

- **JDK 21**, Spring Boot 4.1.0 e Maven Wrapper (não exige Maven global).
- MySQL 8.4 para execução local; Docker Compose é uma opção para preparar o banco.
- Internet no primeiro build, para baixar o Maven e as dependências.
- Flyway versiona o esquema; Hibernate apenas valida a compatibilidade das tabelas.

O projeto Maven fica em **beta**. Os exemplos usam PowerShell 7 a partir da raiz do repositório.

O frontend de agendamento fica em **marquify-web**. Depois de iniciar a API, execute `node serve.mjs` dentro dessa pasta e abra `http://localhost:5173`.

## 1. Compilar e testar

```powershell
cd beta
# Ajuste para o caminho do seu JDK 21:
$env:JAVA_HOME = 'C:\caminho\para\jdk-21'
& "$env:JAVA_HOME\bin\java.exe" -version
.\mvnw.cmd -version
.\mvnw.cmd -B verify
```

Neste checkout foi preparado um JDK portátil em `.tools/jdk21`, ignorado pelo Git. Se essa pasta existir, é possível selecioná-lo sem alterar o Java do Windows:

```powershell
# Dentro de beta:
$env:JAVA_HOME = (Get-ChildItem ../.tools/jdk21 -Directory | Select-Object -First 1).FullName
```

Em Linux/macOS, use `./mvnw` com `JAVA_HOME` configurado para o JDK 21.

Os testes ativam o perfil `test`, usam H2 em memória e aplicam a mesma migration inicial. Não precisam de MySQL nem usam dados reais. H2 não substitui a verificação no MySQL descrita adiante. Sua configuração e dependência ficam somente no classpath de testes.

Para executar os mesmos testes em MySQL, prepare um banco **exclusivo para testes**, inicialmente vazio, e use:

```powershell
$env:TEST_DB_URL = 'jdbc:mysql://localhost:3306/marquify_test'
$env:TEST_DB_USERNAME = 'marquify'
$env:TEST_DB_PASSWORD = Read-Host 'Senha do banco de testes' -MaskInput
.\mvnw.cmd -B verify
Remove-Item Env:TEST_DB_URL, Env:TEST_DB_USERNAME, Env:TEST_DB_PASSWORD
```

O usuário deve ter permissões nesse banco. Esses testes aplicam migrations e verificam uma agenda vazia; nunca use um banco de produção. Remover as variáveis volta a selecionar H2 nas próximas execuções.

## 2. Preparar o MySQL local

### Com Docker Compose

Dentro de `beta`, prepare um arquivo local sem sobrescrever configurações existentes:

```powershell
if (!(Test-Path .env)) { Copy-Item .env.example .env }
```

Edite `.env` e substitua os placeholders por duas senhas diferentes. Depois:

```powershell
docker compose up -d --wait mysql
docker compose ps
```

O serviço cria o banco `marquify` e o usuário `marquify`, publica a porta apenas em `127.0.0.1:3306` e persiste dados em volume. Se a porta estiver ocupada, altere `MYSQL_PORT` no `.env` e ajuste `DB_URL` na aplicação.

`docker compose stop` para o serviço preservando os dados. As credenciais de inicialização só são aplicadas quando o volume está vazio; alterar `.env` não altera usuários de um banco já inicializado.

### Com um MySQL existente

Crie um **banco vazio dedicado ao desenvolvimento** e um usuário com permissão para criar/alterar tabelas e consultar/gravar dados nesse banco. Forneça `DB_URL`, `DB_USERNAME` e `DB_PASSWORD` correspondentes. O Flyway cria as tabelas ao iniciar.

Não aponte esta primeira migration para um banco existente com dados sem revisar o esquema e planejar sua adoção. O baseline automático está desativado: um banco não vazio sem histórico Flyway deve falhar, evitando assumir que seu esquema já corresponde à migration. Não use `ddl-auto=update` nem apague dados para contornar essa falha.

## 3. Configurar e executar

### Demonstração sem MySQL

Para testar a landing e o cadastro comercial sem instalar MySQL, use o perfil temporário `demo`. Os dados são apagados ao encerrar a API:

```powershell
# Dentro de beta, com JAVA_HOME apontando para o JDK 21:
$env:SPRING_PROFILES_ACTIVE = 'demo'
.\mvnw.cmd spring-boot:run
```

O servidor permanece em `http://localhost:8080`. Use esse perfil somente para demonstração local.

### MySQL

O Compose lê `.env`; **Spring Boot e Maven não o carregam automaticamente**. Configure as variáveis na sessão que iniciará a API:

```powershell
$env:SPRING_PROFILES_ACTIVE = 'local'
$env:DB_URL = 'jdbc:mysql://localhost:3306/marquify'
$env:DB_USERNAME = 'marquify'
# Mesma senha de DB_PASSWORD do .env, sem registrá-la no histórico:
$env:DB_PASSWORD = Read-Host 'Senha do usuário MySQL' -MaskInput

# Chave aleatória local; não é impressa nem gravada no repositório:
$jwtBytes = New-Object byte[] 48
$jwtGenerator = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$jwtGenerator.GetBytes($jwtBytes)
$jwtGenerator.Dispose()
$env:JWT_SECRET = [Convert]::ToBase64String($jwtBytes)
$env:JWT_EXPIRATION = '2h'

.\mvnw.cmd spring-boot:run
```

A entrada mascarada requer PowerShell 7. Em outro shell, forneça a variável pelo mecanismo seguro disponível no ambiente.

A API atende em `http://localhost:8080`. O perfil `local` fornece apenas URL/usuário padrão; senha do banco e segredo JWT continuam obrigatórios. Gerar outra chave invalida os tokens anteriores. Para manter sessões entre reinicializações, preserve a chave fora do repositório.

| Variável | Finalidade | Padrão |
|---|---|---|
| SPRING_PROFILES_ACTIVE | local ou prod | Nenhum |
| DB_URL | URL JDBC | Obrigatória; local: jdbc:mysql://localhost:3306/marquify |
| DB_USERNAME | Usuário do banco | Obrigatório; local: marquify |
| DB_PASSWORD | Senha do banco | Obrigatória |
| JWT_SECRET | Segredo com pelo menos 32 bytes UTF-8 | Obrigatório |
| JWT_EXPIRATION | Validade entre 1s e 30d | 2h |
| PORT | Porta HTTP | 8080 |

As propriedades internas do JWT são `api.security.token.secret` e `api.security.token.expiration`. Configuração inválida de segredo ou validade impede a criação do serviço de tokens na inicialização.

Em produção, use `SPRING_PROFILES_ACTIVE=prod` e forneça todas as credenciais por variáveis/gestor de segredos. O Compose é destinado ao desenvolvimento. Esta preparação do ambiente não conclui as correções de autorização e agenda previstas no planejamento.

## 4. Verificar migrations e autenticação

No log de inicialização, confirme a aplicação da versão 1 pelo Flyway e a inicialização da aplicação. Em uma segunda inicialização, a migration deve ser reconhecida como aplicada, sem recriar tabelas.

Para consultar o banco com o Compose:

```powershell
docker compose exec mysql mysql -u marquify -p marquify
```

No cliente MySQL:

```sql
SELECT version, description, success FROM flyway_schema_history;
SHOW TABLES;
```

A migration cria `clientes`, `vendedor`, `vendedor_dias_abertos`, `servicos` e `agendamentos`. Preserva a modelagem atual, inclusive enums ordinais e preço em Double. Não altere uma migration já aplicada: crie a próxima versão.

Não são criadas contas com senha padrão. Para obter um cliente fictício de desenvolvimento, use `/auth/register` com login único e senha escolhida localmente. Depois use `/auth/login` e envie o token no header `Authorization: Bearer SEU_TOKEN`. O cadastro de vendedor ainda não está implementado.

## Endpoints existentes

| Método | Rota | Função |
|---|---|---|
| POST | /auth/register | Cadastrar cliente (login, senha) |
| POST | /auth/login | Autenticar e retornar JWT |
| POST | /agendamento | Criar agendamento |
| PUT | /agendamento/cancelar | Cancelar por agendamentoId no corpo |
| GET | /vendedor/{id} | Consultar vendedor |
| GET | /vendedor/agendamentos | Listar por vendedor_id no corpo (contrato atual) |
| PUT | /vendedor/mudarNome | Alterar nome |
| PUT | /vendedor/mudarNomeLoja | Alterar nome da loja |
| PUT | /vendedor/mudarDiasAbertos | Alterar dias de funcionamento |
| PUT | /vendedor/mudarHoraAbertura | Alterar abertura |
| PUT | /vendedor/mudarHoraFechamento | Alterar fechamento |
| POST | /vendedor/criarServico | Criar serviço |
| POST | /auth/cadastro-comercial | Criar estabelecimento e conta administrativa |
| DELETE | /vendedor/deletarServico | Desativar serviço (rota legada) |
| GET | /vendedor/{vendedorId}/profissionais | Listar profissionais do estabelecimento |
| POST | /vendedor/{vendedorId}/profissionais | Criar profissional |
| PUT | /vendedor/{vendedorId}/profissionais/{profissionalId} | Alterar nome do profissional |
| PATCH | /vendedor/{vendedorId}/profissionais/{profissionalId}/ativar | Ativar profissional |
| PATCH | /vendedor/{vendedorId}/profissionais/{profissionalId}/desativar | Desativar profissional |
| GET | /estabelecimentos/{estabelecimentoId}/servicos | Listar catálogo do estabelecimento |
| POST | /estabelecimentos/{estabelecimentoId}/servicos | Criar serviço no catálogo |
| PUT | /estabelecimentos/{estabelecimentoId}/servicos/{servicoId} | Atualizar serviço e profissionais |
| PATCH | /estabelecimentos/{estabelecimentoId}/servicos/{servicoId}/ativar | Ativar serviço |
| PATCH | /estabelecimentos/{estabelecimentoId}/servicos/{servicoId}/desativar | Desativar serviço |
| GET | /publico/estabelecimentos/{estabelecimentoId} | Dados públicos do estabelecimento |
| GET | /publico/estabelecimentos/{estabelecimentoId}/servicos | Catálogo ativo para clientes |
| GET | /publico/estabelecimentos/{estabelecimentoId}/profissionais | Profissionais ativos para clientes |
| GET | /publico/estabelecimentos/{estabelecimentoId}/profissionais/{profissionalId}/horarios?servicoId={id}&data=YYYY-MM-DD | Horários livres para agendamento |
| GET | /vendedor/{vendedorId}/profissionais/{profissionalId}/disponibilidade | Consultar jornada semanal |
| PUT | /vendedor/{vendedorId}/profissionais/{profissionalId}/disponibilidade | Substituir jornada semanal |

`/auth/**` é público, `/vendedor/**` exige ADMIN e `/agendamento/**` exige USER. Além do perfil, as operações verificam a identidade e a propriedade do registro. Um vendedor só consulta/altera seu próprio cadastro, serviços e agenda. Um cliente só cria reservas para si e cancela as próprias; um vendedor ADMIN pode cancelar reservas da sua agenda.

## Contratos de segurança e integração

- O JWT identifica a conta por `cliente:<id>` ou `vendedor:<id>`. Tokens antigos baseados apenas em e-mail são rejeitados: faça login novamente após atualizar o backend.
- A criação e edição de serviço exigem `profissionaisIds`, com pelo menos um profissional ativo do estabelecimento do proprietário. O catálogo pertence ao estabelecimento; a resposta inclui os resumos desses profissionais.
- A criação de agendamento obtém o cliente pelo token. `clienteId` pode ser omitido; se informado, deve corresponder ao cliente autenticado. A requisição também exige `profissionalId`; o profissional deve estar ativo, pertencer ao estabelecimento do serviço e oferecer o serviço selecionado.
- Criar reservas em nome de clientes usando uma conta de vendedor ainda não é permitido; essa jornada depende da modelagem futura da carteira de clientes.
- O cancelamento localiza o agendamento pelo ID e pelo dono autorizado. Recurso inexistente ou fora desse escopo retorna 404. IDs de vendedor diferentes da conta autenticada nas operações de gestão retornam 403.
- Perfil do vendedor retorna apenas `id`, `nome`, `email`, `nomeLoja`, horários e dias abertos.
- Serviço retorna `id`, `nome`, `descricao`, `preco`, `tempo`, `ativo`, `estabelecimentoId`, `vendedorId` e os profissionais associados (`id`, `nome`), sem entidades aninhadas. `estabelecimentoId` é o dono canônico do catálogo; `vendedorId` permanece temporariamente para compatibilidade com as rotas legadas.
- A gestão do catálogo usa `/estabelecimentos/{estabelecimentoId}/servicos` e só aceita o estabelecimento da conta autenticada. A listagem inclui itens inativos para gestão; serviços inativos continuam no histórico, mas não podem receber novos agendamentos.
- As rotas em `/publico/**` não exigem token e retornam somente dados necessários para a descoberta: estabelecimento ativo, serviços ativos e profissionais ativos. Elas não expõem e-mail, senha, horários internos ou datas administrativas.
- A disponibilidade pública retorna inícios de horário em intervalos de 15 minutos, respeitando a jornada semanal do profissional, a duração do serviço e agendamentos já confirmados. A criação de agendamento faz a mesma validação no servidor e rejeita sobreposições.
- CORS aceita por padrão `http://localhost:3000` e `http://localhost:5173`. Em outro ambiente, defina `CORS_ALLOWED_ORIGINS` com as origens separadas por vírgula.
- A gestão de profissionais usa as rotas aninhadas em `/vendedor/{vendedorId}/profissionais`. O ID deve ser o mesmo da conta autenticada; profissionais de outro estabelecimento não podem ser consultados ou alterados.
- Agendamento retorna seus dados e resumos: cliente (`id`, `nome`), vendedor (`id`, `nomeLoja`), estabelecimento (`id`, `nome`, `fusoHorario`), profissional (`id`, `nome`) e o DTO de serviço. Não retorna senha, hash, permissões ou contatos pessoais aninhados.
- Cadastro rejeita login já existente em qualquer uma das duas tabelas. Se dados legados contiverem mais de uma conta para o mesmo login, o login é recusado, sem escolher uma identidade arbitrariamente. Unicidade transacional global e unificação de identidades permanecem na próxima discussão de modelo.
- Cabeçalho Authorization inválido, token expirado/malformado, token de conta removida ou senha incorreta retornam 401; falta de permissão retorna 403. Os erros tratados usam `status`, `codigo` e `mensagem`, sem stack trace.

Exemplo de erro:

```json
{"status":403,"codigo":"ACESSO_NEGADO","mensagem":"Acesso negado"}
```

Os testes de integração usam o filtro JWT e os endpoints reais via MockMvc, com dois clientes e dois vendedores, e revertem suas gravações ao final de cada teste. A jornada semanal, consulta de horários livres e bloqueio de sobreposições já estão cobertos. Ainda faltam bloqueios pontuais, remarcação e o cadastro de vendedor. Veja os resultados da [etapa 1](Planejamento/etapa-1-resultado.md), [etapa 2](Planejamento/etapa-2-resultado.md) e o planejamento de [disponibilidade](Planejamento/etapa-3j-disponibilidade-profissionais.md).

## Referência técnica

A integração Flyway usa o starter específico do Spring Boot 4, conforme a [documentação oficial dos starters](https://docs.spring.io/spring-boot/4.0/reference/using/build-systems.html).
