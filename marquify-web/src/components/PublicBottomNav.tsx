import { CalendarDays, Scissors, UserRound } from 'lucide-react';
import { useLocation, useNavigate } from 'react-router-dom';

export function PublicBottomNav({ code }: { code: string }) {
  const navigate = useNavigate();
  const path = useLocation().pathname;
  const items = [
    { label: 'Catálogo', icon: Scissors, path: `/agendar/${code}` },
    { label: 'Meus agendamentos', icon: CalendarDays, path: `/agendar/${code}/minha-conta` },
    { label: 'Perfil', icon: UserRound, path: `/agendar/${code}/perfil` }
  ];
  return <nav className="public-bottom-nav" aria-label="Navegação do cliente">{items.map(({ label, icon: Icon, path: target }) => <button type="button" className={path === target ? 'active' : ''} onClick={() => navigate(target)} key={target}><Icon size={20} /><span>{label}</span></button>)}</nav>;
}
