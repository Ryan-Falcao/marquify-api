import { useState } from 'react';
import { LoginDialog } from './components/LoginDialog';
import { DashboardPage } from './pages/DashboardPage';
import { LandingPage } from './pages/LandingPage';
import { hasSession } from './services/api';

export default function App() {
  const [dashboard, setDashboard] = useState(hasSession());
  const [loginOpen, setLoginOpen] = useState(false);
  const authenticated = () => { setLoginOpen(false); setDashboard(true); };
  return <>{dashboard ? <DashboardPage onExit={() => setDashboard(false)} /> : <LandingPage onLogin={() => setLoginOpen(true)} onAuthenticated={authenticated} />}<LoginDialog open={loginOpen} onClose={() => setLoginOpen(false)} onSuccess={authenticated} /></>;
}
