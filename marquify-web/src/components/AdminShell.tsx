import { ReactNode, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, ArrowRight, CalendarDays, ChevronDown, Clock3, Grid2X2, Menu, Palette, Scissors, Settings2, Sparkles, Users, X } from 'lucide-react';
import { logout } from '../services/api';
import '../dashboard-v0-isolated.css';

interface Props { title: string; active: string; userName?: string; onExit: () => void; children: ReactNode; }
const navItems = [['/', 'Visão geral', Grid2X2], ['/agenda', 'Agenda', CalendarDays], ['/catalogo', 'Catálogo', Scissors], ['/profissionais', 'Profissionais', Users], ['/jornada', 'Jornadas', Clock3], ['/personalizacao', 'Personalização', Palette]] as const;

export function AdminShell({ title, active, userName, onExit, children }: Props) {
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);
  const initials = userName?.split(' ').slice(0, 2).map((part) => part[0]).join('').toUpperCase() || 'M';
  const go = (path: string) => { setOpen(false); navigate(path); };
  const exit = () => { logout(); onExit(); };

  return <div className="dashboard-shell"><aside className={`dashboard-sidebar ${open ? 'is-open' : ''}`}><div className="sidebar-head"><div className="dash-brand"><span className="dash-brand-mark">marquify<span>.</span></span><span className="dash-brand-tag">ADMIN</span></div><button className="sidebar-close" onClick={() => setOpen(false)} aria-label="Fechar menu"><X /></button></div><div className="workspace-switcher"><div className="workspace-avatar">{initials}</div><div><strong>Seu estabelecimento</strong><span>Área administrativa</span></div><ChevronDown size={15} /></div><nav className="dashboard-nav" aria-label="Navegação administrativa"><span className="nav-caption">GESTÃO</span>{navItems.map(([path, label, Icon]) => <button key={path} aria-current={active === path ? 'page' : undefined} className={active === path ? 'active' : ''} onClick={() => go(path)}><Icon /><span>{label}</span></button>)}<span className="nav-caption nav-caption-bottom">CONTA</span><button aria-current={active === '/configuracoes' ? 'page' : undefined} className={active === '/configuracoes' ? 'active' : ''} onClick={() => go('/configuracoes')}><Settings2 /><span>Configurações</span></button><button className="nav-logout" onClick={exit}><ArrowLeft /><span>Sair</span></button></nav><div className="sidebar-help"><Sparkles size={17} /><strong>Organize sua operação</strong><span>Mantenha serviços e horários atualizados.</span><button onClick={() => go('/catalogo')}>Revisar catálogo <ArrowRight /></button></div><div className="sidebar-profile"><div className="profile-avatar">{initials}</div><div><strong>{userName || 'Marquify'}</strong><span>Proprietário</span></div></div></aside><div className="v0-dashboard-main"><header className="dashboard-header"><button className="mobile-menu" onClick={() => setOpen(true)} aria-label="Abrir menu" aria-expanded={open}><Menu /></button><div className="header-breadcrumb"><span>Marquify</span><ArrowRight size={13} /><strong>{title}</strong></div><div className="header-actions"><div className="header-avatar">{initials}</div></div></header>{children}</div>{open && <button className="sidebar-backdrop" onClick={() => setOpen(false)} aria-label="Fechar menu" />}</div>;
}
