/**
 * Formatos de fecha y hora que comparten las pantallas. El backend manda fechas locales
 * sin zona ("2026-11-05T14:00:00"), que el navegador interpreta en hora local.
 */

/** "14:00" a partir de una fecha ISO o de una hora "14:00:00". */
export function hora(valor: string | null | undefined): string {
  if (!valor) {
    return '';
  }
  const match = /(\d{2}):(\d{2})/.exec(valor.includes('T') ? valor.split('T')[1] : valor);
  return match ? `${match[1]}:${match[2]}` : '';
}

/** Hora local actual como "14:05". */
export function horaAhora(ahora = new Date()): string {
  return `${String(ahora.getHours()).padStart(2, '0')}:${String(ahora.getMinutes()).padStart(2, '0')}`;
}

/** Hora del dia (0-23) de una fecha ISO. */
export function horaDelDia(valor: string): number {
  return Number(valor.substring(11, 13));
}

/** Fecha local de hoy como "2026-11-05". */
export function hoyIso(): string {
  return isoDate(new Date());
}

export function isoDate(fecha: Date): string {
  const mes = String(fecha.getMonth() + 1).padStart(2, '0');
  const dia = String(fecha.getDate()).padStart(2, '0');
  return `${fecha.getFullYear()}-${mes}-${dia}`;
}

/** Minutos transcurridos desde una fecha ISO local. */
export function minutosDesde(valor: string, ahora = new Date()): number {
  return Math.round((ahora.getTime() - new Date(valor).getTime()) / 60000);
}

/** Iniciales para un avatar: "Ana Torres" -> "AT". */
export function iniciales(nombre: string | null | undefined): string {
  return (nombre ?? '')
    .split(' ')
    .filter(Boolean)
    .map((parte) => parte.charAt(0))
    .join('')
    .slice(0, 2)
    .toUpperCase();
}

/** "REVISION DE BANOS" -> "Revision de banos": el backend guarda los nombres en mayusculas. */
export function oracion(valor: string | null | undefined): string {
  if (!valor) {
    return '';
  }
  const texto = valor.toLowerCase();
  return texto.charAt(0).toUpperCase() + texto.slice(1);
}
