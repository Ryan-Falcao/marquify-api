# Marquify Web

Landing page comercial do Marquify. Não requer build nem Node. Execute `powershell -ExecutionPolicy Bypass -File .\serve-frontend.ps1` nesta pasta e abra `http://localhost:5173`.

O formulário cria o estabelecimento e a conta administrativa por `POST http://localhost:8080/auth/cadastro-comercial`; mantenha a API em execução. A origem `http://localhost:5173` já está liberada no CORS do backend.
