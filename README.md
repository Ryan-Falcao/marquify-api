# Marquify

Sistema de agendamento para estabelecimentos de serviços, com página pública, agenda de clientes, painel administrativo e área do profissional.

## Estrutura

- `beta`: API Spring Boot 4.1, Java 21, JPA/Hibernate, Flyway e PostgreSQL.
- `marquify-web`: frontend React, TypeScript e Vite.
- `ops`: guias e arquivos para testes e operação.
- `Planejamento/roadmap-proximas-atualizacoes.md`: referência do roadmap do produto.

## Executar localmente

1. Abra o Docker Desktop e inicie o PostgreSQL:

   ```powershell
   cd beta
   .\start-postgres.ps1
   ```

2. Inicie a API:

   ```powershell
   .\run-postgres.ps1
   ```

3. Em outro terminal, inicie o frontend:

   ```powershell
   cd ..\marquify-web
   npm install
   npm run dev
   ```

O frontend abre normalmente em `http://localhost:5173` e a API atende em `http://localhost:8080`.

## Verificações

```powershell
# Frontend
cd marquify-web
npm run typecheck
npm run build

# Backend
cd ..\beta
.\mvnw.cmd test
```

As migrations do Flyway são históricas: nunca edite uma já aplicada. Para mudanças de schema, crie uma nova migration.

## Operação

Consulte [ops/README.md](ops/README.md) para testes com PostgreSQL, deploy, health checks e backup.
