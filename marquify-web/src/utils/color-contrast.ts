function channel(value: string) {
  const normalized = Number.parseInt(value, 16) / 255;
  return normalized <= 0.04045 ? normalized / 12.92 : ((normalized + 0.055) / 1.055) ** 2.4;
}

function luminance(color: string) {
  const hex = color.replace('#', '').trim();
  if (!/^[\da-f]{6}$/i.test(hex)) return 1;
  return 0.2126 * channel(hex.slice(0, 2)) + 0.7152 * channel(hex.slice(2, 4)) + 0.0722 * channel(hex.slice(4, 6));
}

export function contrastRatio(foreground: string, background: string) {
  const [light, dark] = [luminance(foreground), luminance(background)].sort((a, b) => b - a);
  return (light + 0.05) / (dark + 0.05);
}

export function hasReadableContrast(foreground: string, background: string, minimum = 4.5) {
  return contrastRatio(foreground, background) >= minimum;
}
