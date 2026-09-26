import type { Appointment, BusinessProfile, LoginPayload, Professional, RegisterBusinessPayload, Service } from '../types';

const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080';

export class ApiError extends Error {
  constructor(public readonly status: number, message: string) {
    super(message);
  }
}

function token() {
  return localStorage.getItem('marquify-token');
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const headers = new Headers(options.headers);
  headers.set('Content-Type', 'application/json');
  if (token()) headers.set('Authorization', `Bearer ${token()}`);

  const response = await fetch(`${apiUrl}${path}`, { ...options, headers });
  const body = response.status === 204 ? null : await response.json().catch(() => ({}));
  if (!response.ok) throw new ApiError(response.status, body.mensagem || 'Não foi possível concluir a operação.');
  return body as T;
}

export async function login(payload: LoginPayload) {
  const response = await request<{ token: string }>('/auth/login', { method: 'POST', body: JSON.stringify(payload) });
  localStorage.setItem('marquify-token', response.token);
}

export async function registerBusiness(payload: RegisterBusinessPayload) {
  const response = await request<{ token: string; estabelecimentoId: number }>('/auth/cadastro-comercial', {
    method: 'POST',
    body: JSON.stringify({ ...payload, fusoHorario: Intl.DateTimeFormat().resolvedOptions().timeZone || 'America/Sao_Paulo' })
  });
  localStorage.setItem('marquify-token', response.token);
  localStorage.setItem('marquify-estabelecimento-id', String(response.estabelecimentoId));
}

export async function dashboardData() {
  const [profile, services, professionals, appointments] = await Promise.all([
    request<BusinessProfile>('/vendedor/me'),
    request<Service[]>('/vendedor/me/servicos'),
    request<Professional[]>('/vendedor/me/profissionais'),
    request<Appointment[]>('/vendedor/me/agendamentos')
  ]);
  return { profile, services, professionals, appointments };
}

export function hasSession() {
  return Boolean(token());
}

export function logout() {
  ['marquify-token', 'marquify-estabelecimento-id', 'marquify-vendedor-id'].forEach((key) => localStorage.removeItem(key));
}
