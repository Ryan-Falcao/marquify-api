import { FormEvent, useEffect, useState } from 'react';
import { MarquifyLanding } from '../components/MarquifyLanding';
import '../landing-v0.css';
import { ApiError, registerBusiness } from '../services/api';

interface Props { onAuthenticated: () => void; onLogin: () => void; }

export function LandingPage({ onAuthenticated, onLogin }: Props) {
  const [registerOpen, setRegisterOpen] = useState(false);
  const [message, setMessage] = useState('');

  useEffect(() => {
    const login = () => onLogin();
    const register = () => { setMessage(''); setRegisterOpen(true); };
    window.addEventListener('marquify-login', login);
    window.addEventListener('marquify-register', register);
    return () => {
      window.removeEventListener('marquify-login', login);
      window.removeEventListener('marquify-register', register);
    };
  }, [onLogin]);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    setMessage('Criando sua conta...');
    try {
      await registerBusiness({ nome: String(form.get('nome')), estabelecimento: String(form.get('estabelecimento')), email: String(form.get('email')), senha: String(form.get('senha')) });
      setRegisterOpen(false);
      onAuthenticated();
    } catch (error) {
      setMessage(error instanceof ApiError && error.status === 409 ? 'Este e-mail já possui uma conta.' : 'Não foi possível criar sua conta.');
    }
  }

  return <><MarquifyLanding />{registerOpen && <div className="v0-register-backdrop" role="dialog" aria-modal="true" aria-labelledby="register-title" onMouseDown={(event) => { if (event.target === event.currentTarget) setRegisterOpen(false); }}><form className="v0-register-card" onSubmit={submit}><button type="button" className="v0-register-close" aria-label="Fechar" onClick={() => setRegisterOpen(false)}>×</button><span className="kicker">COMECE AGORA</span><h2 id="register-title">Crie sua conta</h2><p>Configure seu estabelecimento e comece a receber agendamentos.</p><label>Seu nome<input name="nome" required placeholder="Como podemos te chamar?" /></label><label>Estabelecimento<input name="estabelecimento" required placeholder="Ex.: Barbearia do João" /></label><label>E-mail<input name="email" type="email" required placeholder="voce@email.com" /></label><label>Senha<input name="senha" type="password" minLength={8} required placeholder="Mínimo de 8 caracteres" /></label><button className="button button-coral">Criar minha conta</button>{message && <small className={message.startsWith('Criando') ? '' : 'register-error'}>{message}</small>}<button type="button" className="v0-login-switch" onClick={() => { setRegisterOpen(false); onLogin(); }}>Já tenho uma conta</button></form></div>}</>;
}
