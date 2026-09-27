import type { Appointment } from '../types';

export type AppointmentVisualState = 'upcoming' | 'in-progress' | 'completed' | 'cancelled';

export function appointmentVisualState(appointment: Appointment, now = new Date()): AppointmentVisualState {
  if (appointment.status === 'CANCELADO') return 'cancelled';
  const startsAt = new Date(`${appointment.data}T${appointment.horaInicio}`);
  const endsAt = new Date(`${appointment.data}T${appointment.horaFim || appointment.horaInicio}`);
  if (now >= endsAt) return 'completed';
  if (now >= startsAt) return 'in-progress';
  return 'upcoming';
}

export const appointmentStateLabel: Record<AppointmentVisualState, string> = {
  upcoming: 'Confirmado',
  'in-progress': 'Em atendimento',
  completed: 'Atendido',
  cancelled: 'Cancelado'
};
