export interface BusinessProfile {
  id: number;
  nome: string;
  nomeLoja?: string;
  estabelecimentoId: number;
}

export interface CatalogAppearance {
  businessName: string;
  description: string;
  primaryColor: string;
  coverImage?: string;
  logoImage?: string;
  coverPositionX: number;
  coverPositionY: number;
  backgroundColor: string;
  surfaceColor: string;
  textColor: string;
  heroTextColor: string;
  heroEyebrowColor: string;
  businessNameColor: string;
  descriptionColor: string;
  catalogTitleColor: string;
  serviceNameColor: string;
  serviceDescriptionColor: string;
  serviceDurationColor: string;
  servicePriceColor: string;
  overlayOpacity: number;
  fontStyle: 'moderna' | 'elegante' | 'classica';
  cornerStyle: 'reto' | 'suave' | 'arredondado';
  heroAlignment: 'esquerda' | 'centro';
  heroSize: 'compacta' | 'ampla';
  serviceLayout: 'grade' | 'lista';
  showServiceImages: boolean;
  showServiceDescriptions: boolean;
  showServiceDuration: boolean;
  heroEyebrow: string;
  catalogTitle: string;
}

export interface Professional {
  id: number;
  nome: string;
  ativo: boolean;
}

export interface Service {
  id: number;
  nome: string;
  descricao: string | null;
  preco: number;
  tempo: string;
  ativo: boolean;
  vendedorId: number | null;
  fotoUrl: string | null;
  fotoPosicaoX: number;
  fotoPosicaoY: number;
  profissionais: { id: number; nome: string }[];
}
export interface Availability { profissionalId: number; jornadas: { diaSemana: string; horaInicio: string; horaFim: string }[]; }

export interface ScheduleBlock {
  id: number;
  profissionalId: number;
  tipo: 'FERIAS' | 'PAUSA' | 'AUSENCIA';
  dataInicio: string;
  dataFim: string | null;
  horaInicio: string | null;
  horaFim: string | null;
  motivo: string | null;
  recorrente: boolean;
}

export interface Appointment {
  valorCobrado: number | null;
  duracaoMinutos: number;
  id: number;
  data: string;
  horaInicio: string;
  horaFim: string;
  status: 'AGENDADO' | 'CANCELADO';
  servico: { nome: string } | null;
  profissional: { nome: string } | null;
  estabelecimento?: { nome: string; fusoHorario: string } | null;
}

export interface DashboardData {
  nome: string;
  servicosAtivos: number;
  profissionaisAtivos: number;
  agendamentosHoje: number;
  totalFaturado: number;
  proximosAgendamentos: Appointment[];
}

export type DashboardAppointmentStatus = 'PREVISTO' | 'EM_ATENDIMENTO' | 'FINALIZADO' | 'CANCELADO';
export interface DashboardFilters {
  inicio?: string;
  fim?: string;
  profissionalId?: number;
  clienteId?: number;
  status?: DashboardAppointmentStatus | 'AGENDADO';
  pagina?: number;
  tamanho?: number;
  ordenarPor?: 'data' | 'horaInicio' | 'valorCobrado' | 'cliente' | 'profissional' | 'id';
  direcao?: 'ASC' | 'DESC';
}
export interface DashboardPeriod {
  inicio: string;
  fim: string;
  fusoHorario: string;
  referencia: string;
}
export interface DashboardAppointment {
  id: number;
  data: string;
  horaInicio: string;
  horaFim: string;
  status: DashboardAppointmentStatus;
  clienteId: number | null;
  cliente: string | null;
  profissionalId: number;
  profissional: string;
  servicoId: number | null;
  servico: string | null;
  valorCobrado: number | null;
  duracaoMinutos: number;
}
export interface DashboardAppointmentPage {
  periodo: DashboardPeriod;
  itens: DashboardAppointment[];
  pagina: number;
  tamanho: number;
  totalItens: number;
  totalPaginas: number;
  ordenarPor: NonNullable<DashboardFilters['ordenarPor']>;
  direcao: 'ASC' | 'DESC';
}
export interface DashboardIndicators {
  periodo: DashboardPeriod;
  totalAgendamentos: number;
  previstos: number;
  emAtendimento: number;
  finalizados: number;
  cancelamentos: number;
  faturamentoRealizado: number;
  faturamentoPrevisto: number;
  reservasSemPreco: number;
}

export interface LoginPayload {
  login: string;
  senha: string;
}

export interface RegisterBusinessPayload {
  nome: string;
  estabelecimento: string;
  email: string;
  senha: string;
}

export interface PublicBusiness {
  id: number;
  nome: string;
  fusoHorario: string;
  descricaoPublica: string | null;
  corPrimaria: string | null;
  logoPublico: string | null;
  capaPublica: string | null;
  capaPosicaoX: number;
  capaPosicaoY: number;
  temaPublico: string | null;
}

export interface PublicProfessional {
  id: number;
  nome: string;
}

export interface CustomerRegistrationPayload {
  nome: string;
  numero: string;
  email: string;
  senha: string;
}

export interface CustomerProfile {
  id: number;
  nome: string;
  email: string;
  numero: string;
}
