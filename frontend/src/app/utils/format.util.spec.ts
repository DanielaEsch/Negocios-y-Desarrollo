import {
  hora,
  horaDelDia,
  iniciales,
  isoDate,
  minutosDesde,
  oracion,
  horaAhora,
} from './format.util';

describe('format.util', () => {
  it('saca la hora de fechas ISO y de horas sueltas', () => {
    expect(hora('2026-11-05T14:05:00')).toBe('14:05');
    expect(hora('08:00:00')).toBe('08:00');
    expect(hora('08:00')).toBe('08:00');
    expect(hora(null)).toBe('');
    expect(horaDelDia('2026-11-05T07:30:00')).toBe(7);
  });

  it('arma fechas locales y minutos transcurridos', () => {
    expect(isoDate(new Date(2026, 10, 5))).toBe('2026-11-05');
    expect(minutosDesde('2026-11-05T14:00:00', new Date(2026, 10, 5, 14, 45))).toBe(45);
  });

  it('formatea nombres para la pantalla', () => {
    expect(iniciales('Ana Torres')).toBe('AT');
    expect(oracion('REVISION DE BANOS')).toBe('Revision de banos');
  });
});

describe('horaAhora', () => {
  it('no depende del texto de la zona horaria', () => {
    expect(horaAhora(new Date(2026, 10, 5, 9, 7))).toBe('09:07');
  });
});
