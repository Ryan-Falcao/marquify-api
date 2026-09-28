import { useEffect, useState } from 'react';
import type { Appointment } from '../types';
import { cancelCustomerAppointment, rescheduleAppointment, rescheduleTimes } from '../services/api';
import { appointmentVisualState } from '../utils/appointment-state';
import { downloadCalendarFile, googleCalendarUrl } from '../utils/calendar-event';
import './appointment-actions.css';

export function AppointmentActions({ item, token, onUpdated }: { item: Appointment; token: string; onUpdated: (item: Appointment) => void }) {
  const [mode, setMode] = useState<'cancel' | 'reschedule' | null>(null);
  const [date, setDate] = useState(item.data);
  const [time, setTime] = useState('');
  const [times, setTimes] = useState<string[]>([]);
  const [loading, setLoading] = useState(false);
  const [busy, setBusy] = useState(false);
  const [message, setMessage] = useState('');
  const [success, setSuccess] = useState('');
  const [calendarOpen, setCalendarOpen] = useState(false);
  const [retry, setRetry] = useState(0);
  useEffect(() => {
    if (mode !== 'reschedule' || !date) return;
    let active = true;
    setLoading(true); setTime(''); setTimes([]); setMessage('');
    rescheduleTimes(token, item.id, date).then(value => { if (active) setTimes(value); })
      .catch(error => { if (active) setMessage(error.message); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [mode, date, item.id, token, retry]);
  async function confirm() {
    if (busy) return;
    setBusy(true); setMessage('');
    try {
      const updated = mode === 'cancel' ? await cancelCustomerAppointment(token, item.id) : await rescheduleAppointment(token, item.id, date, time);
      onUpdated(updated); setSuccess(mode === 'cancel' ? 'Agendamento cancelado. O horário foi liberado.' : 'Agendamento remarcado com sucesso.'); setMode(null);
    } catch (error) { setMessage(error instanceof Error ? error.message : 'Não foi possível alterar o agendamento.'); }
    finally { setBusy(false); }
  }
  return <div className="appointment-actions">
    {item.status !== 'CANCELADO' && <div className="calendar-action">
      <button className="calendar-trigger" aria-expanded={calendarOpen} onClick={() => setCalendarOpen(value => !value)}>Adicionar ao calendário</button>
      {calendarOpen && <div className="calendar-menu" role="group" aria-label="Opções de calendário">
        <a href={googleCalendarUrl(item)} target="_blank" rel="noreferrer">Google Agenda <span aria-hidden="true">↗</span></a>
        <button onClick={() => { downloadCalendarFile(item); setCalendarOpen(false); }}>Baixar arquivo .ics</button>
        <small>Compatível com Apple Calendar e Outlook.</small>
      </div>}
    </div>}
    {appointmentVisualState(item) === 'upcoming' && <div className="appointment-trigger-group"><button className="appointment-trigger reschedule" onClick={() => { setMessage(''); setSuccess(''); setMode('reschedule'); }}>Remarcar</button><button className="appointment-trigger cancel" onClick={() => { setMessage(''); setSuccess(''); setMode('cancel'); }}>Cancelar</button></div>}
    {success && <p className="appointment-action-success" role="status">{success}</p>}
    {mode && <div className="appointment-action-overlay"><section role="dialog" aria-modal="true" aria-label={mode === 'cancel' ? 'Cancelar agendamento' : 'Remarcar agendamento'} className="appointment-action-dialog">
      <p className="dialog-eyebrow">{mode === 'cancel' ? 'CONFIRMAÇÃO NECESSÁRIA' : 'REAGENDAR HORÁRIO'}</p>
      <h2>{mode === 'cancel' ? 'Cancelar este agendamento?' : 'Escolha um novo horário'}</h2>
      <p className="dialog-appointment">{item.servico?.nome} · {item.profissional?.nome}</p>
      <p className="dialog-rule">Você pode cancelar ou remarcar até o início do atendimento, no fuso {item.estabelecimento?.fusoHorario || 'do estabelecimento'}. A remarcação mantém o preço, a duração e o profissional.</p>
      {mode === 'cancel' ? <p className="dialog-warning">Ao confirmar, o horário será liberado para outras pessoas.</p> : <>
        <label>Nova data<input type="date" value={date} disabled={busy} onChange={event => setDate(event.target.value)} /></label>
        <div className="reschedule-times" aria-busy={loading}>{loading ? <p>Buscando horários…</p> : times.length ? times.map(value => <button disabled={busy} aria-pressed={time === value} className={time === value ? 'selected' : ''} key={value} onClick={() => setTime(value)}>{value.slice(0, 5)}</button>) : <p>Nenhum horário disponível nesta data.</p>}</div>
        {time && <div className="reschedule-review"><span>REVISÃO</span><strong>{new Date(`${date}T12:00:00`).toLocaleDateString('pt-BR', { weekday: 'long', day: '2-digit', month: 'long' })} às {time.slice(0, 5)}</strong><small>O preço, a duração e o profissional permanecem os mesmos.</small></div>}
        <p>O horário atual só será liberado quando a remarcação for confirmada.</p>
      </>}
      {message && <p className="dialog-message" role="alert">{message}{mode === 'reschedule' && <button disabled={busy} onClick={() => setRetry(value => value + 1)}>Atualizar horários</button>}</p>}
      <footer><button className="dialog-back" disabled={busy} onClick={() => setMode(null)}>Voltar</button><button className={mode === 'cancel' ? 'dialog-confirm danger' : 'dialog-confirm'} disabled={busy || (mode === 'reschedule' && (loading || !date || !time))} onClick={() => void confirm()}>{busy ? 'Salvando…' : mode === 'cancel' ? 'Confirmar cancelamento' : 'Confirmar remarcação'}</button></footer>
    </section></div>}
  </div>;
}
