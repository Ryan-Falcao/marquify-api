import { useState } from 'react';
import Cropper, { type Area } from 'react-easy-crop';

interface Props { source: string; onCancel: () => void; onApply: (file: File) => void; }

export function ImageCropDialog({ source, onCancel, onApply }: Props) {
  const [crop, setCrop] = useState({ x: 0, y: 0 });
  const [zoom, setZoom] = useState(1);
  const [pixels, setPixels] = useState<Area | null>(null);
  const [saving, setSaving] = useState(false);

  async function apply() {
    if (!pixels || saving) return;
    setSaving(true);
    try { onApply(await croppedFile(source, pixels)); } finally { setSaving(false); }
  }

  return <div className="crop-dialog" role="dialog" aria-modal="true" aria-label="Ajustar foto do serviço">
    <div className="crop-card"><div className="crop-heading"><div><p className="tag">AJUSTAR IMAGEM</p><h3>Defina como a foto vai aparecer</h3><p>Arraste a imagem dentro da moldura e use o zoom.</p></div><button type="button" onClick={onCancel} aria-label="Fechar">×</button></div>
      <div className="crop-stage"><Cropper image={source} crop={crop} zoom={zoom} aspect={4 / 5} cropShape="rect" showGrid={false} objectFit="contain" onCropChange={setCrop} onZoomChange={setZoom} onCropComplete={(_, area) => setPixels(area)} /></div>
      <label className="crop-zoom"><span>−</span><input type="range" min={1} max={3} step={0.01} value={zoom} onChange={(event) => setZoom(Number(event.target.value))} /><span>＋</span></label>
      <div className="crop-actions"><button type="button" className="secondary-button" onClick={onCancel}>Cancelar</button><button type="button" className="button" onClick={() => void apply()} disabled={saving}>{saving ? 'Aplicando…' : 'Aplicar recorte'}</button></div>
    </div>
  </div>;
}

async function croppedFile(source: string, area: Area): Promise<File> {
  const image = await loadImage(source);
  const canvas = document.createElement('canvas'); canvas.width = 960; canvas.height = 1200;
  const context = canvas.getContext('2d'); if (!context) throw new Error('Canvas indisponível');
  context.drawImage(image, area.x, area.y, area.width, area.height, 0, 0, canvas.width, canvas.height);
  const blob = await new Promise<Blob>((resolve, reject) => canvas.toBlob((value) => value ? resolve(value) : reject(new Error('Falha ao recortar')), 'image/jpeg', 0.9));
  return new File([blob], 'servico-recortado.jpg', { type: 'image/jpeg' });
}

function loadImage(source: string) {
  return new Promise<HTMLImageElement>((resolve, reject) => { const image = new Image(); image.crossOrigin = 'anonymous'; image.onload = () => resolve(image); image.onerror = reject; image.src = source; });
}
