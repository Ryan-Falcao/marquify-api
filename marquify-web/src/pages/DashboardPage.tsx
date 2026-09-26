import { useEffect, useState } from 'react';
import { dashboardData, logout } from '../services/api';
import type { Appointment } from '../types';

interface Props { onExit: () => void; }

export function DashboardPage({ onExit }: Props) {
  const [name, setName] = useState('');
  const [services, setServices] = useState(0);
  const [professionals, setProfessionals] = useState(0);
  const [appointments, setAppointments] = useState<Appointment[]>([]);

  useEffect(() => { dashboardData().then((data) => {
    setName(data.profile.nome.split(' ')[0]);
    setServices(data.services.filter((service) => service.ativo).length);
    setProfessionals(data.professionals.length);
    setAppointments(data.appointments);
  }).catch(() => setAppointments([])); }, []);

  const today = new Date().toISOString().slice(0, 10);
  const todayAppointments = appointments.filter((item) => item.data === today && item.status === 'AGENDADO');
  return <section className="dashboard"><aside><a className="logo" href="#inicio">marquify<span>.</span></a><p>VISÃO GERAL</p><button className="side-active">⌂ Início</button><button>▣ Agenda</button><button>✦ Serviços</button><button>♙ Profissionais</button><button onClick={() => { logout(); onExit(); }}>Sair</button></aside><div className="dashboard-main"><p className="tag">SEU NEGÓCIO</p><h2>Olá, {name || '!'}</h2><p className="dash-sub">Veja como está sua operação hoje.</p><div className="dash-cards"><article><span>Serviços ativos</span><strong>{services}</strong><small>no seu catálogo</small></article><article><span>Profissionais</span><strong>{professionals}</strong><small>na sua equipe</small></article><article><span>Agenda de hoje</span><strong>{todayAppointments.length}</strong><small>atendimentos</small></article></div><div className="dash-grid"><section><div className="dash-heading"><h3>Próximos agendamentos</h3><span>Hoje</span></div><div className="dash-list">{todayAppointments.length ? todayAppointments.map((item) => <article key={item.id}><b>{item.horaInicio.slice(0, 5)}</b><span>{item.servico.nome}<small>{item.profissional.nome}</small></span></article>) : <p className="muted">Nenhum atendimento confirmado para hoje.</p>}</div></section><section className="onboarding"><p className="tag">PRÓXIMOS PASSOS</p><h3>Deixe sua agenda pronta para vender.</h3><ol><li>Cadastre seu primeiro serviço.</li><li>Adicione quem atende com você.</li><li>Revise os horários disponíveis.</li></ol><button className="button" onClick={onExit}>Ver apresentação <b>→</b></button></section></div></div></section>;
}
