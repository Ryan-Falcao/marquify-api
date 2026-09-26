# Ambiente local com PostgreSQL

## Teste local sem Docker (SQLite)

Para testar cadastro, login e dashboard sem instalar banco ou Docker, execute:

```powershell
powershell -ExecutionPolicy Bypass -File .\run-sqlite.ps1
```

O banco será criado em `data/marquify.db` e continua existindo após reiniciar a API. Ele é exclusivo para demonstração local; a produção deve usar PostgreSQL.

## Ambiente local com PostgreSQL

1. Instale e abra o Docker Desktop.
2. Execute `powershell -ExecutionPolicy Bypass -File .\start-postgres.ps1` nesta pasta.
3. Inicie a API com `powershell -ExecutionPolicy Bypass -File .\run-postgres.ps1`.

Se for necessário executar manualmente, use:

```powershell
docker compose up -d
```

O volume `postgres_data` preserva contas, catálogo e agendamentos mesmo depois de reiniciar a API ou o container. Para encerrar o banco, use `docker compose down`; não use `-v` se quiser manter os dados.
