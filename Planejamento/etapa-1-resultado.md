# Etapa 1 — Ambiente e execução reproduzível

Implementada em 25/09/2026.

## Entregas

- Java 21 validado com o Maven Wrapper 3.9.16 e Spring Boot 4.1.0, mantendo as versões declaradas no projeto.
- Configuração comum em `application.yml` e perfis `local`/`prod`, sem credenciais reais versionadas.
- Banco e JWT configurados por variáveis de ambiente; validade JWT configurável e validação do segredo/validade na criação do serviço.
- Compose para MySQL 8.4 local, com health check, volume persistente e porta publicada apenas no loopback.
- `.env.example` para o Compose, com instruções explícitas sobre a configuração separada da aplicação.
- Flyway com migration V1 equivalente ao modelo existente, baseline automático desativado e Hibernate em `validate`.
- Perfil de testes isolado: H2 por padrão e MySQL opcional por variáveis `TEST_DB_*`.
- Testes de inicialização/migrations, reaplicação sem alterações e configuração/emissão JWT.
- README atualizado com requisitos, comandos PowerShell, banco, perfis, variáveis e rotas reais.

## Verificação realizada

- Build e empacotamento pelo Maven Wrapper com JDK 21.
- Quatro testes aprovados em H2 e quatro em MySQL 8.4.6; nenhum teste ignorado.
- Aplicação da migration em banco MySQL vazio e validação do esquema pelas entidades JPA.
- Segunda chamada ao Flyway sem migrations executadas.
- JAR iniciado contra outro banco MySQL isolado, com perfil `local`.
- Cadastro HTTP com sucesso e login retornando JWT, usando apenas credenciais fictícias.
- Verificação de whitespace com `git diff --check`.

O Java do terminal era o 8 e não havia MySQL/Docker disponível nos caminhos verificados. Para validar, foram baixados JDK 21 e MySQL portáteis na pasta `.tools`, ignorada pelo Git, sem instalar serviços no Windows ou alterar o Java global. Os processos temporários foram encerrados após os testes; a pasta local permanece disponível. O Compose foi preparado, mas sua execução não foi testada neste computador, pois Docker não estava disponível. A validação real do banco usou o MySQL portátil.

## Limites e continuidade

- Esta migration destina-se a banco novo. Um banco existente exige inventário e plano de adoção antes de usar Flyway; não foi feita migração de dados reais.
- Não foram criadas contas padrão ou seeds de usuários com senha fixa. O endpoint de cadastro permite criar o cliente fictício de desenvolvimento; cadastro de vendedor continua previsto em etapa posterior.
- O esquema preserva as escolhas atuais de domínio, incluindo preço em `Double`, duração em `LocalTime` e enums ordinais.
- A primeira etapa não corrige acesso cruzado por IDs, exposição de entidades ou conflitos de agenda. A próxima entrega deve tratar respostas seguras e autorização por proprietário/estabelecimento.
