import { FormEvent, useState } from 'react';
import { ApiError, login } from '../services/api';

interface Props {
  open: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

export function LoginDialog({ open, onClose, onSuccess }: Props) {
  const [message, setMessage] = useState('');

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    setMessage('Entrando...');
    try {
      await login({ login: String(form.get('login')), senha: String(form.get('senha')) });
      onSuccess();
    } catch (error) {
      setMessage(error instanceof ApiError && error.status === 401 ? 'E-mail ou senha inválidos.' : 'Não foi possível entrar.');
    }
  }

  if (!open) return null;
  return <dialog open id="login-dialog"><button id="close-login" onClick={onClose}>×</button><p className="tag">ACESSO DO PROPRIETÁRIO</p><h3>Entre na sua conta</h3><form onSubmit={submit}><label>E-mail<input name="login" type="email" required /></label><label>Senha<input name="senha" type="password" required /></label><button className="button">Entrar <b>→</b></button><p className={message.includes('inválidos') ? 'error' : ''}>{message}</p></form></dialog>;
}
