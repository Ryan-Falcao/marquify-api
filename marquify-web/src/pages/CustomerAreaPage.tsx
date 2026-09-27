import { AppointmentActions } from '../components/AppointmentActions';
import { CSSProperties, FormEvent, useEffect, useState } from 'react';
import { CalendarDays, Clock3, LogOut, Mail, Phone, Scissors, UserRound } from 'lucide-react';
import { useParams } from 'react-router-dom';
import { PublicBottomNav } from '../components/PublicBottomNav';
import { ApiError, customerAppointments, customerProfile, loginCustomer, publicBusiness } from '../services/api';
import type { Appointment, CustomerProfile, PublicBusiness } from '../types';
import { appointmentStateLabel, appointmentVisualState } from '../utils/appointment-state';
import { appearanceFromBusiness, loadAppearance } from './PersonalizationPage';

export function CustomerAreaPage({ view }: { view: 'appointments' | 'profile' }) {
  const { codigoPublico = '' } = useParams();
  const [token, setToken] = useState(() => localStorage.getItem('marquify-client-token') || '');
  const [business, setBusiness] = useState<PublicBusiness | null>(null);
  const [profile, setProfile] = useState<CustomerProfile | null>(null);
  const [items, setItems] = useState<Appointment[]>([]);
  const [loading, setLoading] = useState(Boolean(token));
  const [message, setMessage] = useState('');
  const appearance = business ? appearanceFromBusiness(business) : loadAppearance(codigoPublico);

  useEffect(() => { publicBusiness(codigoPublico).then(setBusiness).catch(() => setBusiness(null)); }, [codigoPublico]);
  useEffect(() => { if (!token) { setLoading(false); return; } setLoading(true); Promise.all([customerProfile(token), customerAppointments(token)]).then(([person, appointments]) => { setProfile(person); setItems(appointments); }).catch(() => { localStorage.removeItem('marquify-client-token'); setToken(''); setMessage('Sua sessão expirou. Entre novamente.'); }).finally(() => setLoading(false)); }, [token]);

  async function login(event: FormEvent<HTMLFormElement>) { event.preventDefault(); const form = new FormData(event.currentTarget); setMessage(''); setLoading(true); try { const result = await loginCustomer({ login: String(form.get('email')), senha: String(form.get('senha')) }); localStorage.setItem('marquify-client-token', result.token); setToken(result.token); } catch (error) { setMessage(error instanceof ApiError ? error.message : 'Não foi possível entrar.'); setLoading(false); } }
  const logout = () => { localStorage.removeItem('marquify-client-token'); setToken(''); setProfile(null); setItems([]); };

  return <main className="customer-portal" style={{ '--green': appearance.primaryColor } as CSSProperties}><header className="customer-portal-header"><div className="customer-brand"><span className="customer-brand-icon">{appearance.logoImage ? <img src={appearance.logoImage} alt="" /> : <Scissors size={18} />}</span><div><small>ÁREA DO CLIENTE</small><strong>{appearance.businessName || business?.nome || 'Estabelecimento'}</strong></div></div>{token && <button onClick={logout} aria-label="Sair da conta"><LogOut size={18} /></button>}</header><section className="customer-portal-content">{!token ? <div className="portal-login"><div className="portal-login-icon"><UserRound size={25} /></div><p className="portal-eyebrow">BEM-VINDO DE VOLTA</p><h1>Acesse sua conta</h1><p>Consulte seus horários e acompanhe seus agendamentos.</p><form onSubmit={login}><label>E-mail<input name="email" type="email" autoComplete="email" required placeholder="voce@email.com" /></label><label>Senha<input name="senha" type="password" autoComplete="current-password" minLength={6} required placeholder="Sua senha" /></label>{message && <p className="portal-error">{message}</p>}<button disabled={loading}>{loading ? 'Entrando…' : 'Entrar na minha conta'}</button></form><small>A conta é criada durante seu primeiro agendamento.</small></div> : loading ? <div className="portal-loading">Carregando sua conta…</div> : view === 'appointments' ? <AppointmentsView items={items} token={token} onUpdated={updated => setItems(current => current.map(item => item.id === updated.id ? updated : item))} message={message} /> : <ProfileView profile={profile} onLogout={logout} />}</section><PublicBottomNav code={codigoPublico} /></main>;
}

function AppointmentsView({ items, token, onUpdated, message }: { items: Appointment[]; token: string; onUpdated: (item: Appointment) => void; message: string }) {
  const sorted = [...items].sort((a, b) => `${b.data}${b.horaInicio}`.localeCompare(`${a.data}${a.horaInicio}`));
  return <div className="portal-view"><p className="portal-eyebrow">SUA AGENDA</p><h1>Meus agendamentos</h1><p className="portal-intro">Todos os seus horários em um só lugar.</p>{message && <p className="portal-error">{message}</p>}<div className="portal-appointments">{sorted.length ? sorted.map((item) => { const state = appointmentVisualState(item); return <article className={`portal-appointment portal-${state}`} key={item.id}><div className="portal-date"><strong>{new Date(`${item.data}T12:00:00`).getDate()}</strong><span>{new Date(`${item.data}T12:00:00`).toLocaleDateString('pt-BR', { month: 'short' }).replace('.', '')}</span></div><div className="portal-appointment-main"><span className={`portal-status status-${state}`}>{appointmentStateLabel[state]}</span><h2>{item.servico?.nome || 'Serviço'}</h2><p><Clock3 size={14} /> {item.horaInicio.slice(0, 5)}–{item.horaFim.slice(0, 5)} · {item.profissional?.nome}</p><small>{item.estabelecimento?.nome}</small></div><AppointmentActions item={item} token={token} onUpdated={onUpdated} /></article>; }) : <div className="portal-empty"><CalendarDays size={28} /><h2>Nenhum agendamento</h2><p>Quando você reservar um horário, ele aparecerá aqui.</p></div>}</div></div>;
}

function ProfileView({ profile, onLogout }: { profile: CustomerProfile | null; onLogout: () => void }) {
  const initials = profile?.nome.split(' ').slice(0, 2).map((part) => part[0]).join('').toUpperCase() || '?';
  return <div className="portal-view"><p className="portal-eyebrow">SUA CONTA</p><h1>Perfil</h1><div className="profile-card"><div className="profile-hero"><span>{initials}</span><div><h2>{profile?.nome}</h2><p>Cliente Marquify</p></div></div><div className="profile-detail"><Mail size={18} /><div><small>E-MAIL</small><strong>{profile?.email}</strong></div></div><div className="profile-detail"><Phone size={18} /><div><small>WHATSAPP</small><strong>{profile?.numero || 'Não informado'}</strong></div></div><button className="profile-logout" onClick={onLogout}><LogOut size={17} /> Sair da conta</button></div></div>;
}

