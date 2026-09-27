import { FormEvent, useEffect, useRef, useState } from 'react';
import { ApiError, dashboardData, login, logout } from '../services/api';

interface Props {
  open: boolean;
  onClose: () => void;
  onSuccess: () => void;
  sessionExpired: boolean;
}

export function LoginDialog({ open, onClose, onSuccess, sessionExpired }: Props) {
  const [message, setMessage] = useState('');
  const dialog = useRef<HTMLDialogElement>(null);

  useEffect(() => {
    setMessage(sessionExpired ? 'Sua sessão expirou. Entre novamente.' : '');
  }, [sessionExpired]);

  useEffect(() => {
    if (open && dialog.current && !dialog.current.open) dialog.current.showModal();
  }, [open]);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    setMessage('Entrando...');
    try {
      const accessToken = await login({ login: String(form.get('login')), senha: String(form.get('senha')) });
      await dashboardData(accessToken);
      onSuccess();
    } catch (error) {
      logout();
      if (error instanceof ApiError && error.status === 429) { setMessage(error.message); return; }
      if (error instanceof ApiError && error.status === 401) setMessage('A API rejeitou a sessão recém-criada. Reinicie a API e tente novamente.');
      else if (error instanceof ApiError && error.status === 403) setMessage('Esta conta não tem acesso ao painel de proprietário.');
      else setMessage('Não foi possível entrar. Confira se a API está em execução.');
    }
  }

  if (!open) return null;
  return <dialog ref={dialog} id="login-dialog" onCancel={onClose}><button id="close-login" onClick={onClose}>×</button><p className="tag">ACESSO DO PROPRIETÁRIO</p><h3>Entre na sua conta</h3><form onSubmit={submit}><label>E-mail<input name="login" type="email" required /></label><label>Senha<input name="senha" type="password" required /></label><button className="button">Entrar <b>→</b></button><p className={message && message !== 'Entrando...' ? 'error' : ''}>{message}</p></form></dialog>;
}
