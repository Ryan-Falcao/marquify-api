# Marquify Web

Frontend React + TypeScript com Vite.

## Desenvolvimento

Instale o Node.js LTS, que inclui npm, e execute:

```powershell
npm install
npm run dev
```

Abra `http://localhost:5173`. Para validar a aplicação de produção, execute `npm run typecheck` e `npm run build`; `npm run preview` serve o resultado do build localmente.

## API

Por padrão, o frontend usa `http://localhost:8080`. Para apontar para outro ambiente, copie `.env.example` para `.env.local` e defina `VITE_API_URL`. As páginas ficam em `src/pages`, componentes reutilizáveis em `src/components` e a comunicação com a API em `src/services/api.ts`.
