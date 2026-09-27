import type { Appointment, Availability, BusinessProfile, CatalogAppearance, CustomerProfile, CustomerRegistrationPayload, DashboardData, LoginPayload, Professional, PublicBusiness, PublicProfessional, RegisterBusinessPayload, ScheduleBlock, Service } from '../types';

const apiUrl = (import.meta.env.VITE_API_URL || 'http://localhost:8080').replace(/\/$/, '');
import type { DashboardFilters, DashboardAppointmentPage, DashboardIndicators } from '../types';

export class ApiError extends Error {
  constructor(public readonly status: number, message: string) {
    super(message);
  }
}

function token() {
  return localStorage.getItem('marquify-token');
}

async function request<T>(path: string, options: RequestInit = {}, includeAuthentication = true, accessToken?: string): Promise<T> {
  const headers = new Headers(options.headers);
  headers.set('Content-Type', 'application/json');
  if (accessToken) headers.set('Authorization', `Bearer ${accessToken}`);
  else if (includeAuthentication && token()) headers.set('Authorization', `Bearer ${token()}`);

  // Respostas autenticadas não podem reutilizar um 401 armazenado pelo navegador.
  const response = await fetch(`${apiUrl}${path}`, { ...options, headers, cache: 'no-store', credentials: 'omit' });
  const body = response.status === 204 ? null : await response.json().catch(() => ({}));
  if (!response.ok) throw new ApiError(response.status, body.mensagem || 'Não foi possível concluir a operação.');
  return body as T;
}

export async function login(payload: LoginPayload) {
  const response = await request<{ token: string }>('/auth/login', { method: 'POST', body: JSON.stringify(payload) }, false);
  localStorage.setItem('marquify-token', response.token);
  return response.token;
}

export async function registerBusiness(payload: RegisterBusinessPayload) {
  const response = await request<{ token: string; estabelecimentoId: number }>('/auth/cadastro-comercial', {
    method: 'POST',
    body: JSON.stringify({ ...payload, fusoHorario: Intl.DateTimeFormat().resolvedOptions().timeZone || 'America/Sao_Paulo' })
  }, false);
  localStorage.setItem('marquify-token', response.token);
  localStorage.setItem('marquify-estabelecimento-id', String(response.estabelecimentoId));
}

export async function dashboardData(accessToken?: string) {
  return request<DashboardData>('/vendedor/me/dashboard', {}, true, accessToken);
}

function dashboardQuery(filters: DashboardFilters): string {
  const query = new URLSearchParams();
  Object.entries(filters).forEach(([key, value]) => { if (value !== undefined) query.set(key, String(value)); });
  return query.toString();
}
export const dashboardAppointments = (filters: DashboardFilters = {}) =>
  request<DashboardAppointmentPage>(`/vendedor/me/dashboard/agendamentos?${dashboardQuery(filters)}`);
export const dashboardIndicators = (filters: DashboardFilters = {}) =>
  request<DashboardIndicators>(`/vendedor/me/dashboard/indicadores?${dashboardQuery(filters)}`);

export const bookingLink = () => request<{ codigoPublico: string; urlAgendamento: string }>('/vendedor/me/link-agendamento');
export const catalogAppearance = () => request<PublicBusiness>('/vendedor/me/personalizacao');
export const saveCatalogAppearance = (appearance: CatalogAppearance) => request<PublicBusiness>('/vendedor/me/personalizacao', { method: 'PUT', body: JSON.stringify({ nome: appearance.businessName, descricao: appearance.description, corPrimaria: appearance.primaryColor, logo: appearance.logoImage || null, capa: appearance.coverImage || null, capaPosicaoX: appearance.coverPositionX, capaPosicaoY: appearance.coverPositionY, tema: JSON.stringify({ backgroundColor: appearance.backgroundColor, surfaceColor: appearance.surfaceColor, textColor: appearance.textColor, heroTextColor: appearance.heroTextColor, overlayOpacity: appearance.overlayOpacity, fontStyle: appearance.fontStyle, cornerStyle: appearance.cornerStyle, heroAlignment: appearance.heroAlignment, heroSize: appearance.heroSize, serviceLayout: appearance.serviceLayout, showServiceImages: appearance.showServiceImages, showServiceDescriptions: appearance.showServiceDescriptions, showServiceDuration: appearance.showServiceDuration, heroEyebrow: appearance.heroEyebrow, catalogTitle: appearance.catalogTitle }) }) });
export const publicBusiness = (code: string) => request<PublicBusiness>(`/publico/e/${code}`, { cache: 'no-store' }, false);
export const publicServices = (code: string) => request<Service[]>(`/publico/e/${code}/servicos`, {}, false);
export const publicProfessionals = (code: string) => request<PublicProfessional[]>(`/publico/e/${code}/profissionais`, {}, false);
export const publicAvailableTimes = (code: string, professionalId: number, serviceId: number, date: string) =>
  request<{ horarios: string[]; horariosOcupados: string[]; horariosBloqueados: string[] }>(`/publico/e/${code}/profissionais/${professionalId}/horarios?servicoId=${serviceId}&data=${date}`, {}, false);
export const registerCustomer = (payload: CustomerRegistrationPayload) => request<{ token: string; clienteId: number; nome: string }>('/auth/cadastro-cliente', { method: 'POST', body: JSON.stringify(payload) }, false);
export const loginCustomer = (payload: LoginPayload) => request<{ token: string }>('/auth/login', { method: 'POST', body: JSON.stringify(payload) }, false);
export const bookAsCustomer = (accessToken: string, payload: { data: string; horaInicio: string; vendedorId: number; servicoId: number; profissionalId: number }) =>
  request<Appointment>('/agendamento', { method: 'POST', body: JSON.stringify(payload) }, false, accessToken);
export const customerAppointments = (accessToken: string) => request<Appointment[]>('/cliente/me/agendamentos', {}, false, accessToken);
export const customerProfile = (accessToken: string) => request<CustomerProfile>('/cliente/me', {}, false, accessToken);
export const cancelCustomerAppointment = (accessToken: string, appointmentId: number) => request<Appointment>('/agendamento/cancelar', { method: 'PUT', body: JSON.stringify({ agendamentoId: appointmentId }) }, false, accessToken);
export const rescheduleTimes = (accessToken: string, id: number, date: string) => request<string[]>(`/agendamento/${id}/horarios-remarcacao?data=${date}`, {}, false, accessToken);
export const rescheduleAppointment = (accessToken: string, id: number, data: string, horaInicio: string) => request<Appointment>(`/agendamento/${id}/remarcar`, { method: 'PUT', body: JSON.stringify({ data, horaInicio }) }, false, accessToken);

export const myProfile = () => request<BusinessProfile>('/vendedor/me');
export const services = () => request<Service[]>('/vendedor/me/servicos');
export const professionals = () => request<Professional[]>('/vendedor/me/profissionais');
export const saveService = (payload: object, id?: number) => request<Service>(`/vendedor/me/servicos${id ? `/${id}` : ''}`, { method: id ? 'PUT' : 'POST', body: JSON.stringify(payload) });
export async function uploadServicePhoto(serviceId: number, photo: File) {
  const form = new FormData(); form.append('foto', photo);
  const response = await fetch(`${apiUrl}/vendedor/me/servicos/${serviceId}/foto`, { method: 'PUT', headers: { Authorization: `Bearer ${token()}` }, body: form });
  const body = await response.json().catch(() => ({}));
  if (!response.ok) throw new ApiError(response.status, body.mensagem || 'Não foi possível enviar a foto.');
  return body as Service;
}
export const updateServicePhotoPosition = (serviceId: number, x: number, y: number) =>
  request<Service>(`/vendedor/me/servicos/${serviceId}/foto-posicao`, { method: 'PATCH', body: JSON.stringify({ x, y }) });
export const servicePhotoUrl = (service: Service) => service.fotoUrl ? `${apiUrl}${service.fotoUrl}` : '';
export const toggleService = (id: number, active: boolean) => request<Service>(`/vendedor/me/servicos/${id}/${active ? 'desativar' : 'ativar'}`, { method: 'PATCH' });
export const saveProfessional = (nome: string, id?: number) => request<Professional>(`/vendedor/me/profissionais${id ? `/${id}` : ''}`, { method: id ? 'PUT' : 'POST', body: JSON.stringify({ nome }) });
export const toggleProfessional = (id: number, active: boolean) => request<Professional>(`/vendedor/me/profissionais/${id}/${active ? 'desativar' : 'ativar'}`, { method: 'PATCH' });
export const availability = (professionalId: number) => request<Availability>(`/vendedor/me/profissionais/${professionalId}/disponibilidade`);
export const saveAvailability = (professionalId: number, jornadas: Availability['jornadas']) => request<Availability>(`/vendedor/me/profissionais/${professionalId}/disponibilidade`, { method: 'PUT', body: JSON.stringify({ jornadas }) });
export const scheduleBlocks = (professionalId: number) => request<ScheduleBlock[]>(`/vendedor/me/profissionais/${professionalId}/bloqueios`);
export const createScheduleBlock = (professionalId: number, payload: Omit<ScheduleBlock, 'id' | 'profissionalId'>) => request<ScheduleBlock>(`/vendedor/me/profissionais/${professionalId}/bloqueios`, { method: 'POST', body: JSON.stringify(payload) });
export const deleteScheduleBlock = (professionalId: number, blockId: number) => request<void>(`/vendedor/me/profissionais/${professionalId}/bloqueios/${blockId}`, { method: 'DELETE' });
export const appointments = (inicio: string, fim: string) => request<Appointment[]>(`/vendedor/me/agendamentos?inicio=${inicio}&fim=${fim}`);

export function hasSession() {
  const currentToken = token();
  if (!currentToken) return false;
  try {
    const payload = JSON.parse(atob(currentToken.split('.')[1].replace(/-/g, '+').replace(/_/g, '/'))) as { exp?: number };
    if (payload.exp && payload.exp * 1000 <= Date.now()) {
      logout();
      return false;
    }
    return true;
  } catch {
    logout();
    return false;
  }
}

export function logout() {
  ['marquify-token', 'marquify-estabelecimento-id', 'marquify-vendedor-id'].forEach((key) => localStorage.removeItem(key));
}
