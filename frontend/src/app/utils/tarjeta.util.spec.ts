import {
  formatearExpiracion,
  formatearNumero,
  leerExpiracion,
  marca,
  pasaLuhn,
  pesos,
} from './tarjeta.util';

describe('tarjeta.util', () => {
  it('valida con Luhn las tarjetas de prueba', () => {
    expect(pasaLuhn('4242 4242 4242 4242')).toBe(true);
    expect(pasaLuhn('4000000000000002')).toBe(true);
    expect(pasaLuhn('4242424242424241')).toBe(false);
    expect(pasaLuhn('1234')).toBe(false);
  });

  it('reconoce la marca por el prefijo', () => {
    expect(marca('4242')).toBe('VISA');
    expect(marca('5555555555554444')).toBe('MASTERCARD');
    expect(marca('378282246310005')).toBe('AMEX');
    expect(marca('6011')).toBe('OTRA');
  });

  it('agrupa los digitos como vienen impresos', () => {
    expect(formatearNumero('4242424242424242')).toBe('4242 4242 4242 4242');
    expect(formatearNumero('378282246310005')).toBe('3782 822463 10005');
    expect(formatearExpiracion('1228')).toBe('12/28');
  });

  it('solo acepta expiraciones futuras', () => {
    const hoy = new Date(2026, 10, 15);
    expect(leerExpiracion('12/28', hoy)).toEqual({ mes: 12, anio: 2028 });
    expect(leerExpiracion('11/26', hoy)).toEqual({ mes: 11, anio: 2026 });
    expect(leerExpiracion('10/26', hoy)).toBeNull();
    expect(leerExpiracion('13/30', hoy)).toBeNull();
  });

  it('formatea pesos mexicanos', () => {
    expect(pesos(1382.72)).toBe('$1,382.72');
  });
});
