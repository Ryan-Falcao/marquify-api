# Etapa 3i — Base de integração com frontend

## Entrega

- CORS configurável por `CORS_ALLOWED_ORIGINS`.
- Rotas públicas para leitura do estabelecimento, catálogo ativo e profissionais ativos.
- DTOs públicos sem informações administrativas ou sensíveis.
- Contrato de consumo documentado em `contrato-frontend.md`.

## Validação

- `./mvnw.cmd -o -B verify`: 34 testes aprovados.
- O teste de integração cobre acesso público sem token, ocultação de itens inativos e preflight CORS do Vite.

## Próxima etapa

Criar disponibilidade de profissionais e a consulta de horários livres para alimentar o seletor de data e hora do frontend.
