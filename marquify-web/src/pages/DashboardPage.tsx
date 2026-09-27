import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { QRCodeSVG } from 'qrcode.react';
import { ArrowLeft, ArrowRight, CalendarDays, Check, ChevronDown, Clock3, Copy, DollarSign, ExternalLink, Grid2X2, Link2, Menu, Palette, QrCode, Scissors, Settings2, Sparkles, Users, X, Zap } from 'lucide-react';
import { ApiError, bookingLink, dashboardData, logout } from '../services/api';
import type { Appointment, DashboardData } from '../types';
import { appointmentStateLabel, appointmentVisualState } from '../utils/appointment-state';
import '../dashboard-v0-isolated.css';

interface Props { onExit: () => void; onSessionExpired: () => void; }
type LoadState = 'loading' | 'ready' | 'error';

const navItems = [
  ['/', 'Visão geral', Grid2X2], ['/agenda', 'Agenda', CalendarDays], ['/catalogo', 'Catálogo', Scissors],
  ['/profissionais', 'Profissionais', Users], ['/jornada', 'Jornadas', Clock3], ['/personalizacao', 'Personalização', Palette]
] as const;

export function DashboardPage({ onExit, onSessionExpired }: Props) {
  const navigate = useNavigate();
  const [data, setData] = useState<DashboardData | null>(null);
  const [state, setState] = useState<LoadState>('loading');
  const [reload, setReload] = useState(0);
  const [publicLink, setPublicLink] = useState('');
  const [copied, setCopied] = useState(false);
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [qrOpen, setQrOpen] = useState(false);
  const [currentTime, setCurrentTime] = useState(() => new Date());

  useEffect(() => {
    let active = true;
    setState('loading');
    dashboardData().then((response) => { if (active) { setData(response); setState('ready'); } }).catch((error) => {
      if (!active) return;
      if (error instanceof ApiError && error.status === 401) onSessionExpired(); else setState('error');
    });
    return () => { active = false; };
  }, [reload, onSessionExpired]);

  useEffect(() => { bookingLink().then((response) => setPublicLink(response.urlAgendamento)).catch(() => setPublicLink('')); }, []);
  useEffect(() => { const timer = window.setInterval(() => setCurrentTime(new Date()), 60_000); return () => window.clearInterval(timer); }, []);
  useEffect(() => { const timer = window.setInterval(() => setReload((value) => value + 1), 60_000); return () => window.clearInterval(timer); }, []);

  const firstName = data?.nome.split(' ')[0] || 'proprietário';
  const initials = data?.nome.split(' ').slice(0, 2).map((part) => part[0]).join('').toUpperCase() || 'M';
  const today = useMemo(() => new Intl.DateTimeFormat('pt-BR', { weekday: 'long', day: '2-digit', month: 'long' }).format(new Date()).toUpperCase(), []);
  const appointments = data?.proximosAgendamentos ?? [];

  async function copyLink() {
    if (!publicLink) return;
    await navigator.clipboard.writeText(publicLink);
    setCopied(true);
    window.setTimeout(() => setCopied(false), 1800);
  }

  function exit() { logout(); onExit(); }

  return <div className="dashboard-shell">
    <aside className={`dashboard-sidebar ${sidebarOpen ? 'is-open' : ''}`}>
      <div className="sidebar-head"><div className="dash-brand"><span className="dash-brand-mark">marquify<span>.</span></span><span className="dash-brand-tag">ADMIN</span></div><button className="sidebar-close" onClick={() => setSidebarOpen(false)} aria-label="Fechar menu"><X /></button></div>
      <div className="workspace-switcher"><div className="workspace-avatar">{initials}</div><div><strong>Seu estabelecimento</strong><span>Área administrativa</span></div><ChevronDown size={15} /></div>
      <nav className="dashboard-nav" aria-label="Menu administrativo"><span className="nav-caption">GESTÃO</span>{navItems.map(([path, label, Icon]) => <button key={path} className={path === '/' ? 'active' : ''} onClick={() => navigate(path)}><Icon /><span>{label}</span>{path === '/agenda' && data?.agendamentosHoje ? <i className="nav-count">{data.agendamentosHoje}</i> : null}</button>)}<span className="nav-caption nav-caption-bottom">CONTA</span><button disabled title="Disponível em breve"><Settings2 /><span>Configurações</span></button><button className="nav-logout" onClick={exit}><ArrowLeft /><span>Sair</span></button></nav>
      <div className="sidebar-help"><Sparkles size={17} /><strong>Divulgue sua agenda</strong><span>Use seu link ou QR Code.</span><button onClick={() => setQrOpen(true)}>Exibir QR Code <ArrowRight /></button></div>
      <div className="sidebar-profile"><div className="profile-avatar">{initials}</div><div><strong>{data?.nome || 'Carregando…'}</strong><span>Proprietário</span></div></div>
    </aside>
    <div className="v0-dashboard-main"><header className="dashboard-header"><button className="mobile-menu" onClick={() => setSidebarOpen(true)} aria-label="Abrir menu"><Menu /></button><div className="header-breadcrumb"><span>Marquify</span><ArrowRight size={13} /><strong>Visão geral</strong></div><div className="header-actions">{publicLink && <a href={publicLink} target="_blank" rel="noreferrer" className="public-link"><ExternalLink size={14} /> Ver catálogo público</a>}<div className="header-avatar">{initials}</div></div></header>
      <main><div className="dashboard-page"><div className="page-heading"><div><span className="page-kicker">{today}</span><h1>{state === 'loading' ? 'Carregando sua operação…' : <>Olá, {firstName} <span>✦</span></>}</h1><p>Aqui está o resumo do seu negócio hoje.</p></div><button className="primary-button" onClick={() => navigate('/agenda')}><CalendarDays size={17} /> Ver agenda</button></div>
        {state === 'error' ? <section className="panel dashboard-error" role="alert"><h2>Não foi possível carregar a dashboard.</h2><p>Confirme se a API está funcionando e tente novamente.</p><button className="primary-button" onClick={() => setReload((value) => value + 1)}>Tentar novamente</button></section> : <>
          <div className="metric-grid metric-grid-revenue" aria-busy={state === 'loading'}><Metric icon={DollarSign} label="Total recebido" value={state === 'loading' ? '—' : (data?.totalFaturado ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })} detail="atendimentos já finalizados" tone="money" /><Metric icon={CalendarDays} label="Agendamentos de hoje" value={state === 'loading' ? '—' : String(data?.agendamentosHoje ?? 0)} detail="atendimentos confirmados" tone="coral" /><Metric icon={Clock3} label="Próximo atendimento" value={state === 'loading' ? '—' : appointments.find((item) => appointmentVisualState(item, currentTime) === 'upcoming')?.horaInicio.slice(0, 5) || 'Livre'} detail={appointments.find((item) => appointmentVisualState(item, currentTime) === 'upcoming')?.servico?.nome || 'sem atendimento próximo'} tone="navy" /><Metric icon={Scissors} label="Serviços ativos" value={state === 'loading' ? '—' : String(data?.servicosAtivos ?? 0)} detail="no catálogo" tone="sage" /><Metric icon={Users} label="Profissionais ativos" value={state === 'loading' ? '—' : String(data?.profissionaisAtivos ?? 0)} detail="na equipe" tone="sand" /></div>
          <div className="content-grid"><section className="panel timeline-panel"><div className="panel-heading"><div><h2>Agenda de hoje</h2><p>Status atualizado conforme o horário</p></div><button className="quiet-button" onClick={() => navigate('/agenda')}>Ver agenda completa <ArrowRight size={15} /></button></div>{state === 'loading' ? <p className="dashboard-empty">Carregando agenda…</p> : appointments.length ? <Timeline appointments={appointments.slice(0, 4)} now={currentTime} /> : <div className="dashboard-empty"><CalendarDays size={24} /><strong>Sua agenda está livre hoje.</strong><span>Compartilhe seu link para receber novos agendamentos.</span></div>}</section>
            <aside className="quick-column"><section className="panel quick-panel"><div className="panel-heading"><div><h2>Atalhos</h2><p>Ganhe tempo no dia a dia</p></div><Zap size={18} className="panel-spark" /></div><div className="quick-actions"><QuickAction icon={Scissors} label="Criar serviço" action={() => navigate('/catalogo')} /><QuickAction icon={Users} label="Adicionar profissional" action={() => navigate('/profissionais')} /><QuickAction icon={Clock3} label="Editar jornada" action={() => navigate('/jornada')} /><QuickAction icon={QrCode} label="Exibir QR Code" action={() => setQrOpen(true)} /></div></section>{publicLink && <section className="public-link-card"><div className="link-card-top"><div className="link-icon"><Link2 size={16} /></div><span>SEU LINK PÚBLICO</span></div><strong>{publicLink.replace(/^https?:\/\//, '')}</strong><p>Compartilhe e receba agendamentos.</p><button onClick={copyLink}>{copied ? <><Check size={15} /> Copiado</> : <><Copy size={15} /> Copiar link</>}</button></section>}</aside></div>
          <section className="panel appointments-panel"><div className="panel-heading"><div><h2>Agendamentos de hoje</h2><p>Acompanhe o andamento dos atendimentos.</p></div><button className="quiet-button" onClick={() => navigate('/agenda')}>Ver todos <ArrowRight size={15} /></button></div>{appointments.length ? <AppointmentRows appointments={appointments} now={currentTime} /> : <p className="dashboard-empty compact-empty">Nenhum atendimento confirmado para hoje.</p>}</section>
        </>}
      </div></main>
    </div>{sidebarOpen && <button className="sidebar-backdrop" onClick={() => setSidebarOpen(false)} aria-label="Fechar menu" />}{qrOpen && <div className="qr-overlay" onClick={() => setQrOpen(false)}><div className="qr-modal" onClick={(event) => event.stopPropagation()}><button className="modal-close" onClick={() => setQrOpen(false)} aria-label="Fechar"><X size={17} /></button>{publicLink ? <QRCodeSVG value={publicLink} size={140} level="M" includeMargin /> : <QrCode size={100} />}<h2>Seu QR Code</h2><p>Clientes podem apontar a câmera para agendar.</p></div></div>}
  </div>;
}

function Metric({ icon: Icon, label, value, detail, tone }: { icon: typeof CalendarDays; label: string; value: string; detail: string; tone: string }) { return <article className="metric"><div className={`metric-icon ${tone}`}><Icon size={18} /></div><span>{label}</span><strong>{value}</strong><small>{detail}</small></article>; }
function QuickAction({ icon: Icon, label, action }: { icon: typeof Scissors; label: string; action: () => void }) { return <button className="quick-action" onClick={action}><span><Icon size={16} /></span>{label}<ArrowRight size={14} /></button>; }
function Timeline({ appointments, now }: { appointments: Appointment[]; now: Date }) { return <div className="timeline-list"><div className="timeline-line" />{appointments.map((item, index) => { const visualState = appointmentVisualState(item, now); return <div className={`timeline-item appointment-${visualState}`} key={item.id}><span className="timeline-hour">{item.horaInicio.slice(0, 5)}</span><div className={`timeline-dot ${['coral', 'sage', 'sand', 'blue'][index % 4]}`} /><div className={`timeline-event ${['coral', 'sage', 'sand'][index % 3]}`}><div><strong>{item.servico?.nome || 'Serviço'}</strong><span>{item.profissional?.nome || 'Profissional'}</span></div><Status state={visualState} /></div></div>; })}</div>; }
function AppointmentRows({ appointments, now }: { appointments: Appointment[]; now: Date }) { return <div className="appointment-list compact">{appointments.map((item, index) => { const visualState = appointmentVisualState(item, now); return <div className={`appointment-row appointment-${visualState}`} key={item.id}><div className={`appointment-time ${['coral', 'sage', 'sand', 'blue'][index % 4]}`}>{item.horaInicio.slice(0, 5)}</div><div className="appointment-main"><strong>{item.servico?.nome || 'Serviço'}</strong><span>{item.data}</span></div><div className="appointment-professional"><div className="small-avatar">{item.profissional?.nome?.split(' ').slice(0, 2).map((part) => part[0]).join('') || '?'}</div><span>{item.profissional?.nome || 'Profissional'}</span></div><Status state={visualState} /><span /></div>; })}</div>; }
function Status({ state }: { state: ReturnType<typeof appointmentVisualState> }) { return <span className={`status status-${state}`}><span />{appointmentStateLabel[state]}</span>; }
