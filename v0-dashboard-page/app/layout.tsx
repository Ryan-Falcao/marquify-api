import { Analytics } from '@vercel/analytics/next'
import type { Metadata, Viewport } from 'next'
import './globals.css'

export const metadata: Metadata = {
  title: 'Marquify — Seus clientes agendam. Você cuida do negócio.',
  description: 'A agenda inteligente para barbearias, salões, clínicas e profissionais autônomos.',
  generator: 'Marquify',
}

export const viewport: Viewport = {
  colorScheme: 'light',
  themeColor: '#fbf8f3',
  userScalable: true,
}

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return <html lang="pt-BR"><body>{children}{process.env.NODE_ENV === 'production' && <Analytics />}</body></html>
}
