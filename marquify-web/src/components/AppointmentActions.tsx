import { useEffect, useState } from 'react';
import type { Appointment } from '../types';
import { cancelCustomerAppointment, rescheduleAppointment, rescheduleTimes } from '../services/api';
import { appointmentVisualState } from '../utils/appointment-state';
import './appointment-actions.css';

export function AppointmentActions({ item, token, onUpdated }: { item: Appointment; token: string; onUpdated: (item: Appointment) => void }) {
  const [mode, setMode] = useState<'cancel' | 'reschedule' | null>(null);
  const [date, setDate] = useState(item.data);
  const [time, setTime] = useState('');
  const [times, setTimes] = useState<string[]>([]);
  const [loading, setLoading] = useState(false);
  const [busy, setBusy] = useState(false);
  const [message, setMessage] = useState('');
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
      onUpdated(updated); setMode(null);
    } catch (error) { setMessage(error instanceof Error ? error.message : 'Não foi possível alterar o agendamento.'); }
    finally { setBusy(false); }
  }
  return <div className="appointment-actions">
    <small>{item.valorCobrado == null ? 'Preço não registrado' : item.valorCobrado.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })} · {item.duracaoMinutos} min contratados</small>
    {appointmentVisualState(item) === 'upcoming' && <div><button onClick={() => { setMessage(''); setMode('reschedule'); }}>Remarcar</button><button onClick={() => { setMessage(''); setMode('cancel'); }}>Cancelar</button></div>}
    {mode && <div className="appointment-action-overlay"><section role="dialog" aria-modal="true" aria-label={mode === 'cancel' ? 'Cancelar agendamento' : 'Remarcar agendamento'} className="appointment-action-dialog">
      <h2>{mode === 'cancel' ? 'Cancelar este agendamento?' : 'Escolha um novo horário'}</h2>
      <p>{item.servico?.nome} · {item.profissional?.nome}</p>
      <p>Você pode cancelar ou remarcar até o início do atendimento, no fuso {item.estabelecimento?.fusoHorario || 'do estabelecimento'}. A remarcação mantém o preço, a duração e o profissional.</p>
      {mode === 'cancel' ? <p>Ao confirmar, o horário será liberado para outras pessoas.</p> : <>
        <label>Nova data<input type="date" value={date} disabled={busy} onChange={event => setDate(event.target.value)} /></label>
        <div className="reschedule-times" aria-busy={loading}>{loading ? <p>Buscando horários…</p> : times.length ? times.map(value => <button disabled={busy} aria-pressed={time === value} className={time === value ? 'selected' : ''} key={value} onClick={() => setTime(value)}>{value.slice(0, 5)}</button>) : <p>Nenhum horário disponível nesta data.</p>}</div>
        <p>O horário atual só será liberado quando a remarcação for confirmada.</p>
      </>}
      {message && <p role="alert">{message}{mode === 'reschedule' && <button disabled={busy} onClick={() => setRetry(value => value + 1)}>Atualizar horários</button>}</p>}
      <footer><button disabled={busy} onClick={() => setMode(null)}>Voltar</button><button disabled={busy || (mode === 'reschedule' && (loading || !date || !time))} onClick={() => void confirm()}>{busy ? 'Salvando…' : mode === 'cancel' ? 'Confirmar cancelamento' : 'Confirmar remarcação'}</button></footer>
    </section></div>}
  </div>;
}
