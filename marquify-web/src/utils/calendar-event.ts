import type { Appointment } from '../types';

function compactDateTime(date: string, time: string) {
  return `${date.replaceAll('-', '')}T${time.slice(0, 8).replaceAll(':', '')}`;
}

function escapeIcs(value: string) {
  return value.replaceAll('\\', '\\\\').replaceAll(';', '\\;').replaceAll(',', '\\,').replace(/\r?\n/g, '\\n');
}

function detailsFor(appointment: Appointment) {
  const lines = [
    appointment.servico?.nome && `Serviço: ${appointment.servico.nome}`,
    appointment.profissional?.nome && `Profissional: ${appointment.profissional.nome}`,
    appointment.estabelecimento?.nome && `Estabelecimento: ${appointment.estabelecimento.nome}`
  ].filter(Boolean);
  return lines.join('\n');
}

function eventTitle(appointment: Appointment) {
  return `${appointment.servico?.nome || 'Agendamento'}${appointment.estabelecimento?.nome ? ` · ${appointment.estabelecimento.nome}` : ''}`;
}

export function googleCalendarUrl(appointment: Appointment) {
  const timezone = appointment.estabelecimento?.fusoHorario || 'America/Sao_Paulo';
  const query = new URLSearchParams({
    action: 'TEMPLATE',
    text: eventTitle(appointment),
    dates: `${compactDateTime(appointment.data, appointment.horaInicio)}/${compactDateTime(appointment.data, appointment.horaFim)}`,
    details: detailsFor(appointment),
    location: appointment.estabelecimento?.nome || '',
    ctz: timezone
  });
  return `https://calendar.google.com/calendar/render?${query.toString()}`;
}

export function downloadCalendarFile(appointment: Appointment) {
  const timezone = appointment.estabelecimento?.fusoHorario || 'America/Sao_Paulo';
  const timestamp = new Date().toISOString().replace(/[-:]/g, '').replace(/\.\d{3}/, '');
  const lines = [
    'BEGIN:VCALENDAR',
    'VERSION:2.0',
    'PRODID:-//Marquify//Agenda//PT-BR',
    'CALSCALE:GREGORIAN',
    'METHOD:PUBLISH',
    'BEGIN:VEVENT',
    `UID:marquify-agendamento-${appointment.id}@marquify.app`,
    `DTSTAMP:${timestamp}`,
    `DTSTART;TZID=${timezone}:${compactDateTime(appointment.data, appointment.horaInicio)}`,
    `DTEND;TZID=${timezone}:${compactDateTime(appointment.data, appointment.horaFim)}`,
    `SUMMARY:${escapeIcs(eventTitle(appointment))}`,
    `DESCRIPTION:${escapeIcs(detailsFor(appointment))}`,
    `LOCATION:${escapeIcs(appointment.estabelecimento?.nome || '')}`,
    'STATUS:CONFIRMED',
    'END:VEVENT',
    'END:VCALENDAR',
    ''
  ];
  const blob = new Blob([lines.join('\r\n')], { type: 'text/calendar;charset=utf-8' });
  const url = URL.createObjectURL(blob);
  const anchor = document.createElement('a');
  anchor.href = url;
  anchor.download = `agendamento-marquify-${appointment.data}.ics`;
  document.body.appendChild(anchor);
  anchor.click();
  anchor.remove();
  URL.revokeObjectURL(url);
}
