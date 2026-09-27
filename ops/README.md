# Operação do Marquify

## CI e bancos suportados nesta etapa

`.github/workflows/ci.yml` roda em pushes e pull requests: Java 21/Maven com H2, a mesma suíte contra PostgreSQL 17 real (incluindo concorrência) e Node 22 com `npm ci`, TypeScript e build. Relatórios de testes e artefatos ficam na execução do GitHub Actions. Configure proteção da branch exigindo os três jobs. O workflow não publica nem faz deploy automaticamente.

PostgreSQL é o banco escolhido para produção. O job real não usa H2 como substituto. MySQL não foi adicionado à matriz; sua dependência legada não equivale a homologação de produção.

Para testes reais locais, com Docker Desktop e Java 21 disponíveis:

```powershell
.\ops\test-postgres.ps1
```

Usa `localhost:55432`, projeto Compose `marquify-tests`, banco e credenciais descartáveis, armazenamento temporário. Nunca aponte `TEST_DB_URL` para produção. O script encerra apenas os containers de teste e restaura as variáveis do processo. A suíte escreve fixtures e aplica migrações.

## Deploy inicial

Pré-requisitos: host com Docker/Compose, DNS e terminador TLS com certificado, Java/Node apenas no estágio de build. Exemplo de referência usa um host e uma instância da API. Não colocar `.env`, dumps ou chaves em Git.

1. Use um commit aprovado no CI. Copie `ops/.env.example` para `ops/.env`; configure senha aleatória do banco, segredo JWT de pelo menos 32 bytes aleatórios, URL pública HTTPS e `RELEASE_TAG` com o SHA do commit. Não altere o segredo JWT a cada deploy, pois isso invalida sessões.
2. Execute da raiz:

```powershell
docker compose --env-file ops/.env -f ops/compose.prod.yml build
docker compose --env-file ops/.env -f ops/compose.prod.yml up -d
curl.exe --fail http://localhost:8080/actuator/health/readiness
```

3. Aguarde readiness `UP`. A API aplica Flyway antes de atender. O frontend fala com `/api` no mesmo domínio; React Router tem fallback para `index.html`. Banco não publica porta; API e web publicam apenas em loopback. Volumes persistem PostgreSQL e fotos de serviços. Logo/capa da página pública ficam no banco.
4. Configure o terminador HTTPS para encaminhar o domínio para `127.0.0.1:8088`. Valide cadastro/login, catálogo, reserva, remarcação, cancelamento e persistência de imagem. Apenas disponibilize o domínio após esses checks.
5. Se a API for recriada com novo IP interno, recrie/reinicie também o serviço `web` para atualizar a resolução do upstream Nginx.

As imagens têm tags por release. O Dockerfile da API empacota sem repetir testes; somente utilize releases aprovadas no CI. Para produção maior, publique essas imagens em um registry controlado e faça deploy por digest.

## Health e observabilidade

- `/actuator/health/liveness`: processo ativo, independente do banco. Use para reinício somente em falhas persistentes.
- `/actuator/health/readiness`: prontidão e conectividade com banco; falha deve retirar a instância do tráfego, não disparar loop de reinício por indisponibilidade do PostgreSQL.
- `/actuator/health`: status geral. Detalhes internos não são expostos. Outros endpoints Actuator não são publicados.

Os probes são acessíveis na porta local da API; Nginx bloqueia `/api/actuator/`. Configure monitor externo pelo host/agente. Sugestão inicial: verificar a cada 15s, alertar após três falhas, acompanhar 5xx, latência, 429, espaço livre e validade do último backup. Compose reinicia processos encerrados; não há escalonador de probes incluído.

Perfil `prod` escreve JSON ECS no stdout usando o [suporte nativo do Spring Boot](https://docs.spring.io/spring-boot/reference/features/logging.html). Cada requisição recebe `X-Request-ID`, incluído nos logs com método, rota-modelo, status e duração. O filtro não registra bodies, query strings, senhas ou JWT. Restrinja acesso/retenção de logs e não habilite logs SQL/bind/debug de segurança em produção. Encaminhe stdout ao coletor do ambiente; configure rotação no Docker para evitar disco cheio.

## Limite de login

API direta: 10 tentativas por IP a cada 60 segundos, incluindo sucessos; excesso retorna JSON 429 e `Retry-After`. Configurações: `API_LOGIN_RATE_LIMIT_MAX_ATTEMPTS`, `API_LOGIN_RATE_LIMIT_WINDOW_SECONDS`, `API_LOGIN_RATE_LIMIT_MAX_CLIENTS`. Estado limitado a 10 mil IPs, por processo e perdido no reinício. Saturação da tabela bloqueia novos IPs brevemente; não cresce indefinidamente.

O endereço vem da conexão, não de `X-Forwarded-For` enviado pelo cliente. Na topologia Compose, a API vê o Nginx e tem um teto agregado de 300/minuto; Nginx aplica 10/minuto com burst 5 por endereço que chega a ele. O login de proprietário mostra a mensagem de 429; clientes também recebem a mensagem da API.

**Proxy TLS/CDN:** se houver outro proxy antes do Nginx, configure `set_real_ip_from` SOMENTE para os endereços desse proxy e `real_ip_header` conforme o provedor; bloqueie acesso direto ao upstream. Sem isso, usuários compartilham a cota do proxy. Nunca confie em qualquer origem nem habilite cabeçalhos encaminhados indiscriminadamente. Para múltiplas réplicas, o limite de borda deve ser compartilhado (gateway/Redis); o limitador em memória não é global. Para ataques distribuídos, evolua com limites por conta e desafios progressivos.

## Atualização e rollback

1. Faça backup consistente e ensaie a restauração antes de migrações relevantes.
2. Registre SHA/tag anterior, versão do PostgreSQL, versão Flyway e localização do backup. Mantenha imagens anteriores disponíveis.
3. Em janela de manutenção, atualize `RELEASE_TAG`, faça build, suba API/web e valide readiness e smoke tests.
4. Se falhar e o schema continuar compatível, volte a tag anterior e suba com `--no-build`. Se houve migração incompatível, não improvise downgrade do schema: restaure em banco novo e faça a troca controlada. Restaurar backup pode perder alterações posteriores ao dump; registre esse impacto antes da troca.

## Backup

```powershell
.\ops\backup.ps1
```

O script para a API durante o dump e a cópia das imagens e reinicia ao final. Planeje a breve indisponibilidade. Executar com aplicação inicialmente saudável; reinicie `web` se o endereço da API mudar. Resultado: `ops/backups/<data>/database.dump`, `service-images/`, `checksums.json`, `COMPLETE`. Sem marcador `COMPLETE`, trate como backup falho. Dump usa formato customizado do PostgreSQL; não há redirecionamento binário pelo PowerShell.

Automatize fora deste repositório conforme o host (Agendador de Tarefas/systemd/serviço gerenciado), com alerta em falha e conferência do marcador. Política inicial: diário, 7 diários + 4 semanais + 3 mensais; criptografe e copie para armazenamento externo. Meta inicial de RPO: até 24h; RTO deve ser medido no ensaio, não prometido. Para RPO menor, adote backups contínuos/WAL/PITR.

Segredos, certificados, `.env` e configurações do host precisam de cópia segura separada. Não deixe a única cópia no mesmo disco do banco. Checksums detectam corrupção acidental; não autenticam um backup de origem desconhecida.

## Ensaio de restauração

```powershell
.\ops\restore-rehearsal.ps1 -BackupDirectory .\ops\backups\<data>
```

Valida checksums, cria PostgreSQL 17 isolado sem porta pública, executa `pg_restore --exit-on-error`, consulta agendamentos e remove somente o container temporário criado pelo script. Confere arquivos de imagem, mas não exercita a aplicação sobre o banco restaurado.

## Restauração de produção

1. Abra janela de manutenção e pare API/web. Preserve banco/volumes atuais; não rode `down -v`.
2. Valide checksums e faça o ensaio acima. Prepare **novo banco vazio** e **novo volume de imagens**; mantenha originais para recuperação.
3. Copie `database.dump` para o novo servidor PostgreSQL e execute `pg_restore --no-owner --no-acl --exit-on-error -U <usuario> -d <banco_novo> <dump>`. Não use `--clean` sobre o banco atual. Use versão PostgreSQL compatível com o dump (referência: 17).
4. Copie `service-images/` para o novo volume e dê acesso ao UID 10001 da API. Verifique quantidade, hashes e referências das fotos. Use o mesmo conjunto de dump+imagens.
5. Aponte `DB_URL` e o volume da API para os novos recursos, mantendo a chave JWT e a tag compatível com o backup. Suba API, confira Flyway/readiness e compare contagens de estabelecimentos, serviços e agendamentos com o backup.
6. Faça smoke tests de login, leitura de agenda, fotos e personalização. Suba/recrie web e libere tráfego somente após validação. Registre o ponto restaurado, tempo gasto e eventuais dados perdidos.

Nenhum deploy, agendamento de backup ou restauração de produção é executado automaticamente por estes arquivos.
