export interface BusinessProfile {
  id: number;
  nome: string;
  estabelecimentoId: number;
}

export interface Service {
  id: number;
  ativo: boolean;
}

export interface Professional {
  id: number;
}

export interface Appointment {
  id: number;
  data: string;
  horaInicio: string;
  status: 'AGENDADO' | 'CANCELADO';
  servico: { nome: string };
  profissional: { nome: string };
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
