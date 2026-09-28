import { useEffect, useState } from 'react';
import { Check, Copy, ExternalLink, Link2, Palette, QrCode, Users, Bell, CreditCard, Building2, CalendarClock, X } from 'lucide-react';
import { QRCodeSVG } from 'qrcode.react';
import { useLocation, useNavigate } from 'react-router-dom';
import { AdminShell } from '../components/AdminShell';
import { ApiError, bookingLink, businessSettings, slugAvailability, subscriptionDetails, startSubscriptionCheckout, type BusinessSettings, saveBusinessSettings } from '../services/api';
import type { SubscriptionDetails } from '../types';
import '../dashboard-v0-isolated.css';

interface Props { onExit: () => void; onSessionExpired: () => void; }
const empty: BusinessSettings = { nome: '', slugPublico: '', descricao: '', telefone: '', endereco: '', fusoHorario: 'America/Sao_Paulo', antecedenciaMinimaMinutos: 0, janelaMaximaAgendamentoDias: 365, intervaloEntreServicosMinutos: 0, antecedenciaCancelamentoMinutos: 0 };

export function SettingsPage({ onExit, onSessionExpired }: Props) {
  const navigate = useNavigate();
  const location = useLocation();
  const [settings, setSettings] = useState<BusinessSettings>(empty);
  const [link, setLink] = useState('');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [notice, setNotice] = useState<{ kind: 'success' | 'error'; text: string } | null>(null);
  const [qrOpen, setQrOpen] = useState(false);
  const [slugMessage, setSlugMessage] = useState<{ available: boolean; text: string } | null>(null);
  const [subscription, setSubscription] = useState<SubscriptionDetails | null>(null);
  const [startingCheckout, setStartingCheckout] = useState(false);

  useEffect(() => {
    let active = true;
    Promise.all([businessSettings(), bookingLink(), subscriptionDetails()]).then(([data, publicLink, plan]) => {
      if (!active) return; setSettings(data); setLink(publicLink.urlAgendamento); setSubscription(plan); setLoading(false);
    }).catch((error) => { if (!active) return; if (error instanceof ApiError && error.status === 401) onSessionExpired(); else { setNotice({ kind: 'error', text: 'Não foi possível carregar as configurações.' }); setLoading(false); } });
    return () => { active = false; };
  }, [onSessionExpired]);

  useEffect(() => { if (!settings.slugPublico) return; const timer = window.setTimeout(() => { slugAvailability(settings.slugPublico).then((result) => setSlugMessage({ available: result.disponivel, text: result.mensagem })).catch(() => setSlugMessage(null)); }, 350); return () => window.clearTimeout(timer); }, [settings.slugPublico]);

  const change = <K extends keyof BusinessSettings>(field: K, value: BusinessSettings[K]) => setSettings((current) => ({ ...current, [field]: value }));
  async function copy() { if (!link) return; await navigator.clipboard?.writeText(link); setNotice({ kind: 'success', text: 'Link público copiado.' }); }
  async function submit(event: React.FormEvent) {
    event.preventDefault(); setSaving(true); setNotice(null);
    try { const saved = await saveBusinessSettings(settings); setSettings(saved); setLink((current) => current.replace(/\/agendar\/[^/]+$/, `/agendar/${saved.slugPublico}`)); setNotice({ kind: 'success', text: 'Configurações salvas e regras atualizadas.' }); }
    catch (error) { setNotice({ kind: 'error', text: error instanceof Error ? error.message : 'Não foi possível salvar.' }); }
    finally { setSaving(false); }
  }

  async function checkout() {
    setStartingCheckout(true); setNotice(null);
    try { const result = await startSubscriptionCheckout(); window.location.assign(result.url); }
    catch (error) { setNotice({ kind: 'error', text: error instanceof Error ? error.message : 'Não foi possível abrir o pagamento.' }); setStartingCheckout(false); window.scrollTo({ top: 0, behavior: 'smooth' }); }
  }

  return <AdminShell title="Configurações" active="/configuracoes" userName={settings.nome || undefined} onExit={onExit}><main className="dashboard-page settings-page">
    <div className="admin-page-heading"><p className="page-kicker">SEU ESTABELECIMENTO</p><h1>Configurações</h1><p>Deixe seu negócio pronto para receber agendamentos do seu jeito.</p></div>
    {notice && <div className={`admin-notice ${notice.kind}`} role="status">{notice.text}<button onClick={() => setNotice(null)} aria-label="Fechar mensagem">×</button></div>}
    {loading ? <section className="dashboard-feedback"><p>Carregando configurações…</p></section> : <form onSubmit={submit} className="settings-layout">
      <section className="settings-section"><div className="settings-title"><span className="settings-icon"><Building2 /></span><div><h2>Dados do negócio</h2><p>Informações básicas para a sua página e operação.</p></div></div><div className="settings-fields two-columns">
        <label>Nome do estabelecimento<input required maxLength={160} value={settings.nome} onChange={(e) => change('nome', e.target.value)} /></label>
        <label>URL pública <small>marquify.app/agendar/</small><input required minLength={3} maxLength={60} value={settings.slugPublico} onChange={(e) => change('slugPublico', e.target.value.toLowerCase().replace(/[^a-z0-9-]/g, '').replace(/-+/g, '-').replace(/^-|-$/g, ''))} />{slugMessage && <small className={`slug-feedback ${slugMessage.available ? 'available' : 'unavailable'}`}>{slugMessage.available ? '✓ ' : '× '}{slugMessage.text}</small>}</label>
        <label>Telefone <small>opcional</small><input maxLength={30} placeholder="(11) 99999-9999" value={settings.telefone || ''} onChange={(e) => change('telefone', e.target.value)} /></label>
        <label className="full">Descrição pública <small>aparece abaixo do nome</small><textarea maxLength={180} rows={3} value={settings.descricao || ''} onChange={(e) => change('descricao', e.target.value)} /></label>
        <label className="full">Endereço <small>opcional</small><input maxLength={255} placeholder="Rua, número, bairro e cidade" value={settings.endereco || ''} onChange={(e) => change('endereco', e.target.value)} /></label>
        <label>Fuso horário<select value={settings.fusoHorario} onChange={(e) => change('fusoHorario', e.target.value)}><option value="America/Sao_Paulo">Brasília (São Paulo)</option><option value="America/Manaus">Manaus</option><option value="America/Cuiaba">Cuiabá</option><option value="America/Rio_Branco">Rio Branco</option></select></label>
      </div></section>
      <section className="settings-section"><div className="settings-title"><span className="settings-icon"><CalendarClock /></span><div><h2>Regras de agendamento</h2><p>Essas regras já valem para os horários que seus clientes enxergam.</p></div></div><div className="settings-rules">
        <Rule label="Antecedência mínima" text="Quanto antes o cliente precisa reservar." value={settings.antecedenciaMinimaMinutos} suffix="minutos" onChange={(value) => change('antecedenciaMinimaMinutos', value)} />
        <Rule label="Janela de agendamento" text="Até quantos dias no futuro exibir horários." value={settings.janelaMaximaAgendamentoDias} suffix="dias" min={1} max={365} onChange={(value) => change('janelaMaximaAgendamentoDias', value)} />
        <Rule label="Intervalo entre serviços" text="Tempo livre entre um atendimento e outro." value={settings.intervaloEntreServicosMinutos} suffix="minutos" onChange={(value) => change('intervaloEntreServicosMinutos', value)} />
        <Rule label="Cancelamento e remarcação" text="Prazo mínimo antes do atendimento para alterar." value={settings.antecedenciaCancelamentoMinutos} suffix="minutos" onChange={(value) => change('antecedenciaCancelamentoMinutos', value)} />
      </div></section>
      <section className="settings-section public-settings"><div className="settings-title"><span className="settings-icon"><Palette /></span><div><h2>Página pública e QR Code</h2><p>Compartilhe sua agenda e ajuste a identidade visual.</p></div></div><div className="settings-public-actions"><div className="settings-link"><Link2 size={17} /><span>{link.replace(/^https?:\/\//, '') || 'Gerando seu link…'}</span></div><button type="button" className="secondary-button" onClick={() => void copy()}><Copy size={15} /> Copiar link</button><button type="button" className="secondary-button" onClick={() => setQrOpen(true)} disabled={!link}><QrCode size={15} /> QR Code</button><button type="button" className="primary-button compact" onClick={() => navigate('/personalizacao')}><Palette size={15} /> Personalizar</button></div>{link && <a className="settings-open-link" href={link} target="_blank" rel="noreferrer"><ExternalLink size={14} /> Abrir página pública</a>}</section>
      {subscription && <section className="settings-section settings-plan"><div className="settings-title"><span className="settings-icon"><CreditCard /></span><div><h2>{location.search.includes('plano=novo') ? 'Escolha como começar' : 'Plano e cobrança'}</h2><p>{location.search.includes('plano=novo') ? 'Comece pelo gratuito ou libere o Profissional por 14 dias.' : 'Seu acesso e os limites da operação ficam claros aqui.'}</p></div></div><div className="plan-overview"><div><span className={`plan-badge ${subscription.plano === 'PROFISSIONAL' ? 'professional' : ''}`}>{subscription.plano === 'PROFISSIONAL' ? 'Profissional' : 'Gratuito'}</span><h3>{subscription.status === 'TESTE_GRATIS' ? `Teste grátis: ${subscription.diasRestantesTeste} ${subscription.diasRestantesTeste === 1 ? 'dia restante' : 'dias restantes'}` : subscription.status === 'PENDENTE' ? 'Cadastre seu cartão para liberar o teste' : subscription.plano === 'PROFISSIONAL' ? 'Plano Profissional ativo' : 'Experimente o Profissional por 14 dias'}</h3><p>{subscription.status === 'TESTE_GRATIS' ? 'Você está usando todos os recursos do Profissional. O primeiro pagamento será cobrado apenas ao fim do teste.' : subscription.status === 'PENDENTE' ? 'Aguardamos a confirmação do cartão pelo Mercado Pago. Enquanto isso, o teste ainda não começou.' : subscription.plano === 'PROFISSIONAL' ? 'Equipe e catálogo sem os limites do plano gratuito.' : 'Cadastre um cartão para começar 14 dias grátis. Nenhum valor é cobrado durante esse período.'}</p></div>{subscription.plano === 'GRATUITO' && <button type="button" className="primary-button plan-action" disabled={startingCheckout} onClick={() => void checkout()}>{startingCheckout ? 'Abrindo pagamento…' : subscription.status === 'PENDENTE' ? 'Continuar cadastro do cartão' : 'Começar 14 dias grátis'}</button>}</div><div className="plan-usage"><PlanUsage label="Serviços ativos" value={subscription.uso.servicos} limit={subscription.limites.servicos} /><PlanUsage label="Profissionais ativos" value={subscription.uso.profissionais} limit={subscription.limites.profissionais} /><PlanUsage label="Agendamentos no mês" value={subscription.uso.agendamentosNoMes} limit={subscription.limites.agendamentosMensais} /></div><small className="plan-payment-note">O cartão é cadastrado e os pagamentos são processados com segurança pelo Mercado Pago.</small></section>}
      <section className="settings-section future-settings"><div className="settings-title"><span className="settings-icon"><Bell /></span><div><h2>Próximos recursos</h2><p>Áreas em preparação para acompanhar o crescimento do seu negócio.</p></div></div><div className="future-grid"><Future icon={Bell} title="Notificações" text="Lembretes e avisos automáticos." /><button type="button" onClick={() => navigate('/profissionais')}><Users /><span><strong>Equipe</strong><small>Gerencie profissionais e jornadas.</small></span><em>Gerenciar</em></button></div></section>
      <footer className="settings-savebar"><span>{saving ? 'Salvando suas alterações…' : 'As mudanças ficam ativas assim que forem salvas.'}</span><button className="primary-button" disabled={saving}>{saving ? 'Salvando…' : <><Check size={16} /> Salvar configurações</>}</button></footer>
    </form>}
    {qrOpen && <div className="qr-overlay" onClick={() => setQrOpen(false)}><div className="qr-modal" onClick={(e) => e.stopPropagation()}><button className="modal-close" onClick={() => setQrOpen(false)} aria-label="Fechar"><X size={17} /></button><QRCodeSVG value={link} size={150} level="M" includeMargin /><h2>Seu QR Code</h2><p>Clientes podem apontar a câmera para acessar sua página.</p></div></div>}
  </main></AdminShell>;
}

function Rule({ label, text, value, suffix, min = 0, max = 720, onChange }: { label: string; text: string; value: number; suffix: string; min?: number; max?: number; onChange: (value: number) => void }) { return <label className="settings-rule"><span><b>{label}</b><small>{text}</small></span><span className="number-control"><input type="number" min={min} max={max} value={value} onChange={(e) => onChange(Math.max(min, Math.min(max, Number(e.target.value) || 0)))} /><em>{suffix}</em></span></label>; }
function Future({ icon: Icon, title, text }: { icon: typeof Bell; title: string; text: string }) { return <div><Icon /><span><strong>{title}</strong><small>{text}</small></span><em>Em breve</em></div>; }
function PlanUsage({ label, value, limit }: { label: string; value: number; limit: number }) { return <div><span>{label}</span><strong>{limit < 0 ? `${value} · ilimitado` : `${value} de ${limit}`}</strong></div>; }
