import { useCallback, useState } from 'react';
import { Navigate, Route, Routes } from 'react-router-dom';
import { AdminPage } from './pages/AdminPage';
import { LoginDialog } from './components/LoginDialog';
import { DashboardPage } from './pages/DashboardPage';
import { LandingPage } from './pages/LandingPage';
import { PublicBookingPage } from './pages/PublicBookingPage';
import { MyAppointmentsPage } from './pages/MyAppointmentsPage';
import { PersonalizationPage } from './pages/PersonalizationPage';
import { CustomerAreaPage } from './pages/CustomerAreaPage';
import { logout } from './services/api';

export default function App() {
  // Um token salvo pode ter sido revogado ou assinado por uma instância anterior da API.
  // A dashboard só é aberta após uma autenticação confirmada nesta sessão do navegador.
  const [dashboard, setDashboard] = useState(false);
  const [loginOpen, setLoginOpen] = useState(false);
  const [sessionExpired, setSessionExpired] = useState(false);
  const authenticated = useCallback(() => { setSessionExpired(false); setLoginOpen(false); setDashboard(true); }, []);
  const expireSession = useCallback(() => { logout(); setDashboard(false); setSessionExpired(true); setLoginOpen(true); }, []);
  const closeLogin = useCallback(() => { setSessionExpired(false); setLoginOpen(false); }, []);
  const openLogin = useCallback(() => { setSessionExpired(false); setLoginOpen(true); }, []);

  return <Routes>
    <Route path="/agendar/:codigoPublico" element={<PublicBookingPage />} />
    <Route path="/agendar/:codigoPublico/minha-conta" element={<CustomerAreaPage view="appointments" />} />
    <Route path="/agendar/:codigoPublico/perfil" element={<CustomerAreaPage view="profile" />} />
    <Route path="/minha-conta" element={<MyAppointmentsPage />} />
    <Route path="*" element={dashboard ? <Routes>
      <Route path="/" element={<DashboardPage onExit={() => setDashboard(false)} onSessionExpired={expireSession} />} />
      <Route path="/agenda" element={<AdminPage key="agenda" onExit={() => setDashboard(false)} onSessionExpired={expireSession} />} />
      <Route path="/catalogo" element={<AdminPage key="catalogo" onExit={() => setDashboard(false)} onSessionExpired={expireSession} />} />
      <Route path="/servicos" element={<AdminPage key="servicos" onExit={() => setDashboard(false)} onSessionExpired={expireSession} />} />
      <Route path="/profissionais" element={<AdminPage key="profissionais" onExit={() => setDashboard(false)} onSessionExpired={expireSession} />} />
      <Route path="/jornada" element={<AdminPage key="jornada" onExit={() => setDashboard(false)} onSessionExpired={expireSession} />} />
      <Route path="/personalizacao" element={<PersonalizationPage onExit={() => setDashboard(false)} onSessionExpired={expireSession} />} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes> : <><LandingPage onLogin={openLogin} onAuthenticated={authenticated} /><LoginDialog open={loginOpen} onClose={closeLogin} onSuccess={authenticated} sessionExpired={sessionExpired} /></>} />
  </Routes>;
}
