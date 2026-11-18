/**
 * Validaciones de la tarjeta en el navegador, antes de mandarla a la pasarela. Solo
 * ayudan al usuario: quien decide es la pasarela.
 */
export type MarcaTarjeta = 'VISA' | 'MASTERCARD' | 'AMEX' | 'OTRA';

export function soloDigitos(valor: string): string {
  return (valor ?? '').replace(/\D/g, '');
}

/** Algoritmo de Luhn: el ultimo digito verifica a los demas. */
export function pasaLuhn(numero: string): boolean {
  const digitos = soloDigitos(numero);
  if (digitos.length < 13 || digitos.length > 19) {
    return false;
  }
  let suma = 0;
  let doble = false;
  for (let i = digitos.length - 1; i >= 0; i--) {
    let d = Number(digitos[i]);
    if (doble) {
      d *= 2;
      if (d > 9) {
        d -= 9;
      }
    }
    suma += d;
    doble = !doble;
  }
  return suma % 10 === 0;
}

export function marca(numero: string): MarcaTarjeta {
  const d = soloDigitos(numero);
  if (d.startsWith('4')) {
    return 'VISA';
  }
  if (/^3[47]/.test(d)) {
    return 'AMEX';
  }
  const prefijo = Number(d.slice(0, 4));
  if ((prefijo >= 5100 && prefijo <= 5599) || (prefijo >= 2221 && prefijo <= 2720)) {
    return 'MASTERCARD';
  }
  return 'OTRA';
}

/** "4242424242424242" -> "4242 4242 4242 4242" (AMEX en 4-6-5). */
export function formatearNumero(valor: string): string {
  const d = soloDigitos(valor).slice(0, 19);
  if (marca(d) === 'AMEX') {
    return [d.slice(0, 4), d.slice(4, 10), d.slice(10, 15)].filter(Boolean).join(' ');
  }
  return d.replace(/(.{4})/g, '$1 ').trim();
}

/** "1228" o "12/28" -> { mes: 12, anio: 2028 }, o null si no es una fecha futura. */
export function leerExpiracion(
  valor: string,
  hoy = new Date(),
): { mes: number; anio: number } | null {
  const d = soloDigitos(valor);
  if (d.length !== 4) {
    return null;
  }
  const mes = Number(d.slice(0, 2));
  const anio = 2000 + Number(d.slice(2));
  if (mes < 1 || mes > 12) {
    return null;
  }
  const finDeMes = new Date(anio, mes, 0, 23, 59, 59);
  return finDeMes >= hoy ? { mes, anio } : null;
}

export function formatearExpiracion(valor: string): string {
  const d = soloDigitos(valor).slice(0, 4);
  return d.length > 2 ? `${d.slice(0, 2)}/${d.slice(2)}` : d;
}

const PESOS = new Intl.NumberFormat('es-MX', { style: 'currency', currency: 'MXN' });

export function pesos(valor: number | null | undefined): string {
  return PESOS.format(valor ?? 0);
}
