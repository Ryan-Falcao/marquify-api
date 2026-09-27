'use client'

import { useState } from 'react'
import Link from 'next/link'
import {
  ArrowRight,
  BarChart3,
  BellRing,
  CalendarCheck2,
  Check,
  ChevronDown,
  Clock3,
  Copy,
  Grid2X2,
  Link2,
  Menu,
  Palette,
  QrCode,
  Scissors,
  Settings2,
  ShieldCheck,
  Sparkles,
  Users,
  X,
  Zap,
} from 'lucide-react'

const benefits = [
  { icon: Clock3, title: 'Agenda 24 horas', text: 'Seu cliente escolhe o melhor horário, mesmo quando você está atendendo.' },
  { icon: BellRing, title: 'Menos mensagens', text: 'Automatize confirmações e lembretes sem perder o toque pessoal.' },
  { icon: ShieldCheck, title: 'Zero conflitos', text: 'Horários sincronizados para você nunca mais marcar duas pessoas.' },
  { icon: Users, title: 'Equipe alinhada', text: 'Organize profissionais, serviços e comissões em um só lugar.' },
]

const resources = [
  ['Serviços', 'Cadastre sua experiência com duração e preço.', Scissors],
  ['Profissionais', 'Cada pessoa com sua agenda e especialidades.', Users],
  ['Jornadas', 'Defina horários de atendimento sem planilhas.', Clock3],
  ['Agenda', 'Visualize o dia e mantenha o ritmo do negócio.', CalendarCheck2],
  ['QR Code', 'Leve seus agendamentos para qualquer lugar.', QrCode],
  ['Clientes', 'Conheça quem volta e cuide melhor da relação.', Copy],
  ['Dashboard', 'Decisões melhores com dados simples e claros.', BarChart3],
]

const segments = ['Barbearias', 'Salões', 'Clínicas', 'Estúdios', 'Autônomos']

function Brand() {
  return <span className="brand-mark">marquify<span>.</span></span>
}

function Header() {
  const [open, setOpen] = useState(false)
  return (
    <header className="site-header">
      <div className="container header-inner">
        <Link href="/" aria-label="Marquify, início"><Brand /></Link>
        <nav className={`main-nav ${open ? 'is-open' : ''}`} aria-label="Navegação principal">
          <a href="#como-funciona" onClick={() => setOpen(false)}>Como funciona</a>
          <a href="#recursos" onClick={() => setOpen(false)}>Recursos</a>
          <a href="#segmentos" onClick={() => setOpen(false)}>Para quem é</a>
          <Link href="/login" className="mobile-login" onClick={() => setOpen(false)}>Entrar</Link>
        </nav>
        <div className="header-actions">
          <Link href="/login" className="login-link">Entrar</Link>
          <Link href="/cadastro" className="button button-dark button-small">Começar agora <ArrowRight size={15} /></Link>
        </div>
        <button className="menu-button" aria-label={open ? 'Fechar menu' : 'Abrir menu'} onClick={() => setOpen(!open)}>{open ? <X /> : <Menu />}</button>
      </div>
    </header>
  )
}

function BookingMockup() {
  return (
    <div className="booking-phone" aria-label="Prévia do agendamento mobile">
      <div className="phone-speaker" />
      <div className="phone-topbar"><span>9:41</span><span>● ● ●</span></div>
      <div className="booking-cover"><div className="cover-logo"><Scissors size={18} /></div><strong>Ateliê Aurora</strong><span>Beleza que combina com você.</span></div>
      <div className="booking-body">
        <span className="mini-label">ESCOLHA UM SERVIÇO</span>
        <div className="service-choice"><div><strong>Corte + finalização</strong><small>45 min · R$ 85</small></div><span className="selected-dot"><Check size={12} /></span></div>
        <div className="service-choice"><div><strong>Coloração</strong><small>1h 30min · R$ 180</small></div><span className="empty-dot" /></div>
        <span className="mini-label spaced">PROFISSIONAL</span>
        <div className="pro-choice"><div className="avatar">MC</div><div><strong>Marina Costa</strong><small>Especialista em cortes</small></div><ChevronDown size={15} /></div>
        <button className="phone-cta">Continuar <ArrowRight size={14} /></button>
      </div>
    </div>
  )
}

function DashboardMockup() {
  return (
    <div className="dashboard-card" aria-label="Prévia do dashboard Marquify">
      <div className="dash-sidebar"><div className="dash-logo"><Scissors size={15} /></div><div className="dash-side-active"><Grid2X2 size={15} /></div><CalendarCheck2 size={15} /><Users size={15} /><BarChart3 size={15} /><Settings2 size={15} /></div>
      <div className="dash-content">
        <div className="dash-top"><div><small>TERÇA-FEIRA, 18 DE JUNHO</small><h3>Bom dia, Marina <span>✦</span></h3></div><div className="dash-user">MC</div></div>
        <div className="dash-stats"><div><small>AGENDAMENTOS HOJE</small><strong>18</strong><span className="up">+12%</span></div><div><small>FATURAMENTO</small><strong>R$ 1.240</strong><span className="up">+8%</span></div><div><small>CLIENTES NOVOS</small><strong>06</strong><span className="up">+24%</span></div></div>
        <div className="dash-calendar"><div className="calendar-heading"><strong>Agenda de hoje</strong><span>Ver calendário completo →</span></div><div className="timeline"><div>09:00</div><div className="event event-one"><strong>Corte + finalização</strong><span>Lucas Almeida · 09:30</span></div><div>10:00</div><div className="event event-two"><strong>Coloração</strong><span>Júlia Martins · 10:15</span></div><div>11:00</div><div className="event event-three"><strong>Barba express</strong><span>Rafael Souza · 11:30</span></div></div></div>
      </div>
    </div>
  )
}

function Hero() {
  return <section className="hero"><div className="hero-orb orb-one" /><div className="hero-orb orb-two" /><div className="container hero-grid"><div className="hero-copy"><div className="eyebrow"><span className="eyebrow-dot" /> Feito para quem faz acontecer</div><h1>Seus clientes <em>agendam.</em><br />Você cuida do negócio.</h1><p>Uma agenda inteligente para você ganhar tempo, organizar sua rotina e fazer seu negócio crescer.</p><div className="hero-actions"><Link href="/cadastro" className="button button-coral">Começar agora <ArrowRight size={17} /></Link><a href="#como-funciona" className="text-link">Descubra como funciona <ArrowRight size={16} /></a></div><div className="trust-row"><div className="avatar-stack"><span>AN</span><span>BR</span><span>LG</span><span>+</span></div><span>Junte-se a mais de <strong>2.000 negócios</strong></span></div></div><div className="hero-visual"><div className="visual-caption caption-one"><Zap size={14} /> Agenda em tempo real</div><DashboardMockup /><BookingMockup /><div className="visual-caption caption-two"><span className="green-pulse" /> Próximo horário livre: 14:30</div></div></div></section>
}

function Benefits() { return <section className="benefits"><div className="container"><div className="section-intro centered"><span className="kicker">MAIS TEMPO PARA O QUE IMPORTA</span><h2>Menos operação.<br /><em>Mais negócio.</em></h2></div><div className="benefit-grid">{benefits.map(({ icon: Icon, title, text }) => <article className="benefit-card" key={title}><div className="icon-box"><Icon size={20} /></div><h3>{title}</h3><p>{text}</p></article>)}</div></div></section> }

function HowItWorks() { return <section className="how-section" id="como-funciona"><div className="container"><div className="section-intro"><span className="kicker">SIMPLES DESDE O PRIMEIRO DIA</span><h2>Comece em poucos<br /><em>passos.</em></h2></div><div className="steps-grid">{[['01','Configure seu negócio','Conte para a Marquify como sua operação funciona.'],['02','Personalize seu catálogo','Crie uma vitrine que tenha a cara da sua marca.'],['03','Compartilhe seu link','Divulgue onde seus clientes já estão.'],['04','Receba agendamentos','A agenda se organiza. Você faz acontecer.']].map(([num,title,text]) => <div className="step" key={num}><span>{num}</span><div><h3>{title}</h3><p>{text}</p></div></div>)}</div></div></section> }

function CatalogShowcase() { const [active, setActive] = useState('Aparência'); return <section className="catalog-section"><div className="container catalog-grid"><div className="catalog-copy"><span className="kicker">SUA MARCA, SEU JEITO</span><h2>Um catálogo que<br /><em>parece você.</em></h2><p>Transforme seu link de agendamento em uma experiência memorável, do primeiro clique ao horário marcado.</p><ul>{['Logotipo e imagem de capa','Cores do estabelecimento','Fotos e organização dos serviços','Informações, endereço e redes sociais','Pré-visualização em tempo real','Link exclusivo e QR Code'].map(item => <li key={item}><span><Check size={14} /></span>{item}</li>)}</ul><Link href="/cadastro" className="text-link">Personalize seu catálogo <ArrowRight size={16} /></Link></div><div className="catalog-demo"><div className="editor-panel"><div className="editor-top"><strong>Editor do catálogo</strong><span><Sparkles size={14} /> Ao vivo</span></div><div className="editor-tabs">{['Aparência','Serviços','Informações'].map(tab => <button className={active === tab ? 'active' : ''} key={tab} onClick={() => setActive(tab)}>{tab}</button>)}</div><div className="editor-field"><label>Imagem de capa</label><div className="cover-upload"><div className="upload-art"><Scissors size={24} /></div><div><strong>capa-atelie.jpg</strong><small>1200 × 680 px</small></div><button aria-label="Copiar nome do arquivo"><Copy size={15} /></button></div></div><div className="editor-field"><label>Cor principal</label><div className="color-row"><span className="color-swatch" /><code>#E5674D</code><span className="color-check"><Check size={13} /></span></div></div><div className="editor-field"><label>Link do catálogo</label><div className="link-row"><Link2 size={14} /><span>marquify.app/atelieaurora</span><Copy size={14} /></div></div><div className="editor-save"><span><span className="green-pulse" /> Alterações salvas</span><QrCode size={19} /></div></div><div className="catalog-phone"><div className="catalog-cover"><div className="cover-logo large"><Scissors size={19} /></div><strong>Ateliê Aurora</strong><span>Beleza que combina com você.</span></div><div className="catalog-phone-body"><span className="mini-label">ESCOLHA UM SERVIÇO</span><div className="catalog-service"><div><strong>Corte + finalização</strong><small>A partir de R$ 85 · 45 min</small></div><ArrowRight size={15} /></div><div className="catalog-service"><div><strong>Coloração</strong><small>A partir de R$ 180 · 1h 30min</small></div><ArrowRight size={15} /></div><div className="catalog-service"><div><strong>Barba express</strong><small>R$ 45 · 30 min</small></div><ArrowRight size={15} /></div><div className="catalog-social"><span aria-hidden="true">◎</span> @atelieaurora <span>QR</span></div></div></div></div></div></section> }

function BookingDemo() { return <section className="demo-section"><div className="container demo-grid"><div className="demo-copy"><span className="kicker">EXPERIÊNCIA DO CLIENTE</span><h2>Agendar pode ser<br /><em>assim de fácil.</em></h2><p>Uma jornada clara, rápida e sem troca de mensagens. Do serviço escolhido à confirmação, em menos de um minuto.</p><div className="demo-progress"><span className="done" /><span className="done" /><span className="active" /><span /><span /></div><small>Passo 3 de 5 · Escolha um horário</small></div><div className="demo-window"><div className="window-top"><span className="window-dots"><i /><i /><i /></span><span>marquify.app/atelieaurora</span><QrCode size={15} /></div><div className="window-body"><div className="window-step"><span>← Voltar</span><small>3 / 5</small></div><h3>Escolha um horário</h3><p>Quarta, 19 de junho · Marina Costa</p><div className="date-row"><span>TER<br /><b>18</b></span><span className="selected-date">QUA<br /><b>19</b></span><span>QUI<br /><b>20</b></span><span>SEX<br /><b>21</b></span><span>SÁB<br /><b>22</b></span></div><div className="time-grid"><button>09:00</button><button>09:30</button><button>10:00</button><button className="selected-time">10:30 <Check size={12} /></button><button>11:00</button><button>11:30</button><button>14:00</button><button>14:30</button><button>15:00</button></div><button className="window-continue">Continuar <ArrowRight size={15} /></button></div></div></div></section> }

function Resources() { return <section className="resources-section" id="recursos"><div className="container"><div className="section-intro centered"><span className="kicker">TUDO NO SEU RITMO</span><h2>Feito para simplificar<br /><em>cada detalhe.</em></h2></div><div className="resource-grid">{resources.map(([title,text,Icon]) => { const I = Icon as typeof Scissors; return <article key={title as string} className="resource-card"><div className="resource-icon"><I size={19} /></div><h3>{title as string}</h3><p>{text as string}</p><ArrowRight className="resource-arrow" size={16} /></article> })}</div></div></section> }

function Segments() { return <section className="segments-section" id="segmentos"><div className="container segment-inner"><div><span className="kicker">DO SEU JEITO</span><h2>Para todo negócio<br /><em>que cuida de pessoas.</em></h2></div><div className="segment-pills">{segments.map((segment, index) => <div className={index === 0 ? 'segment-pill featured' : 'segment-pill'} key={segment}><span>{['✂','✦','⊹','◒','⌁'][index]}</span>{segment}</div>)}</div></div></section> }

function Footer() { return <footer className="footer"><div className="container footer-top"><div><Brand /><p>Agenda simples para negócios<br />que fazem a diferença.</p></div><div className="footer-links"><div><strong>Produto</strong><a href="#recursos">Recursos</a><a href="#como-funciona">Como funciona</a><a href="#segmentos">Para quem é</a></div><div><strong>Empresa</strong><a href="#">Sobre nós</a><a href="#">Contato</a><a href="#">Blog</a></div><div><strong>Comece agora</strong><Link href="/cadastro">Criar minha conta</Link><Link href="/login">Entrar</Link></div></div></div><div className="container footer-bottom"><span>© 2024 Marquify. Feito no Brasil.</span><span>Termos · Privacidade</span></div></footer> }

export function MarquifyLanding() { return <><Header /><main><Hero /><Benefits /><HowItWorks /><CatalogShowcase /><BookingDemo /><Resources /><Segments /><section className="final-cta"><div className="container"><div className="final-spark"><Sparkles size={15} /> Pronto para simplificar?</div><h2>Seu próximo agendamento<br /><em>começa aqui.</em></h2><p>Organize seu negócio e dê aos seus clientes a experiência que eles merecem.</p><Link href="/cadastro" className="button button-coral">Começar agora <ArrowRight size={17} /></Link></div></section></main><Footer /></> }

export default MarquifyLanding
