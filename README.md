# Marquify API 📅

API REST para gerenciamento de **agendamentos**, desenvolvida em **Java** com **Spring Boot**, autenticação segura via **JWT (Spring Security)** e persistência de dados em **MySQL**.

## 📖 Sobre o projeto

A Marquify API foi criada para gerenciar o processo de agendamentos de forma simples e segura, oferecendo autenticação de usuários e operações para criar, consultar, atualizar e cancelar agendamentos.

## 🛠️ Tecnologias utilizadas

- **Java**
- **Spring Boot**
- **Spring Security** (autenticação e autorização)
- **JWT** (JSON Web Token) para autenticação stateless
- **MySQL** (banco de dados relacional)
- **Maven** (gerenciamento de dependências)

## ⚙️ Funcionalidades

- Cadastro e autenticação de usuários
- Login com geração de token JWT
- Criação, listagem, atualização e cancelamento de agendamentos
- Proteção de rotas via token de autenticação

> Ajuste esta lista conforme as funcionalidades reais implementadas no seu projeto.

## 🚀 Como executar o projeto

### Pré-requisitos

- Java 17+ instalado
- Maven instalado
- MySQL rodando localmente ou em servidor

### Passos

```bash
# Clone o repositório
git clone https://github.com/seu-usuario/marquify-api.git

# Acesse a pasta do projeto
cd marquify-api
```

### Configuração do banco de dados

Crie um banco no MySQL e configure as credenciais no arquivo `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/marquify
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Configuração JWT
jwt.secret=sua_chave_secreta
jwt.expiration=86400000
```

> 💡 Dica: mantenha um arquivo `application.properties.example` no repositório (sem dados sensíveis) e adicione o `application.properties` real ao `.gitignore`, para não expor credenciais.

### Executando a aplicação

```bash
./mvnw spring-boot:run
```

A API estará disponível em `http://localhost:8080`.

## 📡 Endpoints principais

### Autenticação

| Método | Rota            | Descrição                       |
|--------|-----------------|----------------------------------|
| POST   | `/auth/register` | Cadastra um novo usuário         |
| POST   | `/auth/login`     | Autentica o usuário e retorna o token JWT |

### Agendamentos

| Método | Rota                  | Descrição                                |
|--------|-----------------------|-------------------------------------------|
| GET    | `/agendamentos`       | Lista todos os agendamentos               |
| GET    | `/agendamentos/{id}`  | Busca um agendamento específico           |
| POST   | `/agendamentos`       | Cria um novo agendamento                  |
| PUT    | `/agendamentos/{id}`  | Atualiza um agendamento existente         |
| DELETE | `/agendamentos/{id}`  | Cancela/remove um agendamento             |

> Ajuste os nomes das rotas e campos conforme a implementação real dos seus controllers.

### Autenticação nas requisições

Após o login, envie o token JWT no header das requisições protegidas:

```
Authorization: Bearer SEU_TOKEN_AQUI
```

## 🧪 Testando a API

Você pode testar os endpoints usando ferramentas como [Postman](https://www.postman.com/) ou [Insomnia](https://insomnia.rest/).

## 🤝 Contribuindo

Contribuições são bem-vindas! Sinta-se à vontade para abrir issues ou pull requests com melhorias, correções ou novas funcionalidades.

## 📄 Licença

Este projeto está sob a licença MIT.
