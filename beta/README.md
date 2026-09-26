# Ambiente local com PostgreSQL

## Teste local sem Docker (SQLite)

Para testar cadastro, login e dashboard sem instalar banco ou Docker, execute:

```powershell
powershell -ExecutionPolicy Bypass -File .\run-sqlite.ps1
```

O banco será criado em `data/marquify.db` e continua existindo após reiniciar a API. Ele é exclusivo para demonstração local; a produção deve usar PostgreSQL.

## Ambiente local com PostgreSQL

1. Instale e abra o Docker Desktop.
2. Copie `.env.example` para `.env` e altere `DB_PASSWORD`.
3. Execute `docker compose up -d` nesta pasta.
4. Inicie a API com o perfil PostgreSQL:

```powershell
$env:JAVA_HOME = 'C:\caminho\para\jdk-21'
$env:SPRING_PROFILES_ACTIVE = 'postgres'
$env:DB_PASSWORD = 'a-mesma-senha-do-arquivo-.env'
$env:JWT_SECRET = 'uma-chave-longa-para-desenvolvimento'
.\mvnw.cmd spring-boot:run
```

O volume `postgres_data` preserva contas, catálogo e agendamentos mesmo depois de reiniciar a API ou o container. Para encerrar o banco, use `docker compose down`; não use `-v` se quiser manter os dados.
