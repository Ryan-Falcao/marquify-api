import { CSSProperties, FormEvent, useEffect, useMemo, useRef, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { CalendarPlus, Check, Download, Image as ImageIcon } from 'lucide-react';
import { ApiError, bookAsCustomer, customerProfile, loginCustomer, publicAvailableTimes, publicBusiness, publicProfessionals, publicServices, registerCustomer, servicePhotoUrl } from '../services/api';
import type { Appointment, CustomerProfile, PublicBusiness, PublicProfessional, Service } from '../types';
import { appearanceFromBusiness, loadAppearance } from './PersonalizationPage';
import { PublicBottomNav } from '../components/PublicBottomNav';
import { downloadCalendarFile, googleCalendarUrl } from '../utils/calendar-event';

const dateLabel = new Intl.DateTimeFormat('pt-BR', { weekday: 'short', day: 'numeric', month: 'short' });
const isoDate = (date: Date) => date.toISOString().slice(0, 10);
const nextDays = () => Array.from({ length: 7 }, (_, index) => { const date = new Date(); date.setHours(12, 0, 0, 0); date.setDate(date.getDate() + index); return date; });
const money = (value: number) => value.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });

export function PublicBookingPage() {
  const { codigoPublico = '' } = useParams();
  const navigate = useNavigate();
  const [business, setBusiness] = useState<PublicBusiness | null>(null);
  const [appearance, setAppearance] = useState(() => loadAppearance(codigoPublico));
  const [services, setServices] = useState<Service[]>([]);
  const [professionals, setProfessionals] = useState<PublicProfessional[]>([]);
  const [selectedService, setSelectedService] = useState<Service | null>(null);
  const [selectedProfessional, setSelectedProfessional] = useState<PublicProfessional | null>(null);
  const [selectedDate, setSelectedDate] = useState(() => isoDate(nextDays()[0]));
  const [availableTimes, setAvailableTimes] = useState<string[]>([]);
  const [bookedTimes, setBookedTimes] = useState<string[]>([]);
  const [blockedTimes, setBlockedTimes] = useState<string[]>([]);
  const [selectedTime, setSelectedTime] = useState<string | null>(null);
  const [status, setStatus] = useState<'loading' | 'ready' | 'not-found'>('loading');
  const [timesState, setTimesState] = useState<'idle' | 'loading' | 'ready' | 'error'>('idle');
  const [bookingState, setBookingState] = useState<'idle' | 'saving' | 'success' | 'error'>('idle');
  const [bookingMessage, setBookingMessage] = useState('');
  const [confirmedAppointment, setConfirmedAppointment] = useState<Appointment | null>(null);
  const datePickerRef = useRef<HTMLDivElement>(null);
  const dates = useMemo(nextDays, []);
  const eligibleProfessionals = selectedService ? professionals.filter((person) => selectedService.profissionais.some((assigned) => assigned.id === person.id)) : [];

  useEffect(() => {
    Promise.all([publicBusiness(codigoPublico), publicServices(codigoPublico), publicProfessionals(codigoPublico)])
      .then(([establishment, catalog, team]) => { setBusiness(establishment); setAppearance(appearanceFromBusiness(establishment)); setServices(catalog.filter((service) => service.ativo)); setProfessionals(team); setStatus('ready'); if (establishment.slugPublico !== codigoPublico) navigate(`/agendar/${establishment.slugPublico}`, { replace: true }); })
      .catch(() => setStatus('not-found'));
  }, [codigoPublico, navigate]);
  useEffect(() => { setSelectedProfessional(null); setSelectedTime(null); setAvailableTimes([]); setBookedTimes([]); setBlockedTimes([]); setTimesState('idle'); }, [selectedService]);
  useEffect(() => {
    if (!selectedService || !selectedProfessional) return;
    let active = true; setTimesState('loading'); setSelectedTime(null);
    publicAvailableTimes(codigoPublico, selectedProfessional.id, selectedService.id, selectedDate)
      .then((result) => { if (active) { setAvailableTimes(result.horarios); setBookedTimes(result.horariosOcupados || []); setBlockedTimes(result.horariosBloqueados || []); setTimesState('ready'); } })
      .catch(() => { if (active) { setAvailableTimes([]); setBookedTimes([]); setBlockedTimes([]); setTimesState('error'); } });
    return () => { active = false; };
  }, [codigoPublico, selectedDate, selectedProfessional, selectedService]);

  async function finishBooking(accessToken: string) {
    if (!selectedService?.vendedorId || !selectedProfessional || !selectedTime) return;
    setBookingState('saving'); setBookingMessage('');
    try {
      localStorage.setItem('marquify-client-token', accessToken);
      const appointment = await bookAsCustomer(accessToken, { data: selectedDate, horaInicio: selectedTime, vendedorId: selectedService.vendedorId, servicoId: selectedService.id, profissionalId: selectedProfessional.id });
      setConfirmedAppointment(appointment);
      setBookingState('success');
    } catch (error) { setBookingState('error'); setBookingMessage(error instanceof ApiError ? error.message : 'Não foi possível confirmar o agendamento.'); }
  }

  if (status === 'loading') return <main className="public-booking public-feedback public-feedback-loading" role="status"><span className="feedback-spinner" aria-hidden="true" /><p>Carregando agenda…</p></main>;
  if (status === 'not-found') return <main className="public-booking public-feedback public-feedback-error" role="alert"><h1>Estabelecimento não encontrado</h1><p>Confira o link ou peça um novo QR code ao estabelecimento.</p></main>;
  if (bookingState === 'success' && confirmedAppointment) return <BookingConfirmation appointment={confirmedAppointment} business={business} codigoPublico={codigoPublico} />;

  const heroBusinessName = appearance.businessName || business?.nome || '';
  const heroNameSize = heroBusinessName.length > 28 ? 'business-name--very-long' : heroBusinessName.length > 17 ? 'business-name--long' : '';
  return <main className={`public-booking theme-font-${appearance.fontStyle} theme-corners-${appearance.cornerStyle} theme-services-${appearance.serviceLayout}`} style={{ '--green': appearance.primaryColor, '--shop-bg': appearance.backgroundColor, '--shop-surface': appearance.surfaceColor, '--shop-text': appearance.textColor, '--hero-text': appearance.heroTextColor } as CSSProperties}>
    <section className={`booking-hero hero-align-${appearance.heroAlignment} hero-size-${appearance.heroSize}`} style={{ color: appearance.heroTextColor, ...(appearance.coverImage ? { backgroundImage: `linear-gradient(rgba(0,0,0,${appearance.overlayOpacity / 100}),rgba(0,0,0,${appearance.overlayOpacity / 100})),url(${appearance.coverImage})`, backgroundSize: 'cover', backgroundPosition: `${appearance.coverPositionX}% ${appearance.coverPositionY}%` } : undefined) }}><a className="logo" href="/">{appearance.logoImage ? <img src={appearance.logoImage} alt={appearance.businessName} /> : <>marquify<span>.</span></>}</a><h1 style={{ color: appearance.businessNameColor }}><span>Agende com</span><i className={heroNameSize}>{heroBusinessName}</i></h1><p style={{ color: appearance.descriptionColor }}>{appearance.description || 'Escolha o serviço, quem vai atender e o horário que funciona para você.'}</p></section>
    <section className="booking-content">
      <div className="booking-progress"><span className="active">1 Serviço</span><span className={selectedService ? 'active' : ''}>2 Profissional</span><span className={selectedProfessional ? 'active' : ''}>3 Horário</span><span className={selectedTime ? 'active' : ''}>4 Conta</span></div>
      <div className="booking-step"><span>1</span><div><p className="tag">ESCOLHA SEU SERVIÇO</p><h2 style={{ color: appearance.catalogTitleColor }}>{appearance.catalogTitle}</h2></div></div>
      <div className="public-services">{services.length ? services.map((service) => <button type="button" className={`${selectedService?.id === service.id ? 'selected' : ''} ${!appearance.showServiceImages ? 'without-image' : ''}`} key={service.id} onClick={() => setSelectedService(service)}>{appearance.showServiceImages && <span className="public-service-media">{service.fotoUrl ? <img src={servicePhotoUrl(service)} alt={`Foto de ${service.nome}`} style={{ objectPosition: `${service.fotoPosicaoX ?? 50}% ${service.fotoPosicaoY ?? 50}%` }} /> : <ImageIcon aria-hidden="true" size={27} strokeWidth={1.5} />}{selectedService?.id === service.id && <i>✓</i>}</span>}<span className="public-service-copy"><b style={{ color: appearance.serviceNameColor }}>{service.nome}</b>{appearance.showServiceDescriptions && <small style={{ color: appearance.serviceDescriptionColor }}>{service.descricao || 'Conheça este atendimento.'}</small>}<span>{appearance.showServiceDuration && <em style={{ color: appearance.serviceDurationColor }}>◷ {service.tempo.slice(0, 5)}</em>}<strong style={{ color: appearance.textColor }}>{money(service.preco)}</strong></span></span></button>) : <p>Nenhum serviço disponível.</p>}</div>
      {selectedService && <><div className="booking-step"><span>2</span><div><p className="tag">EQUIPE</p><h2>Quem vai atender você?</h2></div></div><div className="public-team">{eligibleProfessionals.map((professional) => <button type="button" key={professional.id} className={selectedProfessional?.id === professional.id ? 'selected' : ''} onClick={() => setSelectedProfessional(professional)}><span>{professional.nome.split(' ').slice(0, 2).map((part) => part[0]).join('').toUpperCase()}</span>{professional.nome}<i>{selectedProfessional?.id === professional.id ? '✓' : '→'}</i></button>)}</div></>}
      {selectedProfessional && <><div className="booking-step booking-step-time"><span>3</span><div><p className="tag">DATA E HORÁRIO</p><h2>Quando você quer vir?</h2></div></div><div className="date-carousel"><button type="button" className="date-carousel-arrow previous" aria-label="Ver dias anteriores" onClick={() => datePickerRef.current?.scrollBy({ left: -170, behavior: 'smooth' })}>‹</button><div className="date-picker" ref={datePickerRef} onWheel={(event) => { if (Math.abs(event.deltaY) > Math.abs(event.deltaX) && datePickerRef.current) { event.preventDefault(); datePickerRef.current.scrollLeft += event.deltaY; } }}>{dates.map((date) => <button type="button" key={isoDate(date)} className={selectedDate === isoDate(date) ? 'selected' : ''} onClick={(event) => { setSelectedDate(isoDate(date)); event.currentTarget.scrollIntoView({ behavior: 'smooth', inline: 'center', block: 'nearest' }); }}><small>{dateLabel.format(date).split(',')[0]}</small><b>{date.getDate()}</b></button>)}</div><button type="button" className="date-carousel-arrow next" aria-label="Ver próximos dias" onClick={() => datePickerRef.current?.scrollBy({ left: 170, behavior: 'smooth' })}>›</button></div><div className="date-scroll-hint">Arraste para ver os próximos dias →</div><div className="time-picker">{timesState === 'loading' ? <p>Consultando horários…</p> : timesState === 'error' ? <p>Não foi possível consultar os horários.</p> : availableTimes.length || bookedTimes.length || blockedTimes.length ? [...availableTimes.map((time) => ({ time, state: 'free' })), ...bookedTimes.map((time) => ({ time, state: 'reserved' })), ...blockedTimes.map((time) => ({ time, state: 'blocked' }))].sort((a, b) => a.time.localeCompare(b.time)).map(({ time, state }) => <button type="button" disabled={state !== 'free'} className={`${selectedTime === time ? 'selected' : ''} ${state}`} key={`${time}-${state}`} onClick={() => state === 'free' && setSelectedTime(time)}><b>{time.slice(0, 5)}</b>{state === 'reserved' && <small>Reservado</small>}{state === 'blocked' && <small>Indisponível</small>}</button>) : <p>Nenhum horário disponível nesta data.</p>}</div></>}
      {selectedTime && <section className="booking-summary"><div className="booking-summary-heading"><div><p className="tag">REVISE SEU AGENDAMENTO</p><h3>Está tudo certo?</h3><p>Confira os dados antes de confirmar.</p></div><span>4 de 4</span></div><div className="booking-summary-grid"><div><small>Serviço</small><strong>{selectedService?.nome}</strong><span>{selectedService?.tempo.slice(0, 5)} de duração</span></div><div><small>Profissional</small><strong>{selectedProfessional?.nome}</strong><span>{business?.nome}</span></div><div><small>Data</small><strong>{new Date(`${selectedDate}T12:00:00`).toLocaleDateString('pt-BR', { weekday: 'long', day: '2-digit', month: 'long' })}</strong><span>{selectedTime.slice(0, 5)}</span></div><div className="summary-price"><small>Valor</small><strong>{selectedService && money(selectedService.preco)}</strong><span>Pagamento no estabelecimento</span></div></div><CustomerAccess onAuthenticated={finishBooking} saving={bookingState === 'saving'} error={bookingMessage} /></section>}
    </section><PublicBottomNav code={codigoPublico} />
  </main>;
}

function BookingConfirmation({ appointment, business, codigoPublico }: { appointment: Appointment; business: PublicBusiness | null; codigoPublico: string }) {
  const date = new Date(`${appointment.data}T12:00:00`).toLocaleDateString('pt-BR', { weekday: 'long', day: '2-digit', month: 'long' });
  return <main className="public-booking booking-confirmation" style={{ '--green': business?.corPrimaria || '#245945' } as CSSProperties}>
    <section className="confirmation-card">
      <div className="confirmation-check"><Check size={25} strokeWidth={3} /></div>
      <p className="tag">AGENDAMENTO CONFIRMADO</p>
      <h1>Seu horário está reservado.</h1>
      <p className="confirmation-intro">Pronto! Guardamos todos os detalhes para você.</p>
      <div className="confirmation-appointment">
        <span>{date}</span>
        <strong>{appointment.horaInicio.slice(0, 5)} · {appointment.servico?.nome || 'Agendamento'}</strong>
        <small>{appointment.profissional?.nome && `${appointment.profissional.nome} · `}{business?.nome || appointment.estabelecimento?.nome}</small>
      </div>
      <div className="confirmation-calendar">
        <div><CalendarPlus size={19} /><span><b>Não esqueça do horário</b><small>Adicione agora ao seu calendário.</small></span></div>
        <a className="confirmation-google" href={googleCalendarUrl(appointment)} target="_blank" rel="noreferrer">Adicionar ao Google Agenda <span aria-hidden="true">↗</span></a>
        <button type="button" onClick={() => downloadCalendarFile(appointment)}><Download size={15} /> Baixar para Apple Calendar ou Outlook</button>
      </div>
      <Link className="confirmation-account" to={`/agendar/${codigoPublico}/minha-conta`}>Ver meus agendamentos</Link>
    </section>
    <PublicBottomNav code={codigoPublico} />
  </main>;
}

function CustomerAccess({ onAuthenticated, saving, error }: { onAuthenticated: (token: string) => Promise<void>; saving: boolean; error: string }) {
  const [mode, setMode] = useState<'register' | 'login'>('register');
  const [message, setMessage] = useState('');
  const [savedSession, setSavedSession] = useState<{ token: string; profile: CustomerProfile } | null>(null);
  const [checkingSession, setCheckingSession] = useState(true);

  useEffect(() => {
    const savedToken = localStorage.getItem('marquify-client-token');
    if (!savedToken) { setCheckingSession(false); return; }
    let active = true;
    customerProfile(savedToken)
      .then((profile) => { if (active) setSavedSession({ token: savedToken, profile }); })
      .catch(() => { localStorage.removeItem('marquify-client-token'); })
      .finally(() => { if (active) setCheckingSession(false); });
    return () => { active = false; };
  }, []);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); const form = new FormData(event.currentTarget); setMessage('');
    try {
      const credentials = { login: String(form.get('email')), senha: String(form.get('senha')) };
      const result = mode === 'register' ? await registerCustomer({ nome: String(form.get('nome')), numero: String(form.get('numero')), email: credentials.login, senha: credentials.senha }) : await loginCustomer(credentials);
      localStorage.setItem('marquify-client-token', result.token);
      await onAuthenticated(result.token);
    } catch (cause) { setMessage(cause instanceof ApiError ? cause.message : 'Não foi possível acessar sua conta.'); }
  }
  if (checkingSession) return <div className="customer-access session-check"><span className="session-spinner" /><p>Verificando sua conta…</p></div>;
  if (savedSession) return <div className="customer-access authenticated-customer"><div className="authenticated-customer-info"><span>{savedSession.profile.nome.split(' ').slice(0, 2).map((part) => part[0]).join('').toUpperCase()}</span><div><small>AGENDANDO COMO</small><strong>{savedSession.profile.nome}</strong><p>{savedSession.profile.email}</p></div></div>{error && <p className="booking-error">{error}</p>}<button className="button confirm-booking" disabled={saving} onClick={() => void onAuthenticated(savedSession.token)}>{saving ? 'Confirmando…' : 'Confirmar agendamento →'}</button><button type="button" className="change-customer" onClick={() => { localStorage.removeItem('marquify-client-token'); setSavedSession(null); }}>Usar outra conta</button></div>;
  return <div className="customer-access"><div className="account-tabs"><button type="button" className={mode === 'register' ? 'active' : ''} onClick={() => { setMode('register'); setMessage(''); }}>Criar minha conta</button><button type="button" className={mode === 'login' ? 'active' : ''} onClick={() => { setMode('login'); setMessage(''); }}>Já tenho conta</button></div><h3>{mode === 'register' ? 'Crie sua conta para confirmar' : 'Entre para confirmar'}</h3><p>{mode === 'register' ? 'Esta conta é permanente e permite consultar e cancelar seus agendamentos.' : 'Use o mesmo e-mail e senha dos seus agendamentos anteriores.'}</p><form className="customer-form" onSubmit={submit}>{mode === 'register' && <><label>Seu nome<input name="nome" required autoComplete="name" /></label><label>WhatsApp<input name="numero" required type="tel" autoComplete="tel" /></label></>}<label>E-mail<input name="email" required type="email" autoComplete="email" /></label><label>Senha<input name="senha" required type="password" minLength={6} autoComplete={mode === 'register' ? 'new-password' : 'current-password'} /></label>{(message || error) && <p className="booking-error">{message || error}</p>}<button className="button" disabled={saving}>{saving ? 'Confirmando…' : 'Confirmar agendamento →'}</button></form></div>;
}
